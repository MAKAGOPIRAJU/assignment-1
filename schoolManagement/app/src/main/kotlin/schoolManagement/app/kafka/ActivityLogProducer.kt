package schoolManagement.app.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import javax.inject.Inject
import javax.inject.Named

class ActivityLogProducer @Inject constructor(
    @Named("kafka.bootstrap.servers") private val bootstrapServers: String,
    @Named("kafka.username") private val username: String,
    @Named("kafka.password") private val password: String,
    @Named("kafka.topic") private val topic: String
) {

    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username='$username' password='$password';")
            put("security.protocol", "SASL_SSL")
            put("sasl.mechanism", "PLAIN")
        }

        producer = KafkaProducer<String, String>(props)

    }

    fun sendLog(logMessage: String) {
        val record = ProducerRecord<String, String>(topic, logMessage)  // specify <String, String>
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
