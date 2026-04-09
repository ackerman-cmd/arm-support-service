package com.base.armsupportservice.integration.api

import com.base.armsupportservice.SecurityTestSupport
import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.integration.AbstractIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
class ActiveMailboxApiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
    }

    // ─── GET /mailboxes/me ────────────────────────────────────────────────────

    @Test
    fun `GET me returns assignment mailbox when user is member`() {
        val username = "op_test_1"
        val user = inserter.insertSyncedUser(username = username)
        inserter.insertAssignmentGroup(
            name = "Линия 1",
            mailboxEmail = "line1@system-alerts.ru",
            operatorIds = mutableSetOf(user.id),
        )

        mockMvc
            .perform(
                get("/api/v1/mailboxes/me")
                    .with(SecurityTestSupport.principal(userId = user.id, username = username)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("line1@system-alerts.ru"))
            .andExpect(jsonPath("$[0].groupType").value("ASSIGNMENT"))
            .andExpect(jsonPath("$[0].groupName").value("Линия 1"))
    }

    @Test
    fun `GET me returns skill mailbox when user is member`() {
        val username = "op_test_2"
        val user = inserter.insertSyncedUser(username = username)
        inserter.insertSkillGroup(
            name = "Скилл ДБО",
            mailboxEmail = "skill-dbo@system-alerts.ru",
            operatorIds = mutableSetOf(user.id),
        )

        mockMvc
            .perform(
                get("/api/v1/mailboxes/me")
                    .with(SecurityTestSupport.principal(userId = user.id, username = username)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("skill-dbo@system-alerts.ru"))
            .andExpect(jsonPath("$[0].groupType").value("SKILL"))
    }

    @Test
    fun `GET me returns both group types combined`() {
        val username = "op_test_3"
        val user = inserter.insertSyncedUser(username = username)
        inserter.insertAssignmentGroup(
            name = "Назначение",
            mailboxEmail = "assign@system-alerts.ru",
            operatorIds = mutableSetOf(user.id),
        )
        inserter.insertSkillGroup(
            name = "Скилл",
            mailboxEmail = "skill@system-alerts.ru",
            operatorIds = mutableSetOf(user.id),
        )

        mockMvc
            .perform(
                get("/api/v1/mailboxes/me")
                    .with(SecurityTestSupport.principal(userId = user.id, username = username)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `GET me returns empty when user has no groups`() {
        val username = "op_test_4"
        val user = inserter.insertSyncedUser(username = username)

        mockMvc
            .perform(
                get("/api/v1/mailboxes/me")
                    .with(SecurityTestSupport.principal(userId = user.id, username = username)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `GET me returns empty when username not found in synced users`() {
        mockMvc
            .perform(
                get("/api/v1/mailboxes/me")
                    .with(SecurityTestSupport.principal(username = "nonexistent_user")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `GET me skips groups without configured mailbox`() {
        val username = "op_test_5"
        val user = inserter.insertSyncedUser(username = username)
        inserter.insertAssignmentGroup(
            name = "Без ящика",
            mailboxEmail = null,
            operatorIds = mutableSetOf(user.id),
        )
        inserter.insertAssignmentGroup(
            name = "С ящиком",
            mailboxEmail = "has-mailbox@system-alerts.ru",
            operatorIds = mutableSetOf(user.id),
        )

        mockMvc
            .perform(
                get("/api/v1/mailboxes/me")
                    .with(SecurityTestSupport.principal(userId = user.id, username = username)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("has-mailbox@system-alerts.ru"))
    }

    @Test
    fun `GET me requires authentication`() {
        mockMvc
            .perform(get("/api/v1/mailboxes/me"))
            .andExpect(status().isUnauthorized)
    }

    // ─── GET /mailboxes/assignment-groups/{groupId} ───────────────────────────

    @Test
    fun `GET assignment-groups mailbox returns configured email`() {
        val group =
            inserter.insertAssignmentGroup(
                name = "Карты",
                mailboxEmail = "cards@system-alerts.ru",
            )

        mockMvc
            .perform(
                get("/api/v1/mailboxes/assignment-groups/${group.id}")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("cards@system-alerts.ru"))
            .andExpect(jsonPath("$.groupType").value("ASSIGNMENT"))
            .andExpect(jsonPath("$.groupName").value("Карты"))
            .andExpect(jsonPath("$.groupId").value(group.id.toString()))
    }

    @Test
    fun `GET assignment-groups mailbox returns 404 when group missing`() {
        mockMvc
            .perform(
                get("/api/v1/mailboxes/assignment-groups/${UUID.randomUUID()}")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `GET assignment-groups mailbox returns 404 when mailbox not configured`() {
        val group = inserter.insertAssignmentGroup(name = "Без ящика", mailboxEmail = null)

        mockMvc
            .perform(
                get("/api/v1/mailboxes/assignment-groups/${group.id}")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isNotFound)
    }
}
