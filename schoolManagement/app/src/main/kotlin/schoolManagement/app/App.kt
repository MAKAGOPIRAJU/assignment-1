package schoolManagement.app

import com.mongodb.client.MongoDatabase
import schoolManagement.app.di.*
import schoolManagement.app.helper.AppContext
import javax.inject.Named

fun main(args: Array<String>) {

    AppContext.init(args)

    val configModule = ConfigModule()

    val appComponent: AppComponent = DaggerAppComponent.builder()
        .configModule(ConfigModule())
        .build()

    val server = appComponent.server();


    server.start()
}

