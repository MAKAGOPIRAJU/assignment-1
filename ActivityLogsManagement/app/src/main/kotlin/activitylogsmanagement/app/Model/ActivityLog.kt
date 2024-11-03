package schoolManagement.app.model

import org.json.JSONObject
import java.sql.Timestamp

data class ActivityLog(
    var resourceId: String?,
    var changes: List<Change>?,
    var description: String?,
    var uuid: String?,
    var revision: Int?,
    var time: Timestamp?,
    var resourceType: String?
) {
    constructor() : this(
        resourceId = null,
        changes = null,
        description = null,
        uuid = null,
        revision = null,
        time = null,
        resourceType = null
    )

    override fun toString(): String {
        val changesArray = changes?.map { it.toJSONObject() } ?: emptyList<JSONObject>()
        return JSONObject()
            .put("resourceId", this.resourceId ?: JSONObject.NULL)
            .put("changes", changesArray)
            .put("description", this.description ?: JSONObject.NULL)
            .put("uuid", this.uuid ?: JSONObject.NULL)
            .put("revision", this.revision ?: JSONObject.NULL)
            .put("time", this.time ?: JSONObject.NULL)
            .put("resourceType", this.resourceType ?: JSONObject.NULL)
            .toString()
    }


    data class Change(
        var lastValue: Any?,
        var fieldName: String?,
        var fieldType: String?,
        var currentValue: Any?
    ) {
        constructor() : this(
            lastValue = null,
            fieldName = null,
            fieldType = null,
            currentValue = null
        )

        fun toJSONObject(): JSONObject {
            return JSONObject()
                .put("lastValue", this.lastValue ?: JSONObject.NULL)
                .put("fieldName", this.fieldName ?: JSONObject.NULL)
                .put("fieldType", this.fieldType ?: JSONObject.NULL)
                .put("currentValue", this.currentValue ?: JSONObject.NULL)
        }
    }
}


