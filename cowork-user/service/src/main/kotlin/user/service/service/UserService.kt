package user.service.service

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import user.service.dto.UserDto
import user.service.dto.UserQueryDto
import user.service.repository.UserRepository

@Primary
interface UserService : UserRepository, UserServiceExt

interface UserServiceExt {
    fun search(queryDto: UserQueryDto): Page<UserDto>

    fun updateDto(user: UserDto): UserDto

    fun findDtoById(id: String): UserDto?
}