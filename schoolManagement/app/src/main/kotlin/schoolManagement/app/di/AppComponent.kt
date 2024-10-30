package schoolManagement.app.di

import com.mongodb.client.MongoDatabase
import org.glassfish.grizzly.http.server.HttpServer // Use Grizzly's HttpServer
import dagger.Component
import schoolManagement.app.kafka.ActivityLogConsumer
import schoolManagement.app.resources.StudentResource
import javax.inject.Singleton

@Singleton
@Component(modules = [HttpModule::class , DatabaseModule::class, ServiceModule::class,ConfigModule::class,KafkaModule::class])

interface AppComponent {

     fun server(): HttpServer

    fun getActivityLogConsumer(): ActivityLogConsumer

}
