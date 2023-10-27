package doc.service.service.impl

import common.exception.ApiException
import common.utils.getRequestId
import doc.service.dto.DocDto
import doc.service.dto.DocQueryDto
import doc.service.dto.toDocDto
import doc.service.entity.DocEntity
import doc.service.service.DocNodeService
import doc.service.service.DocServiceExt
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

@Service
open class DocServiceImpl(
    private val mongo: MongoTemplate,
    private val nodeService: DocNodeService,
) : DocServiceExt {

    override fun search(queryDto: DocQueryDto): Page<DocDto> {
        val query = Query().apply {
            if (!queryDto.title.isNullOrEmpty()) {
                addCriteria(Criteria.where("title").regex(queryDto.title))
            }
            addCriteria(Criteria.where("owner.id").`is`(getRequestId()))
            addCriteria(Criteria.where("delete").`is`(queryDto.delete ?: false))
        }.with(queryDto.page).with(queryDto.sortBy)

        return PageImpl(
            mongo.find(query, DocEntity::class.java).map { it.toDocDto() },
            Pageable.unpaged(),
            mongo.count(query, DocEntity::class.java)
        )
    }

    override fun findDtoById(id: String) = mongo.findById<DocEntity>(id)?.toDocDto()

    override fun insertDto(doc: DocDto): DocDto {
        val exists =
            mongo.exists(Query.query(Criteria.where("title").`is`(doc.title)), DocEntity::class.java)
        if (exists) {
            throw ApiException(HttpStatus.BAD_REQUEST, "doc title duplicate")
        }

        return mongo.save(doc.toEntity()).toDocDto()
    }

    @Transactional
    override fun updateDto(doc: DocDto): DocDto {
        val origin: DocEntity = mongo.findById(doc.id)
            ?: throw ApiException(HttpStatus.NOT_FOUND, "doc not found")

        mongo.save(origin.apply {
            title = doc.title
            trash = doc.delete
        })

        return origin.toDocDto()
    }

    @Transactional
    override fun updateDtos(docs: List<DocDto>): List<DocDto> {
        return docs.mapNotNull {
            val origin: DocEntity = mongo.findById(it.id) ?: return@mapNotNull null
            mongo.save(origin.apply {
                title = it.title
                trash = it.delete
            })
        }.map { it.toDocDto() }
    }

    @Transactional
    override fun removeById(id: String): Boolean {
        val deleted = mongo.remove(
            Query.query(Criteria.where("id").`is`(id)), DocEntity::class.java
        ).deletedCount > 0
        if (deleted) {
            nodeService.deleteAllByDid(id)
        }
        return deleted
    }
}