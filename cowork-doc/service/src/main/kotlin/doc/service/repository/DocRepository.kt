package doc.service.repository

import doc.service.entity.DocEntity
import doc.service.entity.DocNode
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DocRepository : MongoRepository<DocEntity, String>

@Repository
interface DocNodeRepository : MongoRepository<DocNode, String> {
    fun deleteAllByDid(did: String)
}