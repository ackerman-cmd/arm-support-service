package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealAction
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.security.UserPrincipal
import com.base.armsupportservice.service.PermissionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionServiceTest {
    private lateinit var service: PermissionService

    @BeforeEach
    fun setUp() {
        service = PermissionService()
    }

    // -------- helpers --------

    private fun appeal(
        status: AppealStatus = AppealStatus.PENDING_PROCESSING,
        assignedOperatorId: UUID? = null,
    ) = Appeal(
        subject = "Test",
        channel = AppealChannel.EMAIL,
        direction = AppealDirection.INBOUND,
        status = status,
        priority = AppealPriority.MEDIUM,
        createdById = UUID.randomUUID(),
        assignedOperatorId = assignedOperatorId,
    )

    private fun principal(vararg permissions: String) =
        UserPrincipal(
            userId = UUID.randomUUID(),
            username = "test",
            email = "test@test.local",
            roles = listOf("ROLE_USER"),
            permissions = permissions.toList(),
            status = "ACTIVE",
        )

    // -------- no permissions → empty --------

    @Test
    fun `returns empty set when user has no relevant permissions`() {
        val actions = service.availableActions(appeal(), principal("SOME_OTHER"))
        assertTrue(actions.isEmpty())
    }

    // -------- read-only --------

    @Test
    fun `APPEAL_READ user can only VIEW_MESSAGES`() {
        val actions = service.availableActions(appeal(), principal("APPEAL_READ"))
        assertTrue(AppealAction.VIEW_MESSAGES in actions)
        assertFalse(AppealAction.TAKE_INTO_WORK in actions)
        assertFalse(AppealAction.CLOSE in actions)
    }

    // -------- APPEAL_WRITE + statuses --------

    @Test
    fun `writer can TAKE_INTO_WORK on PENDING_PROCESSING`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.PENDING_PROCESSING),
                principal("APPEAL_WRITE"),
            )
        assertContains(actions, AppealAction.TAKE_INTO_WORK)
    }

    @Test
    fun `writer can TAKE_INTO_WORK on WAITING_CLIENT_RESPONSE`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.WAITING_CLIENT_RESPONSE),
                principal("APPEAL_WRITE"),
            )
        assertContains(actions, AppealAction.TAKE_INTO_WORK)
    }

    @Test
    fun `writer cannot TAKE_INTO_WORK on IN_PROGRESS`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.IN_PROGRESS),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.TAKE_INTO_WORK in actions)
    }

    @Test
    fun `writer can REPLY_TO_CLIENT on IN_PROGRESS`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.IN_PROGRESS),
                principal("APPEAL_WRITE"),
            )
        assertContains(actions, AppealAction.REPLY_TO_CLIENT)
    }

    @Test
    fun `writer cannot REPLY_TO_CLIENT on PENDING_PROCESSING`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.PENDING_PROCESSING),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.REPLY_TO_CLIENT in actions)
    }

    @Test
    fun `writer cannot REPLY_TO_CLIENT on WAITING_CLIENT_RESPONSE`() {
        // После отправки письма оператором статус становится WAITING_CLIENT_RESPONSE,
        // и повторный ответ недоступен пока оператор не возьмёт обращение в работу заново
        val actions =
            service.availableActions(
                appeal(AppealStatus.WAITING_CLIENT_RESPONSE),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.REPLY_TO_CLIENT in actions)
    }

    @Test
    fun `disabled REPLY_TO_CLIENT on WAITING_CLIENT_RESPONSE hints to re-take into work`() {
        val result =
            service.evaluateActions(
                appeal(AppealStatus.WAITING_CLIENT_RESPONSE),
                principal("APPEAL_WRITE"),
            )
        val item = result.first { it.action == AppealAction.REPLY_TO_CLIENT }
        assertFalse(item.enabled)
        assertTrue(item.hint.contains("работ", ignoreCase = true))
    }

    @Test
    fun `writer cannot MARK_AS_SPAM on already SPAM`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.SPAM),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.MARK_AS_SPAM in actions)
    }

    @Test
    fun `writer cannot CLOSE on already CLOSED`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.CLOSED),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.CLOSE in actions)
    }

    @Test
    fun `writer cannot EDIT on CLOSED appeal`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.CLOSED),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.EDIT in actions)
    }

    @Test
    fun `writer cannot CHANGE_STATUS on CLOSED appeal`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.CLOSED),
                principal("APPEAL_WRITE"),
            )
        assertFalse(AppealAction.CHANGE_STATUS in actions)
    }

    // -------- admin --------

    @Test
    fun `admin has DELETE action`() {
        val actions = service.availableActions(appeal(), principal("ADMIN_ACCESS"))
        assertContains(actions, AppealAction.DELETE)
    }

    @Test
    fun `APPEAL_WRITE without ADMIN_ACCESS has no DELETE`() {
        val actions = service.availableActions(appeal(), principal("APPEAL_WRITE"))
        assertFalse(AppealAction.DELETE in actions)
    }

    @Test
    fun `admin with APPEAL_WRITE has full action set on active appeal`() {
        val actions =
            service.availableActions(
                appeal(AppealStatus.IN_PROGRESS),
                principal("APPEAL_READ", "APPEAL_WRITE", "ADMIN_ACCESS"),
            )
        assertContains(actions, AppealAction.VIEW_MESSAGES)
        assertContains(actions, AppealAction.ASSIGN_OPERATOR)
        assertContains(actions, AppealAction.REPLY_TO_CLIENT)
        assertContains(actions, AppealAction.CHANGE_STATUS)
        assertContains(actions, AppealAction.CLOSE)
        assertContains(actions, AppealAction.EDIT)
        assertContains(actions, AppealAction.DELETE)
    }

    // -------- evaluateActions: всегда возвращает все действия --------

    @Test
    fun `evaluateActions returns entry for every AppealAction`() {
        val result = service.evaluateActions(appeal(), principal("APPEAL_WRITE"))
        val returnedActions = result.map { it.action }.toSet()
        AppealAction.entries.forEach { action ->
            assertTrue(action in returnedActions, "Missing action in evaluateActions result: $action")
        }
    }

    @Test
    fun `every ActionItem has non-blank hint and description`() {
        val result = service.evaluateActions(appeal(AppealStatus.IN_PROGRESS), principal("APPEAL_WRITE"))
        result.forEach { item ->
            assertTrue(item.hint.isNotBlank(), "Blank hint for action ${item.action}")
            assertTrue(item.description.isNotBlank(), "Blank description for action ${item.action}")
        }
    }

    @Test
    fun `enabled actions have hint about what will happen`() {
        val result = service.evaluateActions(appeal(AppealStatus.PENDING_PROCESSING), principal("APPEAL_WRITE"))
        val takeIntoWork = result.first { it.action == AppealAction.TAKE_INTO_WORK }
        assertTrue(takeIntoWork.enabled)
        assertTrue(takeIntoWork.hint.isNotBlank())
    }

    @Test
    fun `disabled TAKE_INTO_WORK on IN_PROGRESS has explanatory hint`() {
        val result = service.evaluateActions(appeal(AppealStatus.IN_PROGRESS), principal("APPEAL_WRITE"))
        val item = result.first { it.action == AppealAction.TAKE_INTO_WORK }
        assertFalse(item.enabled)
        assertTrue(item.hint.contains("работ", ignoreCase = true))
    }

    @Test
    fun `disabled REPLY_TO_CLIENT on PENDING_PROCESSING hints to take into work first`() {
        val result = service.evaluateActions(appeal(AppealStatus.PENDING_PROCESSING), principal("APPEAL_WRITE"))
        val item = result.first { it.action == AppealAction.REPLY_TO_CLIENT }
        assertFalse(item.enabled)
        assertTrue(item.hint.contains("работ", ignoreCase = true))
    }

    @Test
    fun `disabled DELETE for non-admin has hint about ADMIN_ACCESS`() {
        val result = service.evaluateActions(appeal(), principal("APPEAL_WRITE"))
        val item = result.first { it.action == AppealAction.DELETE }
        assertFalse(item.enabled)
        assertTrue(item.hint.contains("ADMIN_ACCESS"))
    }

    @Test
    fun `disabled JOIN_WORK without group assignment has explanatory hint`() {
        val result = service.evaluateActions(appeal(), principal("APPEAL_WRITE"))
        val item = result.first { it.action == AppealAction.JOIN_WORK }
        assertFalse(item.enabled)
        assertTrue(item.hint.contains("групп", ignoreCase = true))
    }

    @Test
    fun `no permissions — all actions disabled with permission hints`() {
        val result = service.evaluateActions(appeal(), principal("SOME_ROLE"))
        assertTrue(result.all { !it.enabled })
        result.forEach { item ->
            assertTrue(item.hint.isNotBlank(), "Expected non-blank hint for ${item.action}")
        }
    }
}
