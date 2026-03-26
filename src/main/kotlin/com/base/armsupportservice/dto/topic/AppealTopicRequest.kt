package com.base.armsupportservice.dto.topic

import com.base.armsupportservice.domain.topic.AppealTopicCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class AppealTopicRequest(
    @field:NotBlank
    @field:Size(max = 64)
    @field:Pattern(regexp = "^[A-Z0-9_]+$", message = "Код должен содержать только заглавные латинские буквы, цифры и _")
    val code: String,
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:NotNull
    val category: AppealTopicCategory,
    val description: String? = null,
    val active: Boolean = true,
)
