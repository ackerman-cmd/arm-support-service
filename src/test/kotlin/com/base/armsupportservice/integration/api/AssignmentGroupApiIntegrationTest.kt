package com.base.armsupportservice.integration.api

import com.base.armsupportservice.SecurityTestSupport
import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.dto.group.AssignmentGroupRequest
import com.base.armsupportservice.dto.group.GroupOperatorsRequest
import com.base.armsupportservice.integration.AbstractIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
class AssignmentGroupApiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
    }

    @Test
    fun `POST assignment-group without admin returns 403`() {
        mockMvc
            .perform(
                post("/api/v1/assignment-groups")
                    .with(SecurityTestSupport.operator())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            AssignmentGroupRequest(name = "G"),
                        ),
                    ),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `admin creates group with operators`() {
        val op = inserter.insertSyncedUser()
        val body =
            AssignmentGroupRequest(
                name = "Линия 1",
                operatorIds = setOf(op.id),
            )
        mockMvc
            .perform(
                post("/api/v1/assignment-groups")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Линия 1"))
            .andExpect(jsonPath("$.operatorCount").value(1))
    }

    @Test
    fun `GET list returns groups`() {
        inserter.insertAssignmentGroup(name = "Только список")
        mockMvc
            .perform(
                get("/api/v1/assignment-groups")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].name").value("Только список"))
    }

    @Test
    fun `duplicate name returns 409`() {
        inserter.insertAssignmentGroup(name = "Дубликат")
        mockMvc
            .perform(
                post("/api/v1/assignment-groups")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            AssignmentGroupRequest(name = "Дубликат"),
                        ),
                    ),
            ).andExpect(status().isConflict)
    }

    @Test
    fun `add operators via subresource`() {
        val g = inserter.insertAssignmentGroup(name = "Расширение")
        val op = inserter.insertSyncedUser()
        mockMvc
            .perform(
                post("/api/v1/assignment-groups/${g.id}/operators")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            GroupOperatorsRequest(operatorIds = setOf(op.id)),
                        ),
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.operatorCount").value(1))
    }

    @Test
    fun `remove operator`() {
        val op = inserter.insertSyncedUser()
        val g =
            inserter.insertAssignmentGroup(
                name = "Состав",
                operatorIds = mutableSetOf(op.id),
            )
        mockMvc
            .perform(
                delete("/api/v1/assignment-groups/${g.id}/operators/${op.id}")
                    .with(SecurityTestSupport.admin()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.operatorCount").value(0))
    }

    @Test
    fun `PUT updates group`() {
        val g = inserter.insertAssignmentGroup(name = "Старое")
        val body =
            AssignmentGroupRequest(
                name = "Новое",
                description = "описание",
            )
        mockMvc
            .perform(
                put("/api/v1/assignment-groups/${g.id}")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Новое"))
    }

    @Test
    fun `unknown operator on create returns 404`() {
        val body =
            AssignmentGroupRequest(
                name = "Без людей",
                operatorIds = setOf(UUID.randomUUID()),
            )
        mockMvc
            .perform(
                post("/api/v1/assignment-groups")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isNotFound)
    }
}
