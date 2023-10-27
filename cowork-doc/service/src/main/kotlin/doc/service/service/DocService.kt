package doc.service.service

import doc.service.dto.DocDto
import doc.service.dto.DocQueryDto
import doc.service.repository.DocNodeRepository
import doc.service.repository.DocRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page

@Primary
interface DocService : DocRepository, DocServiceExt

@Primary
interface DocNodeService : DocNodeRepository

interface DocServiceExt {
    fun search(queryDto: DocQueryDto): Page<DocDto>

    fun findDtoById(id: String): DocDto?

    fun insertDto(doc: DocDto): DocDto

    fun updateDto(doc: DocDto): DocDto

    fun updateDtos(docs: List<DocDto>): List<DocDto>

    fun removeById(id: String): Boolean
}