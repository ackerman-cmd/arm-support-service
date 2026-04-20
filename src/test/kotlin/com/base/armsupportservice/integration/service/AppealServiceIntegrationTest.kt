package com.base.armsupportservice.integration.service

import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.dto.appeal.AppealMessageRequest
import com.base.armsupportservice.dto.appeal.AppealRequest
import com.base.armsupportservice.dto.appeal.AssignOperatorRequest
import com.base.armsupportservice.integration.AbstractIntegrationTest
import com.base.armsupportservice.security.UserPrincipal
import com.base.armsupportservice.service.AppealService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppealServiceIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var appealService: AppealService

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    private lateinit var operatorId: UUID
    private lateinit var principal: UserPrincipal

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
        operatorId = UUID.randomUUID()
        inserter.insertSyncedUser(id = operatorId)
        principal =
            UserPrincipal(
                userId = operatorId,
                username = "op",
                email = "op@test.local",
                roles = listOf("USER"),
                permissions = listOf("APPEAL_WRITE"),
                status = "ACTIVE",
            )
    }

    @Test
    fun `assign rejects inactive synced user`() {
        val activeAppeal =
            inserter.insertAppeal(
                createdById = operatorId,
                status = AppealStatus.IN_PROGRESS,
                assignedOperatorId = operatorId,
            )
        val inactiveId = UUID.randomUUID()
        inserter.insertSyncedUser(
            id = inactiveId,
            username = "inact",
            email = "in@test.local",
            status = UserStatus.INACTIVE,
        )

        assertFailsWith<IllegalStateException> {
            appealService.assign(activeAppeal.id, AssignOperatorRequest(operatorId = inactiveId))
        }
    }

    @Test
    fun `receiveClientMessage deduplicates by external id in database`() {
        val appeal =
            inserter.insertAppeal(
                createdById = operatorId,
                status = AppealStatus.WAITING_CLIENT_RESPONSE,
            )
        val req =
            AppealMessageRequest(
                content = "первое",
                channel = AppealChannel.EMAIL,
                externalMessageId = "dedup-key",
            )
        appealService.receiveClientMessage(appeal.id, req)
        val second =
            AppealMessageRequest(
                content = "дубликат",
                channel = AppealChannel.EMAIL,
                externalMessageId = "dedup-key",
            )
        val again = appealService.receiveClientMessage(appeal.id, second)
        assertEquals("первое", again.content)
    }

    @Test
    fun `full create take close flow`() {
        val created =
            appealService.create(
                AppealRequest(
                    subject = "Поток",
                    channel = AppealChannel.CHAT,
                    direction = AppealDirection.INBOUND,
                ),
                principal,
            )
        val taken = appealService.takeIntoWork(created.id, principal)
        assertEquals(AppealStatus.IN_PROGRESS, taken.status)
        val closed = appealService.close(created.id)
        assertEquals(AppealStatus.CLOSED, closed.status)
    }
}
