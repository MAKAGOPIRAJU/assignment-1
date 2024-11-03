package activitylogsmanagement.app.di


import activitylogsmanagement.app.consumer.ActivityLogConsumer
import dagger.Component
import dagger.Module
import org.glassfish.grizzly.http.server.HttpServer
import javax.inject.Singleton

@Singleton
@Component(modules = [HttpModule::class , DatabaseModule::class,ConfigModule::class,KafkaModule::class])
interface AppComponent {

    fun httpServer(): HttpServer;

    fun activityLogConsumer(): ActivityLogConsumer;

}