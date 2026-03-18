package com.base.armsupportservice.dto.appeal

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class AssignOperatorRequest(
    @field:NotNull(message = "ID оператора обязателен")
    val operatorId: UUID,
)
