package schoolManagement.app.kafka

import com.google.gson.Gson
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
import kotlin.concurrent.thread

class ActivityLogConsumer @Inject constructor(private val database: MongoDatabase) {

    private val consumer: KafkaConsumer<String, String>

    private val logger = Logger.getLogger(ActivityLogConsumer::class.java.name)

    init {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "pkc-12576z.us-west2.gcp.confluent.cloud:9092"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.GROUP_ID_CONFIG] = "activityLogConsumerGroup"
        props["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required username='5G25HLVZ5P7DWLZ6' password='ZM2F1QqZuUHcor6gY681ipf9vG2aZyOU6bHr69RkC9OC0EkEnn/bu45mbS7NHgIm';"
        props["security.protocol"] = "SASL_SSL"
        props["sasl.mechanism"] = "PLAIN"

        consumer = KafkaConsumer(props)
        consumer.subscribe(listOf("activityLog"))
    }

    fun startConsuming() {
        thread {
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))
                records.forEach { record ->
                    // Parse the log message JSON string back into a JsonObject
                    val gson = Gson()
                    val logActivityJson = gson.fromJson(record.value(), JsonObject::class.java)

                    // Create a Document from the JsonObject
                    val logDocument = Document()
                    logActivityJson.entrySet().forEach { entry ->
                        logDocument[entry.key] = entry.value
                    }

                    // Store the document in the activityLogs collection
                    val logCollection = database.getCollection("activityLogs")
                    logCollection.insertOne(logDocument)

                    logger.info("The activity log is consumed and added to the database");
                }
            }
        }
    }
}
