package doc.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Event of collab
 */
enum class Event(@JsonValue val event: Int, val subject: String) {
    Login(1, "events.login"),
    Logout(2, "events.logout"),
    Sync(3, "events.sync"),
    Update(4, "events.update"),
    Save(5, "events.save"),
    ;

    companion object {
        private val mapping = Event.values().associateBy { it.event }

        @JsonCreator
        fun forValue(event: Int) = mapping[event]
    }

}

class CollabMessage(
    val event: Event = Event.Update,
    val uid: String = "",
    val did: String = "",
    val data: ByteArray? = null
)