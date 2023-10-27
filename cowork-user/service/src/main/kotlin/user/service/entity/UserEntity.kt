package user.service.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import user.api.User
import java.time.LocalDateTime
import java.time.ZoneOffset


@Document(collection = "user")
class UserEntity {
    @Id
    var id: String = ""
    var username: String = ""
    var nickname: String = ""
    var email: String = ""
    var password: String? = null
    var avatar: String? = null
    var sessions: MutableMap<String, Session> = hashMapOf()
    var enable = true

    @CreatedDate
    var createdAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

    @LastModifiedDate
    var updatedAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

    fun toUser(): User = User.newBuilder()
        .setId(id)
        .setUsername(username)
        .setEmail(email)
        .setPassword(password)
        .addAllSessions(
            sessions.values.sortedDescending().map {
                user.api.Session.newBuilder()
                    .setId(it.id)
                    .setIp(it.ip)
                    .setClient(it.client)
                    .setTimestamp(it.createdAt)
                    .build()
            })
        .build()
}

data class Session(val id: String) : Comparable<Session> {
    var ip: String? = null
    var createdAt: Long = 0
    var lastUsage: Long = 0
    var client: String? = null
        get() = field ?: "Unknown"

    constructor(
        id: String,
        client: String?,
        ip: String?,
        timestamp: Long?,
        lastUsage: Long = 0
    ) : this(id) {
        this.ip = ip
        this.createdAt = timestamp ?: 0
        this.client = client
        this.lastUsage = lastUsage
    }

    override fun compareTo(other: Session) = createdAt.compareTo(other.createdAt)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        return id == other.id
    }

    override fun hashCode() = id.hashCode()

}