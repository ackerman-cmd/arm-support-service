package com.base.armsupportservice.dto.organization

import com.base.armsupportservice.domain.organization.Organization
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class OrganizationResponse(
    val id: UUID,
    val name: String,
    val inn: String,
    val kpp: String?,
    val ogrn: String?,
    val legalAddress: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val description: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(org: Organization) =
            OrganizationResponse(
                id = org.id,
                name = org.name,
                inn = org.inn,
                kpp = org.kpp,
                ogrn = org.ogrn,
                legalAddress = org.legalAddress,
                contactEmail = org.contactEmail,
                contactPhone = org.contactPhone,
                description = org.description,
                createdAt = org.createdAt,
                updatedAt = org.updatedAt,
            )
    }
}

data class OrganizationSummaryResponse(
    val id: UUID,
    val name: String,
    val inn: String,
) {
    companion object {
        fun from(org: Organization) =
            OrganizationSummaryResponse(
                id = org.id,
                name = org.name,
                inn = org.inn,
            )
    }
}
