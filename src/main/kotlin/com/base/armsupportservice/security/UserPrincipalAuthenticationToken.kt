package com.base.armsupportservice.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class UserPrincipalAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>,
    private val userPrincipal: UserPrincipal,
) : JwtAuthenticationToken(jwt, authorities, userPrincipal.username) {
    override fun getPrincipal(): UserPrincipal = userPrincipal
}
