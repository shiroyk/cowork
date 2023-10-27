package doc.service.controller

import common.exception.ApiException
import doc.service.dto.DocDto
import doc.service.dto.DocQueryDto
import doc.service.service.DocService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class DocController(
    private val service: DocService
) {
    @GetMapping
    fun search(queryDto: DocQueryDto, response: HttpServletResponse): List<DocDto> {
        return service.search(queryDto).run {
            response.setHeader("X-Total-Count", totalElements.toString())
            toList()
        }
    }

    @PostMapping
    fun post(@Valid @RequestBody doc: DocDto) = service.insertDto(doc)

    @GetMapping("/{id}")
    fun get(@PathVariable id: String) = service.findDtoById(id)
        ?: throw ApiException(HttpStatus.NOT_FOUND, "doc not found")

    @PutMapping
    fun put(@Valid @RequestBody doc: DocDto) = service.updateDto(doc)

    @PatchMapping
    fun patch(@Valid @RequestBody docs: List<DocDto>) = service.updateDtos(docs)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = service.deleteById(id)

}