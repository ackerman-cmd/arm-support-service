package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.organization.Organization
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganizationRepository : JpaRepository<Organization, UUID> {
    fun existsByInn(inn: String): Boolean

    fun existsByInnAndIdNot(
        inn: String,
        id: UUID,
    ): Boolean

    fun findByNameContainingIgnoreCase(
        name: String,
        pageable: Pageable,
    ): Page<Organization>
}
