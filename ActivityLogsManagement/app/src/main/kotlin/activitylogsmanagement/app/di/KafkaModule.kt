package activitylogsmanagement.app.di

import activitylogsmanagement.app.consumer.ActivityLogConsumer;
import com.mongodb.client.MongoDatabase
import dagger.Module
import dagger.Provides
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
}