package com.base.armsupportservice.dto.group

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class AssignmentGroupRequest(
    @field:NotBlank(message = "Название группы обязательно")
    @field:Size(max = 255)
    val name: String,
    val description: String? = null,
    @field:Email(message = "Некорректный адрес почтового ящика")
    @field:Size(max = 320)
    val mailboxEmail: String? = null,
    val operatorIds: Set<UUID> = emptySet(),
)

data class GroupOperatorsRequest(
    val operatorIds: Set<UUID>,
)
