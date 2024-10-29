package schoolManagement.app.di

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import dagger.Module
import dagger.Provides
import schoolManagement.app.APP_DB_CONNECTION_STRING_KEY
import schoolManagement.app.APP_DB_NAME_KEY
import javax.inject.Named
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
   fun connectToMongo(
       @Named(APP_DB_CONNECTION_STRING_KEY) connection_string: String
   ): MongoClient {
        return MongoClients.create(connection_string)
    }

    @Provides
    @Singleton
    fun getDatabase(
        mongoClient: MongoClient, // Injects the MongoClient instance
        @Named(APP_DB_NAME_KEY) databse: String
    ): MongoDatabase {
        // Get and return the MongoDatabase instance corresponding to the provided database name
        return mongoClient.getDatabase(databse)
    }
}