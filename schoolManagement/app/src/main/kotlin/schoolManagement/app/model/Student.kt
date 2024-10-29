package schoolManagement.app.model

import org.glassfish.grizzly.http.util.TimeStamp
import org.json.JSONObject
import java.sql.Timestamp

data class Student(
    var name: String?,
    var age: Int?,
    var email: String?,
    var course: String?
) {
    constructor(): this(
        name = null,
        age = null,
        email = null,
        course = null
    )

    override fun toString(): String {
        return JSONObject()
            .put("name", this.name ?: JSONObject.NULL)
            .put("age", this.age ?: JSONObject.NULL)
            .put("email", this.email ?: JSONObject.NULL)
            .put("course", this.course ?: JSONObject.NULL)
            .toString()
    }
}


data class LogActivity(
    var description: String?,
    var log_id: String?,
    var createdAt: Timestamp?,
    var student_id: String?
) {
    constructor() : this(
        description = null,
        log_id = null,
        createdAt = null,
        student_id = null
    )

    override fun toString(): String {
        return JSONObject()
            .put("description", this.description ?: JSONObject.NULL)
            .put("log_id", this.log_id ?: JSONObject.NULL)
            .put("createdAt", this.createdAt ?: JSONObject.NULL)
            .put("student_id", this.student_id ?: JSONObject.NULL)
            .toString()
    }
}
