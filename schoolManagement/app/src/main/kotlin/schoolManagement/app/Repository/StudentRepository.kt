package schoolManagement.app.repository

import com.google.gson.Gson
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.Document
import schoolManagement.app.STUDENT_COLLECTION_NAME_KEY
import schoolManagement.app.model.Student
import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import schoolManagement.app.kafka.ActivityLogProducer;

class StudentRepository @Inject constructor(private val database: MongoDatabase,
                                            @Named(STUDENT_COLLECTION_NAME_KEY) collectionName: String,
                                            private val activityLogProducer: ActivityLogProducer){

    private val collection: MongoCollection<Document> = database.getCollection(collectionName)

    // Create a new student
    fun createStudent(student: Student): Student {

        val student_document = Document()
            .append("name", student.name)
            .append("age", student.age)
            .append("email", student.email)
            .append("course", student.course)

        collection.insertOne(student_document)

            var log_id = UUID.randomUUID();
            var createdAt = Timestamp(System.currentTimeMillis())
            var student_id = student_document.getObjectId("_id").toHexString() // Reference the created student


        // Create a document for LogActivity
        val logActivityDocument = Document()
            .append("description", "New student is Created")
            .append("log_id", log_id)
            .append("createdAt", createdAt)
            .append("student_id", student_id)

        // Convert logActivityDocument to JSON string using Gson
        val gson = Gson()
        val logMessage = gson.toJson(logActivityDocument)

        // Send the log message to Kafka
        activityLogProducer.sendLog(logMessage)

        return student;
    }

    fun fetchAllStudents() : List<Student>{

        val students = mutableListOf<Student>()

        val cursor = collection.find().iterator()

        try {
            while (cursor.hasNext()) {

                val doc = cursor.next()

                val student = Student(
                    name = doc.getString("name"),
                    age = doc.getInteger("age"),
                    email = doc.getString("email"),
                    course = doc.getString("course")
                )
                students.add(student)
            }
        } finally {
            cursor.close() // Close the cursor to avoid memory leaks
        }

      return students;
    }


    // Read a student by ID
    fun getStudent(id: String): Student? {

        val document = collection.find(Filters.eq("_id", id)).first() ?: return null
        return Student(
            name = document.getString("name"),
            age = document.getInteger("age"),
            email = document.getString("email"),
            course = document.getString("course")
        )
    }


    // Delete a student
    fun deleteStudent(id: String): Boolean {

        val deleteResult = collection.deleteOne(Filters.eq("_id", id))
        return deleteResult.deletedCount > 0

    }

}
