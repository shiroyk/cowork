package user.service.service.impl

import common.exception.ApiException
import common.utils.getUserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import user.service.dto.UserDto
import user.service.dto.UserQueryDto
import user.service.dto.toUserDto
import user.service.dto.toUserInfoDto
import user.service.entity.UserEntity
import user.service.service.UserServiceExt

@Service
open class UserServiceExtImpl(
    private val mongo: MongoTemplate
) : UserServiceExt {
    override fun search(queryDto: UserQueryDto): Page<UserDto> {
        val query = Query().apply {
            if (!queryDto.ids.isNullOrEmpty()) {
                addCriteria(Criteria.where("id").`in`(queryDto.ids))
            }
            if (!queryDto.username.isNullOrEmpty()) {
                addCriteria(Criteria.where("username").regex(queryDto.username))
            }
            if (!queryDto.nickname.isNullOrEmpty()) {
                addCriteria(Criteria.where("nickname").regex(queryDto.nickname))
            }
            if (!queryDto.email.isNullOrEmpty()) {
                addCriteria(Criteria.where("email").regex(queryDto.email))
            }
        }.with(queryDto.page).with(queryDto.sortBy)

        return PageImpl(
            mongo.find(query, UserEntity::class.java).map { it.toUserDto() },
            Pageable.unpaged(),
            mongo.count(query, UserEntity::class.java)
        )
    }

    @Transactional
    override fun updateDto(user: UserDto): UserDto {
        mongo.findOne(Query().apply {
            addCriteria(Criteria.where("username").`is`(user.username!!))
        }, UserEntity::class.java)?.let {
            if (it.id != getUserId()) {
                throw ApiException(HttpStatus.BAD_REQUEST, "username already exists")
            }
        }

        val entity: UserEntity = mongo.findById(getUserId())
            ?: throw ApiException(HttpStatus.NOT_FOUND, "user not found")

        mongo.save(entity.apply {
            username = user.username!!
            nickname = user.nickname!!
            email = user.email!!
        })
        user.id = entity.id
        return entity.toUserDto()
    }

    override fun findDtoById(id: String): UserDto? {
        val entity: UserEntity? = mongo.findById(id)
        return entity?.let {
            if (id == getUserId()) it.toUserInfoDto() else it.toUserDto()
        }
    }
}