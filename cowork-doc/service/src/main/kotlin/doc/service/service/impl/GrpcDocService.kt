package doc.service.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.StringValue
import doc.api.DocNodes
import doc.api.DocServiceGrpc
import doc.api.VerifyRequest
import doc.api.VerifyResponse
import doc.service.entity.DocNode
import doc.service.service.DocService
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.bson.types.Binary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

@GrpcService
class GrpcDocService(
    private val service: DocService,
    private val mongo: MongoTemplate
) : DocServiceGrpc.DocServiceImplBase() {

    override fun verifyPermission(request: VerifyRequest, observer: StreamObserver<VerifyResponse>) {
        val entity = service.findById(request.did)
        if (entity.isPresent) {
            observer.onNext(
                VerifyResponse.newBuilder()
                    .setOk(entity.get().ownerId == request.uid)
                    .build()
            )
        } else {
            observer.onNext(
                VerifyResponse.newBuilder()
                    .setOk(false)
                    .setMsg("doc not exists")
                    .build()
            )
        }
        observer.onCompleted()
    }

    override fun findNodesByDid(request: StringValue, observer: StreamObserver<DocNodes>) {
        val builder = DocNodes.newBuilder()
        val nodes = mongo.find<org.bson.Document>(Query().apply {
            fields().include("data")
            addCriteria(Criteria.where("did").`is`(request.value))
        }, DocNode::class.java.getAnnotation(Document::class.java).value)

        for (node in nodes) {
            builder.addNodes(ByteString.copyFrom(node.get("data", Binary::class.java).data))
        }
        observer.onNext(builder.build())
        observer.onCompleted()
    }
}