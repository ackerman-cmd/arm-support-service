package com.base.armsupportservice.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPrincipalJwtConverter : Converter<Jwt, AbstractAuthenticationToken> {
    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val roles = jwt.getClaimAsStringList("roles").orEmpty()
        val permissions = jwt.getClaimAsStringList("permissions").orEmpty()

        val authorities =
            roles.map { SimpleGrantedAuthority("ROLE_$it") } +
                permissions.map { SimpleGrantedAuthority(it) }

        val principal =
            UserPrincipal(
                userId = UUID.fromString(jwt.getClaimAsString("user_id")),
                username = jwt.getClaimAsString("username"),
                email = jwt.getClaimAsString("email"),
                roles = roles,
                permissions = permissions,
                status = jwt.getClaimAsString("status"),
            )

        return UserPrincipalAuthenticationToken(jwt, authorities, principal)
    }
}
