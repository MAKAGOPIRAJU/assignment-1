package schoolManagement.app.di

import dagger.Module
import dagger.Provides
import schoolManagement.app.kafka.ActivityLogProducer
import javax.inject.Named
import javax.inject.Singleton

@Module
class KafkaModule {

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
