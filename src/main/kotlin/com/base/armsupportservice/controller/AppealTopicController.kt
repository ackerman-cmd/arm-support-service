package com.base.armsupportservice.controller

import com.base.armsupportservice.dto.topic.AppealTopicRequest
import com.base.armsupportservice.dto.topic.AppealTopicResponse
import com.base.armsupportservice.dto.topic.AppealTopicsByCategoryResponse
import com.base.armsupportservice.service.AppealTopicService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
@RequestMapping("/api/v1/appeal-topics")
@Tag(name = "Appeal Topics", description = "Справочник тематик обращений")
class AppealTopicController(
    private val topicService: AppealTopicService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Все тематики (включая неактивные) — для администратора")
    fun getAll(): List<AppealTopicResponse> = topicService.getAll()

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Только активные тематики")
    fun getAllActive(): List<AppealTopicResponse> = topicService.getAllActive()

    @GetMapping("/grouped")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Активные тематики, сгруппированные по категории — для dropdown")
    fun getGrouped(): List<AppealTopicsByCategoryResponse> = topicService.getGroupedByCategory()

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_READ')")
    @Operation(summary = "Тематика по ID")
    fun getById(
        @PathVariable id: UUID,
    ): AppealTopicResponse = topicService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Создать тематику")
    fun create(
        @Valid @RequestBody request: AppealTopicRequest,
    ): AppealTopicResponse = topicService.create(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Обновить тематику")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AppealTopicRequest,
    ): AppealTopicResponse = topicService.update(id, request)

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @Operation(summary = "Активировать / деактивировать тематику")
    fun setActive(
        @PathVariable id: UUID,
        @RequestParam active: Boolean,
    ): AppealTopicResponse = topicService.setActive(id, active)
}
