package doc.service.entity

import common.constants.Permission
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document("doc")
class DocEntity(
    @Id
    var id: String,
    var title: String,
    var owner: OwnerEnum,
    var ownerId: String
) {
    // uid => EditBy
    var clients: Map<String, EditBy> = emptyMap()
    var links: List<Link> = emptyList()
    var trash: Boolean = false

    @CreatedDate
    var createdAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

    @LastModifiedDate
    var updatedAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
}

@Document("docNode")
class DocNode(
    @Indexed
    val did: String,
    @Indexed
    val uid: String,
    val data: ByteArray,
) {
    @Id
    lateinit var id: String
}

data class Link(val url: String, val permission: Permission)

enum class OwnerEnum {
    User, Group
}

data class EditBy(
    val clientId: Int,
    var updateAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
)