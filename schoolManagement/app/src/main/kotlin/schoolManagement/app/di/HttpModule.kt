package schoolManagement.app.di


import dagger.Module
import dagger.Provides
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig
import schoolManagement.app.APP_SERVER_HOST_KEY
import schoolManagement.app.APP_SERVER_PORT_KEY
import schoolManagement.app.model.Student
import schoolManagement.app.resources.StudentResource
import javax.ws.rs.core.UriBuilder
import java.util.logging.Logger
import javax.inject.Named
import kotlin.math.log

@Module(includes = [ServiceModule::class, DatabaseModule::class])
class HttpModule {

    private val logger: Logger = Logger.getLogger(HttpModule::class.java.name)



    @Provides
    fun AppConfig(studentResource: StudentResource): ResourceConfig {

        return ResourceConfig()
            .register(studentResource)
    }


    @Provides
    fun provideHttpServer(
        @Named(APP_SERVER_PORT_KEY) port: Int,
        @Named(APP_SERVER_HOST_KEY) host: String,
        application: ResourceConfig
    ): HttpServer {

        return try {
            val uri = UriBuilder.fromUri("http://$host").port(port).build()

           println(uri);

            val server = GrizzlyHttpServerFactory.createHttpServer(uri, application)
            server
        } catch (e: Exception) {
            logger.severe("Failed to start the server: ${e.message}")
            throw e
        }
    }
}
