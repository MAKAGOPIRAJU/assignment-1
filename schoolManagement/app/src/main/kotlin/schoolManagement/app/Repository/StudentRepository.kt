package schoolManagement.app.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.Document
import org.bson.types.ObjectId
import schoolManagement.app.STUDENT_COLLECTION_NAME_KEY
import schoolManagement.app.model.Student
import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import schoolManagement.app.kafka.ActivityLogProducer;
import java.util.logging.Logger

class StudentRepository @Inject constructor(private val database: MongoDatabase,
                                            @Named(STUDENT_COLLECTION_NAME_KEY) collectionName: String,
                                            private val activityLogProducer: ActivityLogProducer){

    private val collection: MongoCollection<Document> = database.getCollection(collectionName)

   private val logger = Logger.getLogger(StudentRepository::class.java.name)


    fun createStudent(student: Student): Student {
        // Create a document from the student's details
        val studentDocument = Document()
            .append("name", student.name)
            .append("age", student.age)
            .append("email", student.email)
            .append("course", student.course)

        // Insert the document into MongoDB
        collection.insertOne(studentDocument)

        // Retrieve the stored document from MongoDB, now including the MongoDB-generated _id
        val storedDocument = collection.find(Filters.eq("_id", studentDocument.getObjectId("_id"))).first()

        // If retrieved, convert _id to string and prepare the JSON for Kafka
        val studentJsonForKafka = storedDocument?.apply {
            put("_id", getObjectId("_id").toString())  // Convert ObjectId to String for JSON compatibility
        }?.toJson() ?: ""

        // Log the JSON string to verify
        println("Sending to Kafka: $studentJsonForKafka")

        // Send the JSON string to Kafka
        activityLogProducer.sendLog(studentJsonForKafka)
//
//        // Set the student ID to the newly generated MongoDB _id and return
//        student.id = studentDocument.getObjectId("_id").toString()
        return student
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
        val objectId = ObjectId(id) // Convert the string ID to ObjectId
        val document = collection.find(Filters.eq("_id", objectId)).first() ?: return null
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

    fun updateStudent(id: String, updatedStudent: Student): Student? {
        return try {
            println("Update student initialized")

            // Convert String ID to ObjectId for MongoDB query
            val objectId = try {
                ObjectId(id)
            } catch (e: IllegalArgumentException) {
                logger.severe("Invalid ID format: $id")
                return null
            }

            // Fetch the existing student with exception handling
            val existingStudent = try {
                getStudent(id) ?: return null
            } catch (e: Exception) {
                logger.severe("Error fetching student with ID $id: ${e.message}")
                throw RuntimeException("An error occurred while fetching the student. Please try again later.")
            }

            val previousStudent: Student? = getStudent(id)

            // Update fields only if they are provided in the request
            updatedStudent.name?.let { existingStudent.name = it }
            updatedStudent.age?.let { existingStudent.age = it }
            updatedStudent.email?.let { existingStudent.email = it }
            updatedStudent.course?.let { existingStudent.course = it }

            // Update the student in the database
            val updateResult = collection.updateOne(
                Filters.eq("_id", objectId), // Use ObjectId instead of String
                Updates.combine(
                    Updates.set("name", existingStudent.name),
                    Updates.set("age", existingStudent.age),
                    Updates.set("email", existingStudent.email),
                    Updates.set("course", existingStudent.course)
                )
            )

            // Send the updated student JSON to Kafka
            try {
                val studentJson = updateStudentWithResourceId(id, updatedStudent, previousStudent)
                activityLogProducer.sendLog(studentJson)
                logger.info("Updated student with ID $id sent to Kafka: $studentJson")
            } catch (e: Exception) {
                logger.severe("Error sending updated student to Kafka: ${e.message}")
            }

            existingStudent // Return the updated student object

        } catch (e: Exception) {
            logger.severe("Error updating student with ID $id: ${e.message}")
            throw RuntimeException("An error occurred while updating the student. Please try again later.")
        }
    }


    fun updateStudentWithResourceId(resourceId: String, updateStudent: Student, previousStudent: Student?): String {

        val gson = Gson()

        // Convert the student object to JSON
        val studentJson = gson.toJson(updateStudent)

        // Create a JsonObject to hold the combined data
        val resultJson = JsonObject().apply {
            // Add the updated student JSON
            this.add("student", gson.fromJson(studentJson, JsonObject::class.java))
            // Add the resourceId
            this.addProperty("_id", resourceId)

            // If previousStudent is not null, add it to the result JSON as "previousStudent"
            previousStudent?.let {
                val previousStudentJson = gson.toJson(it)
                this.add("previousStudent", gson.fromJson(previousStudentJson, JsonObject::class.java))
            }
        }

        println(resultJson)
        // Return the resulting JSON as a string
        return gson.toJson(resultJson)
    }

}
