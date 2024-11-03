package activitylogsmanagement.app.consumer;

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mongodb.client.MongoDatabase
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.bson.Document
import java.time.Duration
import java.util.Properties
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Named
import kotlin.concurrent.thread

class ActivityLogConsumer @Inject constructor(
    private val database: MongoDatabase,
    @Named("kafka.bootstrap.servers") private val bootstrapServers: String,
    @Named("kafka.username") private val username: String,
    @Named("kafka.password") private val password: String,
    @Named("kafka.topic") private val topic: String,
) {

    private val consumer: KafkaConsumer<String, String>
    private val logger = Logger.getLogger(ActivityLogConsumer::class.java.name)

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
                    val gson = Gson()
                    val logActivityJson = gson.fromJson(record.value(), JsonObject::class.java)
                    processLog(logActivityJson)
                }
            }
        }
    }

    private fun processLog(logActivityJson: JsonObject) {
        val logCollection = database.getCollection("activityLogs")

        // Check if _id is a direct string instead of a nested JSON object
        val resourceId = if (logActivityJson.get("_id")?.isJsonPrimitive == true) {
            logActivityJson.get("_id")?.asString
        } else {
            logActivityJson.get("_id")?.asJsonObject?.get("\$oid")?.asString
        } ?: ""

        if (resourceId.isNotEmpty()) {
            val existingLog = logCollection.find(Document("resourceId", resourceId)).first()

            // Determine if this is a new entry based on the existence of resourceId in the collection
            if (existingLog == null) {
                // Check for Student or Teacher based on the presence of the "subject" field
                val resourceType = if (logActivityJson.has("subject")) "Teacher" else "Student"
                val description = "Created a new $resourceType: $logActivityJson"

                val logDocument = Document().apply {
                    put("resourceId", resourceId)
                    put("description", description)
                    put("changes", null)
                    put("resourceType", resourceType)
                }

                logCollection.insertOne(logDocument)
                logger.info("Created new $resourceType log: $description")
            } else {
                // Existing entry (update case)
                val changes = logActivityJson.getAsJsonArray("changes")
                val description = generateUpdateDescription(changes, resourceId)
                val resourceType = existingLog.getString("resourceType") // Assume existing resourceType

                val logDocument = Document().apply {
                    put("resourceId", resourceId)
                    put("description", description)
                    put("changes", changes)
                    put("resourceType", resourceType)
                }

                logCollection.insertOne(logDocument)
                logger.info("Updated $resourceType log: $description")
            }
        }
    }


    private fun generateUpdateDescription(changes: JsonElement, resourceId: String): String {
        val descriptions = mutableListOf<String>()

        for (change in changes.asJsonArray) {
            val lastValue = change.asJsonObject.get("lastValue")
            val fieldName = change.asJsonObject.get("fieldName").asString
            val currentValue = change.asJsonObject.get("currentValue")

            descriptions.add("$fieldName from ${lastValue.toString()} to ${currentValue.toString()}")
        }

        return "Updated record: id=$resourceId, " + descriptions.joinToString(", ")
    }
}
