package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealStatus
import jakarta.validation.constraints.NotNull

data class ChangeStatusRequest(
    @field:NotNull(message = "Новый статус обязателен")
    val status: AppealStatus,
)
