package user.service.controller

import common.exception.ApiException
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import user.service.dto.UserDto
import user.service.dto.UserQueryDto
import user.service.service.UserService

@RestController
@RequestMapping("/api")
class UserController(
    private val service: UserService
) {

    @GetMapping
    fun search(queryDto: UserQueryDto, response: HttpServletResponse): List<UserDto> {
        return service.search(queryDto).run {
            response.setHeader("X-Total-Count", totalElements.toString())
            toList()
        }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String) = service.findDtoById(id)
        ?: throw ApiException(HttpStatus.NOT_FOUND, "user not found")


    @PutMapping
    fun put(@Valid @RequestBody user: UserDto) = service.updateDto(user)
}