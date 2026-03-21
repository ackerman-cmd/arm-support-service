package com.base.armsupportservice.unit.security

import com.base.armsupportservice.security.UserPrincipalJwtConverter
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserPrincipalJwtConverterTest {
    private val converter = UserPrincipalJwtConverter()

    @Test
    fun `convert maps claims to principal and authorities`() {
        val userId = UUID.randomUUID()
        val jwt =
            Jwt
                .withTokenValue("t")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("user_id", userId.toString())
                .claim("username", "alice")
                .claim("email", "a@b.c")
                .claim("status", "ACTIVE")
                .claim("roles", listOf("OPERATOR"))
                .claim("permissions", listOf("APPEAL_READ"))
                .build()

        val auth = converter.convert(jwt)

        val principal = auth.principal as com.base.armsupportservice.security.UserPrincipal
        assertEquals(userId, principal.userId)
        assertEquals("alice", principal.username)
        assertEquals("a@b.c", principal.email)
        assertEquals(listOf("OPERATOR"), principal.roles)
        assertEquals(listOf("APPEAL_READ"), principal.permissions)
        assertTrue(auth.authorities.any { it.authority == "ROLE_OPERATOR" })
        assertTrue(auth.authorities.any { it.authority == "APPEAL_READ" })
    }

    @Test
    fun `convert handles missing roles and permissions as empty`() {
        val userId = UUID.randomUUID()
        val jwt =
            Jwt
                .withTokenValue("t")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("user_id", userId.toString())
                .claim("username", "bob")
                .claim("email", "b@b.c")
                .claim("status", "ACTIVE")
                .build()

        val auth = converter.convert(jwt)
        val principal = auth.principal as com.base.armsupportservice.security.UserPrincipal
        assertEquals(emptyList<String>(), principal.roles)
        assertEquals(emptyList<String>(), principal.permissions)
    }
}
