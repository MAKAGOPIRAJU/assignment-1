package activitylogsmanagement.app.Repository

import activitylogsmanagement.app.ACTIVITY_LOG_COLL_NAME
import activitylogsmanagement.app.APP_DB_NAME
import activitylogsmanagement.app.Model.Student
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import schoolManagement.app.model.ActivityLog
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ActivityLogRepository @Inject constructor(
    @Named(APP_DB_NAME) database: MongoDatabase,
    @Named(ACTIVITY_LOG_COLL_NAME) collection: String
    ) {



    private val collection: MongoCollection<Document> = database.getCollection(collection)

    private val logger = java.util.logging.Logger.getLogger(ActivityLogRepository::class.java.name)

    fun storeLogIntoDb(activityLog: ActivityLog) {

        var resource_id = UUID.randomUUID().toString();

        var student = Student(null,null,null,null);

       var activityLog_document = generateActivityLog(student,student);

        logger.info("$activityLog_document is generated successfully ")
        collection.insertOne(activityLog_document);
    }

//    fun fetchStudentById(studentId: String): Student? {
//
//         val httpClient: HttpClient = HttpClient.newBuilder().build()
//         val schoolManagementBaseUrl = "http://localhost:8080/student"
//
//        val request = HttpRequest.newBuilder()
//            .uri(URI.create("$schoolManagementBaseUrl/$studentId"))
//            .GET()
//            .build()
//
//        return try {
//            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
//
//            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
//                // Parse JSON response to Student object
//                Gson().fromJson(response.body(), Student::class.java)
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }

    fun generateActivityLog(updatedStudent: Student, originalStudent: Student) : Document{

        val resourceId = "student-uuid-${UUID.randomUUID()}"
        val logUuid = "log-uuid-${UUID.randomUUID()}"
        val currentTime = Timestamp(System.currentTimeMillis()).time

        val changes = mutableListOf<Document>()

        if (updatedStudent.name != originalStudent.name) {
            changes.add(Document()
                .append("lastValue", originalStudent.name)
                .append("fieldName", "name")
                .append("fieldType", "String")
                .append("currentValue", updatedStudent.name))
        }
        if (updatedStudent.age != originalStudent.age) {
            changes.add(Document()
                .append("lastValue", originalStudent.age)
                .append("fieldName", "age")
                .append("fieldType", "Integer")
                .append("currentValue", updatedStudent.age))
        }
        if (updatedStudent.email != originalStudent.email) {
            changes.add(Document()
                .append("lastValue", originalStudent.email)
                .append("fieldName", "email")
                .append("fieldType", "String")
                .append("currentValue", updatedStudent.email))
        }
        if (updatedStudent.course != originalStudent.course) {
            changes.add(Document()
                .append("lastValue", originalStudent.course)
                .append("fieldName", "course")
                .append("fieldType", "String")
                .append("currentValue", updatedStudent.course))
        }

        val description = buildString {
            append("Updated student: id=$resourceId, ")
            changes.forEachIndexed { index, change ->
                val fieldName = change.getString("fieldName")
                val lastValue = change.get("lastValue")
                val currentValue = change.get("currentValue")
                if (index > 0) append(", ")
                append("changed $fieldName from $lastValue to $currentValue")
            }
        }

        // Calculate the revision based on the existing logs
        val existingLogsCount = collection.countDocuments(Filters.eq("resourceId", resourceId)).toInt()
        val revision = existingLogsCount + 1

        // Create the activity log document
        val activityLogDocument = Document()
            .append("resourceId", resourceId)
            .append("changes", changes)
            .append("description", description)
            .append("uuid", logUuid)
            .append("revision", revision)
            .append("time", currentTime)
            .append("resourceType", "Student")

        // Insert the document into the collection
        return activityLogDocument;
    }
}