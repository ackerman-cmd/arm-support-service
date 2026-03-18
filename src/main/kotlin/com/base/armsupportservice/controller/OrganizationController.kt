package com.base.armsupportservice.controller

import com.base.armsupportservice.dto.organization.OrganizationRequest
import com.base.armsupportservice.dto.organization.OrganizationResponse
import com.base.armsupportservice.service.OrganizationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Управление организациями (юридическими лицами)")
class OrganizationController(
    private val organizationService: OrganizationService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Получить список организаций с пагинацией")
    fun getAll(
        @ParameterObject @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable,
    ): Page<OrganizationResponse> = organizationService.getAll(pageable)

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Поиск организаций по названию")
    fun search(
        @RequestParam name: String,
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): Page<OrganizationResponse> = organizationService.search(name, pageable)

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Получить организацию по ID")
    fun getById(
        @PathVariable id: UUID,
    ): OrganizationResponse = organizationService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('APPEAL_WRITE')")
    @Operation(summary = "Создать организацию")
    fun create(
        @Valid @RequestBody request: OrganizationRequest,
    ): OrganizationResponse = organizationService.create(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_WRITE')")
    @Operation(summary = "Обновить организацию")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: OrganizationRequest,
    ): OrganizationResponse = organizationService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Удалить организацию (только администратор)")
    fun delete(
        @PathVariable id: UUID,
    ) = organizationService.delete(id)
}
