package activitylogsmanagement.app.di

import activitylogsmanagement.app.*
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
        @Named(KAFKA_BOOTSTRAP_SERVERS_URL) bootstrapServers: String,
        @Named(KAFKA_USER_NAME)  username: String,
        @Named(KAFKA_PASSWORD) password: String,
        @Named(KAFKA_TOPIC_NAME)  topic: String,
        @Named(ACTIVITY_LOG_COLL_NAME) collection: String): ActivityLogConsumer {
        return ActivityLogConsumer(database ,bootstrapServers, username,password,topic,collection)
    }
}