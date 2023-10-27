package doc.service.dto

import common.dto.BaseQueryDto

data class DocQueryDto(
    val title: String?,
    val delete: Boolean?
) : BaseQueryDto()