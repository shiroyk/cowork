package common.dto

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

open class BaseQueryDto {
    private val sort: List<String>? = null
    private val asc: Boolean = true
    private val limit: Int = 10
    private val offset: Int = 0

    val page: PageRequest
        get() = PageRequest.of(offset, limit)
    val sortBy: Sort
        get() = sort?.let { Sort.by(if (asc) Sort.Direction.ASC else Sort.Direction.DESC, *sort.toTypedArray()) }
            ?: Sort.by("createdAt")
}
