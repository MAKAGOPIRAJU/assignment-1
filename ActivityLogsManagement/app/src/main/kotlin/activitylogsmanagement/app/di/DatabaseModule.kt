package activitylogsmanagement.app.di

import activitylogsmanagement.app.APP_DB_NAME
import activitylogsmanagement.app.DB_CONNECTION_URL
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideMongoClient(
        @Named(DB_CONNECTION_URL) dbConnectionUrl: String
    ): MongoClient {

        return MongoClients.create(dbConnectionUrl);
    }

    @Singleton
    @Provides
    fun provideDatabase(
        mongoClient: MongoClient,
        @Named(APP_DB_NAME) dbName : String
    ) : MongoDatabase {

        return mongoClient.getDatabase(dbName);
    }
}