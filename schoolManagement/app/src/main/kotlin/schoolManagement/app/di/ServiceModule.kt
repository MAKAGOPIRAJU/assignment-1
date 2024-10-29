package schoolManagement.app.di

import dagger.Module
import dagger.Provides
import schoolManagement.app.repository.StudentRepository
import com.mongodb.client.MongoDatabase
import schoolManagement.app.STUDENT_COLLECTION_NAME_KEY
import schoolManagement.app.kafka.ActivityLogProducer
import schoolManagement.app.resources.StudentResource
import javax.inject.Named
import javax.inject.Singleton

@Module
class ServiceModule {

    @Provides
    fun provideStudentRepository(database: MongoDatabase ,
                                 @Named(STUDENT_COLLECTION_NAME_KEY) collection: String,
                                 activityLogProducer: ActivityLogProducer
    ): StudentRepository {
        return StudentRepository(database , collection,activityLogProducer)
    }

    @Provides
    @Singleton
    fun provideStudentResource(studentRepository: StudentRepository): StudentResource {
        return StudentResource(studentRepository)
    }
}

