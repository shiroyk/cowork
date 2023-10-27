package user.service.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import user.service.entity.UserEntity
import java.util.*

@Repository
interface UserRepository : MongoRepository<UserEntity, String> {

    fun findByIdIn(ids: List<String>): List<UserEntity>

    fun findByUsername(username: String): Optional<UserEntity>

    fun findByEmail(email: String): Optional<UserEntity>
}