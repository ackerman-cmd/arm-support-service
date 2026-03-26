package com.base.armsupportservice.controller

import com.base.armsupportservice.dto.common.FetchByIdsRequest
import com.base.armsupportservice.dto.group.GroupOperatorsRequest
import com.base.armsupportservice.dto.group.SkillGroupRequest
import com.base.armsupportservice.dto.group.SkillGroupResponse
import com.base.armsupportservice.service.SkillGroupService
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/skill-groups")
@Tag(name = "Skill Groups", description = "Скилл-группы операторов")
class SkillGroupController(
    private val skillGroupService: SkillGroupService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Список скилл-групп")
    fun getAll(
        @ParameterObject @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable,
    ): Page<SkillGroupResponse> = skillGroupService.getAll(pageable)

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Получить скилл-группу по ID")
    fun getById(
        @PathVariable id: UUID,
    ): SkillGroupResponse = skillGroupService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Создать скилл-группу")
    fun create(
        @Valid @RequestBody request: SkillGroupRequest,
    ): SkillGroupResponse = skillGroupService.create(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Обновить скилл-группу")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: SkillGroupRequest,
    ): SkillGroupResponse = skillGroupService.update(id, request)

    @PostMapping("/{id}/operators")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Добавить операторов в скилл-группу")
    fun addOperators(
        @PathVariable id: UUID,
        @RequestBody request: GroupOperatorsRequest,
    ): SkillGroupResponse = skillGroupService.addOperators(id, request.operatorIds)

    @DeleteMapping("/{id}/operators/{operatorId}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Удалить оператора из скилл-группы")
    fun removeOperator(
        @PathVariable id: UUID,
        @PathVariable operatorId: UUID,
    ): SkillGroupResponse = skillGroupService.removeOperator(id, operatorId)

    @PostMapping("/fetch")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Пакетная загрузка скилл-групп по списку ID")
    fun fetchByIds(
        @Valid @RequestBody request: FetchByIdsRequest,
    ): List<SkillGroupResponse> = skillGroupService.fetchByIds(request.ids)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Удалить скилл-группу")
    fun delete(
        @PathVariable id: UUID,
    ) = skillGroupService.delete(id)
}
