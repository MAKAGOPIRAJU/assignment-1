package activitylogsmanagement.app.consumer

import activitylogsmanagement.app.*
import activitylogsmanagement.app.Model.Student
import com.google.gson.*
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.bson.Document
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Properties
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Named
import kotlin.concurrent.thread

class ActivityLogConsumer @Inject constructor(
    private val database: MongoDatabase,
    @Named(KAFKA_BOOTSTRAP_SERVERS_URL) private val bootstrapServers: String,
    @Named(KAFKA_USER_NAME) private val username: String,
    @Named(KAFKA_PASSWORD) private val password: String,
    @Named(KAFKA_TOPIC_NAME) private val topic: String,
    @Named(ACTIVITY_LOG_COLL_NAME) private val collection: String
) {

    private val consumer: KafkaConsumer<String, String>
    private val logger = Logger.getLogger(ActivityLogConsumer::class.java.name)
    private val logCollection: MongoCollection<Document> = database.getCollection(collection)

    init {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.GROUP_ID_CONFIG, "activityLogConsumerGroup")
            put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username='$username' password='$password';")
            put("security.protocol", "SASL_SSL")
            put("sasl.mechanism", "PLAIN")
        }

        consumer = KafkaConsumer(props)
        consumer.subscribe(listOf(topic))
    }

    fun startConsuming() {
        thread {
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))
                records.forEach { record ->
                    val rawJson = record.value()
                    try {
                        // Use JsonParser to create a lenient reader
                        val logActivityJson = JsonParser.parseString(rawJson)
                        processLog(logActivityJson.asJsonObject)
                    } catch (e: JsonSyntaxException) {
                        logger.severe("Failed to parse JSON: $rawJson")
                        logger.severe("Exception: ${e.message}")
                    }
                }
            }
        }
    }

    private fun processLog(logActivityJson: JsonObject) {
        val resourceId = logActivityJson.get("_id")?.asString ?: return
        val existingLog = logCollection.find(Document("resourceId", resourceId)).first()

        if (existingLog == null) {
            createLogEntry(logActivityJson, resourceId)
        } else {
            updateLogEntry(logActivityJson, existingLog, resourceId)
        }
    }

    private fun createLogEntry(logActivityJson: JsonObject, resourceId: String) {
        val resourceType = if (logActivityJson.has("course")) "Student" else "Teacher"
        val description = "Created a new $resourceType: ${logActivityJson.toString()}"

        val logDocument = Document().apply {
            put("resourceId", resourceId)
            put("description", description)
            put("changes", null)
            put("uuid", "log-uuid-${System.currentTimeMillis()}") // Generate unique UUID
            put("revision", 0)
            put("time", System.currentTimeMillis()) // Current time in milliseconds
            put("resourceType", resourceType)
        }

        logCollection.insertOne(logDocument)
        logger.info("Created new $resourceType log: $description")
    }

    private fun updateLogEntry(logActivityJson: JsonObject, existingLog: Document, resourceId: String) {
        val changesArray = existingLog.getList("changes", Document::class.java) ?: mutableListOf() // Retrieve existing changes
        val descriptionParts = mutableListOf<String>()

        // Fetch the current `student` object and `previousStudent` object from logActivityJson
        val studentJson = logActivityJson.getAsJsonObject("student")
        val previousStudentJson = logActivityJson.getAsJsonObject("previousStudent")

        // Retrieve the current revision from the existing log and increment it
        val currentRevision = existingLog.getInteger("revision", 0)
        val newRevision = currentRevision + 1

        // Use entrySet() to iterate over the properties in `student`
        studentJson.entrySet().forEach { entry ->
            val fieldName = entry.key
            val currentValue = entry.value

            // Get the last value from `previousStudent`
            val lastValue = previousStudentJson?.get(fieldName)?.asString

            // Compare last value with current value
            if (lastValue != currentValue.toString()) {
                // Record the new change details in a Document
                val newChange = Document().apply {
                    put("fieldName", fieldName)
                    put("lastValue", lastValue ?: "null")  // Use "null" if lastValue is null
                    put("currentValue", currentValue.toString())
                }
                changesArray.add(newChange) // Add new change to the existing changes array
                descriptionParts.add("$fieldName from $lastValue to $currentValue")
            }
        }

        // Build the description
        val description = "Updated record: id=$resourceId, " + descriptionParts.joinToString(", ")

        // Update the existing log document in the collection
        val updateQuery = Document("\$set", Document("changes", changesArray)
            .append("description", description)
            .append("revision", newRevision)) // Set the new revision value

        logCollection.updateOne(Document("resourceId", resourceId), updateQuery)

        logger.info("Updated log: $description")
    }




}
