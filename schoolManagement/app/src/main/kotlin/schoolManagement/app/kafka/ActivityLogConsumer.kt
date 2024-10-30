package schoolManagement.app.kafka

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

    private fun jsonToDocument(jsonObject: JsonObject): Document {
        val document = Document()
        jsonObject.entrySet().forEach { (key, value) ->
            document[key] = convertJsonElement(value)
        }
        return document
    }

    private fun convertJsonElement(element: JsonElement): Any? {
        return when {
            element.isJsonNull -> null
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber
                    primitive.isString -> primitive.asString
                    else -> primitive.asString
                }
            }
            element.isJsonObject -> jsonToDocument(element.asJsonObject)
            element.isJsonArray -> element.asJsonArray.map { convertJsonElement(it) }
            else -> element.toString()
        }
    }

    fun startConsuming() {
        // Start a new thread to handle the consuming process asynchronously
        thread {
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))

                // Iterate over each record (message) retrieved from the poll
                records.forEach { record ->
                    val gson = Gson()

                    // Deserialize the record's value (JSON string) to a JsonObject
                    val logActivityJson = gson.fromJson(record.value(), JsonObject::class.java)

                    // Convert the JsonObject to a MongoDB Document for insertion
                    val logDocument = jsonToDocument(logActivityJson)

                    val logCollection = database.getCollection("activityLogs")

                    logCollection.insertOne(logDocument)

                    logger.info("The activity log is consumed and added to the database.")
                }
            }
        }
    }

}
