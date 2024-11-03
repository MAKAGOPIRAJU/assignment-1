package activitylogsmanagement.app.di

import activitylogsmanagement.app.APP_HOST
import activitylogsmanagement.app.APP_PORT
import activitylogsmanagement.app.Repository.ActivityLogRepository
import dagger.Module
import dagger.Provides
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import javax.inject.Named
import javax.inject.Singleton
import javax.ws.rs.core.UriBuilder


@Module
class HttpModule {

    @Provides
    @Singleton
    fun provideHttpServer (@Named(APP_HOST) host: String,
                           @Named(APP_PORT) port: Int): HttpServer {

        val logger = java.util.logging.Logger.getLogger(ActivityLogRepository::class.java.name)

        try{

            val uri  = UriBuilder.fromUri("http://$host").port(port).build()

            println(uri);

            val server = GrizzlyHttpServerFactory.createHttpServer(uri);

            return server;
        }

        catch (e:Exception) {

           throw e;
        }
    }
}