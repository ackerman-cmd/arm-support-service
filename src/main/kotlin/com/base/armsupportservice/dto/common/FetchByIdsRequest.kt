package com.base.armsupportservice.dto.common

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.util.UUID

data class FetchByIdsRequest(
    @field:NotEmpty(message = "Список ids не может быть пустым")
    @field:Size(max = 200, message = "Нельзя запросить более 200 записей за раз")
    val ids: Set<UUID>,
)
