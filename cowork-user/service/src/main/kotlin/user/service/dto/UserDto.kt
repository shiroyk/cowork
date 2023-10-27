package user.service.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import user.service.entity.Session
import user.service.entity.UserEntity

open class UserDto(
    open var id: String? = null,
    @field:NotEmpty(message = "{username.not-empty}")
    @field:Size(min = 4, max = 10, message = "{username.size-error}")
    open val username: String? = null,
    @field:NotEmpty(message = "{nickname.not-empty}")
    @field:Size(max = 10, message = "{nickname.size-error}")
    open val nickname: String? = null,
    @field:NotEmpty(message = "{email.not-empty}")
    @field:Email(message = "{email.error}")
    open val email: String? = null,
    open val avatar: String? = null
)

fun UserEntity.toUserDto() = UserDto(id, username, nickname, email, avatar)

data class UserInfoDto(
    override var id: String? = null,
    override val username: String? = null,
    override val nickname: String? = null,
    override val email: String? = null,
    override val avatar: String? = null,
    var sessions: List<Session> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
) : UserDto()


fun UserEntity.toUserInfoDto() =
    UserInfoDto(id, username, nickname, email, avatar, sessions.values.sortedDescending(), createdAt, updatedAt)