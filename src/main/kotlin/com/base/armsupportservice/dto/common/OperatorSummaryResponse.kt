package com.base.armsupportservice.dto.common

import java.util.UUID

data class OperatorSummaryResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { username },
)
