package com.base.armsupportservice.service

import com.base.armsupportservice.domain.organization.Organization
import com.base.armsupportservice.dto.organization.OrganizationRequest
import com.base.armsupportservice.dto.organization.OrganizationResponse
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.OrganizationNotFoundException
import com.base.armsupportservice.repository.OrganizationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
) {
    fun getById(id: UUID): OrganizationResponse =
        organizationRepository
            .findById(id)
            .map(OrganizationResponse::from)
            .orElseThrow { OrganizationNotFoundException(id) }

    fun getAll(pageable: Pageable): Page<OrganizationResponse> = organizationRepository.findAll(pageable).map(OrganizationResponse::from)

    fun search(
        name: String,
        pageable: Pageable,
    ): Page<OrganizationResponse> = organizationRepository.findByNameContainingIgnoreCase(name, pageable).map(OrganizationResponse::from)

    @Transactional
    fun create(request: OrganizationRequest): OrganizationResponse {
        if (organizationRepository.existsByInn(request.inn)) {
            throw DuplicateResourceException("Организация с ИНН ${request.inn} уже существует")
        }
        val org =
            Organization(
                name = request.name,
                inn = request.inn,
                kpp = request.kpp,
                ogrn = request.ogrn,
                legalAddress = request.legalAddress,
                contactEmail = request.contactEmail,
                contactPhone = request.contactPhone,
                description = request.description,
            )
        return OrganizationResponse.from(organizationRepository.save(org))
    }

    @Transactional
    fun update(
        id: UUID,
        request: OrganizationRequest,
    ): OrganizationResponse {
        val org = organizationRepository.findById(id).orElseThrow { OrganizationNotFoundException(id) }
        if (organizationRepository.existsByInnAndIdNot(request.inn, id)) {
            throw DuplicateResourceException("Организация с ИНН ${request.inn} уже существует")
        }
        org.name = request.name
        org.inn = request.inn
        org.kpp = request.kpp
        org.ogrn = request.ogrn
        org.legalAddress = request.legalAddress
        org.contactEmail = request.contactEmail
        org.contactPhone = request.contactPhone
        org.description = request.description
        return OrganizationResponse.from(organizationRepository.save(org))
    }

    @Transactional
    fun delete(id: UUID) {
        if (!organizationRepository.existsById(id)) throw OrganizationNotFoundException(id)
        organizationRepository.deleteById(id)
    }
}
