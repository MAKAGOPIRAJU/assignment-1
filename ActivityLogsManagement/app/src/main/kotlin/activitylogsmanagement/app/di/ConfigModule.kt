package activitylogsmanagement.app.di

import activitylogsmanagement.app.*
import activitylogsmanagement.app.helper.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ConfigModule {

    @Provides
    @Named(APP_HOST)
    fun provideSeverHost(): String {
        return AppContext.getProp(APP_HOST).toString()
    }

    @Provides
    @Named(APP_PORT)
    fun provideServerPort(): Int {
        return AppContext.getProp(APP_PORT)?.toInt() ?: 8080
    }

    @Provides
    @Named(APP_DB_NAME)
    fun provideDbName(): String{
        return AppContext.getProp(APP_DB_NAME).toString()
    }

    @Provides
    @Named(DB_CONNECTION_URL)
    fun provideDbConnectionString(): String{
        return AppContext.getProp(DB_CONNECTION_URL).toString()
    }

    @Provides
    @Named(ACTIVITY_LOG_COLL_NAME)
    fun provideStudentCollection(): String{
        return AppContext.getProp(ACTIVITY_LOG_COLL_NAME).toString()
    }

    @Provides
    @Named(KAFKA_BOOTSTRAP_SERVERS_URL)
    fun provideKafkaBootstrapServers(): String {
        return AppContext.getProp(KAFKA_BOOTSTRAP_SERVERS_URL).toString()
    }

    @Provides
    @Named(KAFKA_USER_NAME)
    fun provideKafkaUsername(): String {
        return AppContext.getProp(KAFKA_USER_NAME).toString()
    }

    @Provides
    @Named(KAFKA_PASSWORD)
    fun provideKafkaPassword(): String {
        return AppContext.getProp(KAFKA_PASSWORD).toString()
    }

    @Provides
    @Named(KAFKA_TOPIC_NAME)
    fun provideKafkaTopic(): String {
        return AppContext.getProp(KAFKA_TOPIC_NAME).toString()
    }

}