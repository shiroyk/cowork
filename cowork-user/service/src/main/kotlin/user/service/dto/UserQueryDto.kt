package user.service.dto

import common.dto.BaseQueryDto

data class UserQueryDto(
    val ids: List<String>?,
    val username: String?,
    val nickname: String?,
    val email: String?,
) : BaseQueryDto()
