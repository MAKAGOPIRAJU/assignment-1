package activitylogsmanagement.app.Model

import com.google.gson.Gson

data class Student(
    var name: String?,
    var age: Int?,
    var email: String?,
    var course: String?
) {
    companion object {
        fun fromJson(json: String): Student {
            return Gson().fromJson(json, Student::class.java)
        }
    }
}
