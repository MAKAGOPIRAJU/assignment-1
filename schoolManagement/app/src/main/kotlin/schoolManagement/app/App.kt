package schoolManagement.app

import com.mongodb.client.MongoDatabase
import schoolManagement.app.di.*
import schoolManagement.app.helper.AppContext
import schoolManagement.app.kafka.ActivityLogConsumer

fun main(args: Array<String>) {

    AppContext.init(args)

    val configModule = ConfigModule()

    val appComponent: AppComponent = DaggerAppComponent.builder()
        .configModule(ConfigModule())
        .build()

    val server = appComponent.server();

    // Get the MongoDB database instance from Dagger
    val mongoDatabase: MongoDatabase = appComponent.mongoDatabase()

    // Create and start the consumer
    val activityLogConsumer = ActivityLogConsumer(mongoDatabase)
    activityLogConsumer.startConsuming()

    server.start()
}

