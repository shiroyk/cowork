package user.service.service.impl


import com.google.protobuf.Empty
import com.google.protobuf.StringValue
import common.exception.ApiException
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import user.api.*
import user.service.entity.Session
import user.service.entity.UserEntity
import user.service.service.UserService

@GrpcService
class GrpcUserService(
    private val service: UserService
) : UserServiceGrpc.UserServiceImplBase() {

    override fun findById(request: StringValue, observer: StreamObserver<User>) {
        val entity = service.findById(request.value)
        if (!entity.isPresent) {
            observer.onError(ApiException(HttpStatus.NOT_FOUND, "user not exist"))
            return
        }
        observer.onNext(entity.get().toUser())
        observer.onCompleted()
    }

    override fun findByIds(request: Ids, observer: StreamObserver<UserList>) {
        val entity = service.findByIdIn(request.idList)
        if (entity.isEmpty()) {
            observer.onError(ApiException(HttpStatus.NOT_FOUND, "users not exist"))
            return
        }
        val builder = UserList.newBuilder()
        for (user in entity) {
            builder.addItem(
                UserList.Dto.newBuilder()
                    .setId(user.id)
                    .setUsername(user.username)
                    .setNickname(user.nickname)
                    .setAvatar(user.avatar ?: "")
                    .setEmail(user.email)
                    .build()
            )
        }
        observer.onNext(builder.build())
        observer.onCompleted()
    }

    override fun findByName(request: StringValue, observer: StreamObserver<User>) {
        val entity = service.findByUsername(request.value)
        if (!entity.isPresent) {
            observer.onError(ApiException(HttpStatus.NOT_FOUND, "user not exist"))
            return
        }
        observer.onNext(entity.get().toUser())
        observer.onCompleted()
    }

    override fun create(request: User, observer: StreamObserver<User>) {
        val entity = service.findByUsername(request.username)
        if (entity.isPresent) {
            observer.onError(ApiException(HttpStatus.BAD_REQUEST, "user already exists"))
            return
        }
        observer.onNext(
            service.save(UserEntity().apply {
                id = ObjectId.get().toString()
                username = request.username
                email = request.email
                password = request.password
            }).toUser()
        )
        observer.onCompleted()
    }

    override fun resetPassword(request: User, observer: StreamObserver<Empty>) {
        val entity = service.findByUsername(request.username)
        if (!entity.isPresent) {
            observer.onError(ApiException(HttpStatus.NOT_FOUND, "user not exists"))
            return
        }
        observer.onNext(Empty.getDefaultInstance())
        observer.onCompleted()
    }

    override fun saveSession(request: SessionAction, observer: StreamObserver<Empty>) {
        val entity = service.findById(request.userId)
        if (!entity.isPresent) {
            observer.onError(ApiException(HttpStatus.NOT_FOUND, "user not exists"))
            return
        }
        service.save(entity.get().also { user ->
            when (request.action!!) {
                SessionAction.Action.SignIn -> user.sessions.getOrPut(request.session.id) {
                    Session(
                        id = request.session.id,
                        client = request.session.client,
                        timestamp = request.session.timestamp,
                        ip = request.session.ip
                    )
                }.lastUsage = request.session.timestamp

                SessionAction.Action.Refresh -> user.sessions[request.session.id]
                    ?.let { it.lastUsage = request.session.timestamp }

                SessionAction.Action.Logout -> user.sessions.remove(request.session.id) ?: throw ApiException(
                    HttpStatus.NOT_FOUND,
                    "session not exists"
                )

                SessionAction.Action.UNRECOGNIZED -> {}
            }
        })
        observer.onNext(Empty.getDefaultInstance())
        observer.onCompleted()
    }
}