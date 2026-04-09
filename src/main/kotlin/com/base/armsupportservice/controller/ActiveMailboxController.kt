package com.base.armsupportservice.controller

import com.base.armsupportservice.dto.mailbox.ActiveMailboxResponse
import com.base.armsupportservice.security.UserPrincipal
import com.base.armsupportservice.service.ActiveMailboxService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/mailboxes")
@Tag(name = "Mailboxes", description = "Получение активных почтовых ящиков по пользователю и группе назначения")
class ActiveMailboxController(
    private val activeMailboxService: ActiveMailboxService,
) {
    @GetMapping("/me")
    @Operation(summary = "Активные почтовые ящики текущего пользователя")
    fun getMyMailboxes(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<ActiveMailboxResponse> = activeMailboxService.getByUser(principal.username)

    @GetMapping("/assignment-groups/{groupId}")
    @Operation(summary = "Активный почтовый ящик группы назначения")
    fun getAssignmentGroupMailbox(
        @PathVariable groupId: UUID,
    ): ActiveMailboxResponse = activeMailboxService.getByAssignmentGroup(groupId)
}
