package schoolManagement.app.di

import com.mongodb.client.MongoDatabase
import dagger.Module
import dagger.Provides
import schoolManagement.app.kafka.ActivityLogConsumer
import schoolManagement.app.kafka.ActivityLogProducer
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
class KafkaModule {


    @Provides
    @Singleton
    fun provideActivityLogConsumer(
        database: MongoDatabase,
        @Named("kafka.bootstrap.servers") bootstrapServers: String,
        @Named("kafka.username")  username: String,
        @Named("kafka.password") password: String,
        @Named("kafka.topic")  topic: String): ActivityLogConsumer {
        return ActivityLogConsumer(database ,bootstrapServers, username,password,topic)
    }

    @Provides
    @Singleton
    fun provideActivityLogProducer(

        @Named("kafka.bootstrap.servers") bootstrapServers: String,
        @Named("kafka.username") username: String,
        @Named("kafka.password") password: String,
        @Named("kafka.topic")  topic: String
    ): ActivityLogProducer {
        return ActivityLogProducer(bootstrapServers,username,password,topic)
    }
}
