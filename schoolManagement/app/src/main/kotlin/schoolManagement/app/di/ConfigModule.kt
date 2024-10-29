package schoolManagement.app.di

import dagger.Module
import dagger.Provides
import javax.inject.Named
import schoolManagement.app.*
import schoolManagement.app.helper.AppContext


@Module
class ConfigModule {

    @Provides
    @Named(APP_SERVER_HOST_KEY)
    fun provideSeverHost(): String {
        return AppContext.getProp(APP_SERVER_HOST_KEY).toString()
    }

    @Provides
    @Named(APP_SERVER_PORT_KEY)
    fun provideServerPort(): Int {
        return AppContext.getProp(APP_SERVER_PORT_KEY)?.toInt() ?: 8080
    }

    @Provides
    @Named(APP_DB_NAME_KEY)
    fun provideDbName(): String{
        return AppContext.getProp(APP_DB_NAME_KEY).toString()
    }

    @Provides
    @Named(APP_DB_CONNECTION_STRING_KEY)
    fun provideDbConnectionString(): String{
        return AppContext.getProp(APP_DB_CONNECTION_STRING_KEY).toString()
    }

    @Provides
    @Named(STUDENT_COLLECTION_NAME_KEY)
    fun provideStudentCollection(): String{
        return AppContext.getProp(STUDENT_COLLECTION_NAME_KEY).toString()
    }

}