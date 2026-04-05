package com.base.armsupportservice.dto.group

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class SkillGroupRequest(
    @field:NotBlank(message = "Название скилл-группы обязательно")
    @field:Size(max = 255)
    val name: String,
    val description: String? = null,
    @field:Email(message = "Некорректный адрес почтового ящика")
    @field:Size(max = 320)
    val mailboxEmail: String? = null,
    val skills: Set<String> = emptySet(),
    val operatorIds: Set<UUID> = emptySet(),
)
