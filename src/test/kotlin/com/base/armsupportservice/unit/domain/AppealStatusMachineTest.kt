package com.base.armsupportservice.unit.domain

import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.AppealStatusMachine
import com.base.armsupportservice.exception.InvalidStatusTransitionException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class AppealStatusMachineTest {
    @Test
    fun `initialStatus INBOUND is PENDING_PROCESSING`() {
        assertEquals(AppealStatus.PENDING_PROCESSING, AppealStatusMachine.initialStatus(AppealDirection.INBOUND))
    }

    @Test
    fun `initialStatus OUTBOUND is WAITING_CLIENT_RESPONSE`() {
        assertEquals(AppealStatus.WAITING_CLIENT_RESPONSE, AppealStatusMachine.initialStatus(AppealDirection.OUTBOUND))
    }

    @Test
    fun `validate allows PENDING_PROCESSING to IN_PROGRESS`() {
        AppealStatusMachine.validate(AppealStatus.PENDING_PROCESSING, AppealStatus.IN_PROGRESS)
    }

    @Test
    fun `validate rejects PENDING_PROCESSING to WAITING_CLIENT_RESPONSE`() {
        assertThrows<InvalidStatusTransitionException> {
            AppealStatusMachine.validate(AppealStatus.PENDING_PROCESSING, AppealStatus.WAITING_CLIENT_RESPONSE)
        }
    }

    @Test
    fun `validate rejects CLOSED to any`() {
        assertThrows<InvalidStatusTransitionException> {
            AppealStatusMachine.validate(AppealStatus.CLOSED, AppealStatus.IN_PROGRESS)
        }
    }

    @Test
    fun `afterOperatorReply moves IN_PROGRESS to WAITING_CLIENT_RESPONSE`() {
        assertEquals(
            AppealStatus.WAITING_CLIENT_RESPONSE,
            AppealStatusMachine.afterOperatorReply(AppealStatus.IN_PROGRESS),
        )
    }

    @Test
    fun `afterClientReply moves WAITING_CLIENT_RESPONSE to IN_PROGRESS`() {
        assertEquals(
            AppealStatus.IN_PROGRESS,
            AppealStatusMachine.afterClientReply(AppealStatus.WAITING_CLIENT_RESPONSE),
        )
    }
}
