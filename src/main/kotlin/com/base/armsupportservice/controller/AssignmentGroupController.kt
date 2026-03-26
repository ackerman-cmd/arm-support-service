package com.base.armsupportservice.controller

import com.base.armsupportservice.dto.common.FetchByIdsRequest
import com.base.armsupportservice.dto.group.AssignmentGroupRequest
import com.base.armsupportservice.dto.group.AssignmentGroupResponse
import com.base.armsupportservice.dto.group.GroupOperatorsRequest
import com.base.armsupportservice.service.AssignmentGroupService
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
@RequestMapping("/api/v1/assignment-groups")
@Tag(name = "Assignment Groups", description = "Группы назначения операторов")
class AssignmentGroupController(
    private val assignmentGroupService: AssignmentGroupService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Список групп назначения")
    fun getAll(
        @ParameterObject @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable,
    ): Page<AssignmentGroupResponse> = assignmentGroupService.getAll(pageable)

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Получить группу назначения по ID")
    fun getById(
        @PathVariable id: UUID,
    ): AssignmentGroupResponse = assignmentGroupService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Создать группу назначения")
    fun create(
        @Valid @RequestBody request: AssignmentGroupRequest,
    ): AssignmentGroupResponse = assignmentGroupService.create(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Обновить группу назначения")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignmentGroupRequest,
    ): AssignmentGroupResponse = assignmentGroupService.update(id, request)

    @PostMapping("/{id}/operators")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Добавить операторов в группу")
    fun addOperators(
        @PathVariable id: UUID,
        @RequestBody request: GroupOperatorsRequest,
    ): AssignmentGroupResponse = assignmentGroupService.addOperators(id, request.operatorIds)

    @DeleteMapping("/{id}/operators/{operatorId}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Удалить оператора из группы")
    fun removeOperator(
        @PathVariable id: UUID,
        @PathVariable operatorId: UUID,
    ): AssignmentGroupResponse = assignmentGroupService.removeOperator(id, operatorId)

    @PostMapping("/fetch")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Пакетная загрузка групп назначения по списку ID")
    fun fetchByIds(
        @Valid @RequestBody request: FetchByIdsRequest,
    ): List<AssignmentGroupResponse> = assignmentGroupService.fetchByIds(request.ids)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Удалить группу назначения")
    fun delete(
        @PathVariable id: UUID,
    ) = assignmentGroupService.delete(id)
}
