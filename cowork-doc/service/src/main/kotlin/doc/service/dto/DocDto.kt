package doc.service.dto

import doc.service.entity.DocEntity
import doc.service.entity.EditBy
import doc.service.entity.Link
import doc.service.entity.OwnerEnum
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class DocDto(
    val id: String = "",
    @field:NotEmpty(message = "{title.not-empty}")
    @field:Size(min = 1, max = 32, message = "{title.size-error}")
    val title: String,
    val clients: Map<String, EditBy>?,
    val links: List<Link>?,
    @field:NotNull(message = "{owner.not-null}")
    val owner: OwnerEnum?,
    @field:NotNull(message = "{owner.not-null}")
    val ownerId: String?,
    val delete: Boolean = false,
    val createdAt: Long?,
    val updatedAt: Long?
) {
    fun toEntity() = DocEntity(id, title, owner!!, ownerId!!)
}

fun DocEntity.toDocDto() = DocDto(id, title, clients, links, owner, ownerId, trash, createdAt, updatedAt)