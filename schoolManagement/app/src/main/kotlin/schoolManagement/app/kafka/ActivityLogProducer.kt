package schoolManagement.app.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import javax.inject.Inject

class ActivityLogProducer @Inject constructor() {

    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "pkc-12576z.us-west2.gcp.confluent.cloud:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required username='5G25HLVZ5P7DWLZ6' password='ZM2F1QqZuUHcor6gY681ipf9vG2aZyOU6bHr69RkC9OC0EkEnn/bu45mbS7NHgIm';"
        props["security.protocol"] = "SASL_SSL"
        props["sasl.mechanism"] = "PLAIN"

        producer = KafkaProducer<String, String>(props)

    }

    fun sendLog(logMessage: String) {
        val record = ProducerRecord<String, String>("activityLog", logMessage)  // specify <String, String>
        producer.send(record) { metadata, exception ->
            if (exception != null) {
                println("Error sending log: ${exception.message}")
            } else {
                println("Log sent successfully to ${metadata.topic()} at offset ${metadata.offset()}")
            }
        }
    }


    fun close() {
        producer.close()
    }
}
