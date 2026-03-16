package com.base.armsupportservice.security

import java.util.UUID

data class UserPrincipal(
    val userId: UUID,
    val username: String,
    val email: String,
    val roles: List<String>,
    val permissions: List<String>,
    val status: String,
)
