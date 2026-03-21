package com.base.armsupportservice

import com.base.armsupportservice.security.UserPrincipal
import com.base.armsupportservice.security.UserPrincipalAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.request.RequestPostProcessor
import java.time.Instant
import java.util.UUID

object SecurityTestSupport {
    fun operator(
        userId: UUID = UUID.randomUUID(),
        username: String = "operator",
        email: String = "operator@test.local",
    ): RequestPostProcessor =
        principal(
            userId = userId,
            username = username,
            email = email,
            permissions = listOf("APPEAL_READ", "APPEAL_WRITE"),
        )

    fun admin(
        userId: UUID = UUID.randomUUID(),
        username: String = "admin",
        email: String = "admin@test.local",
    ): RequestPostProcessor =
        principal(
            userId = userId,
            username = username,
            email = email,
            permissions = listOf("APPEAL_READ", "APPEAL_WRITE", "ADMIN_ACCESS"),
        )

    fun readOnly(userId: UUID = UUID.randomUUID()): RequestPostProcessor =
        principal(
            userId = userId,
            permissions = listOf("APPEAL_READ"),
        )

    fun principal(
        userId: UUID = UUID.randomUUID(),
        username: String = "user",
        email: String = "user@test.local",
        roles: List<String> = listOf("USER"),
        permissions: List<String> = listOf("APPEAL_READ", "APPEAL_WRITE"),
        status: String = "ACTIVE",
    ): RequestPostProcessor {
        val jwt =
            Jwt
                .withTokenValue("test-token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("user_id", userId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("status", status)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .build()
        val authorities =
            roles.map { SimpleGrantedAuthority("ROLE_$it") } +
                permissions.map { SimpleGrantedAuthority(it) }
        val userPrincipal =
            UserPrincipal(
                userId = userId,
                username = username,
                email = email,
                roles = roles,
                permissions = permissions,
                status = status,
            )
        val auth = UserPrincipalAuthenticationToken(jwt, authorities, userPrincipal)
        return authentication(auth)
    }
}
