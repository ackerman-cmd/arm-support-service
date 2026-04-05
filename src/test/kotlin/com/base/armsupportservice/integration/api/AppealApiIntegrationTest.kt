package com.base.armsupportservice.integration.api

import com.base.armsupportservice.SecurityTestSupport
import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.dto.appeal.AppealMessageRequest
import com.base.armsupportservice.dto.appeal.AppealRequest
import com.base.armsupportservice.dto.appeal.AppealUpdateRequest
import com.base.armsupportservice.dto.appeal.AssignOperatorRequest
import com.base.armsupportservice.dto.appeal.ChangeStatusRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
class AppealApiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    private lateinit var operatorId: UUID

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
        operatorId = UUID.randomUUID()
        inserter.insertSyncedUser(id = operatorId, username = "op1", email = "op1@test.local")
    }

    @Test
    fun `GET appeals without auth returns 401`() {
        mockMvc
            .perform(get("/api/v1/appeals"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create and get appeal`() {
        val id = createAppealViaApi()
        mockMvc
            .perform(
                get("/api/v1/appeals/$id")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.subject").value("Интеграционное обращение"))
            .andExpect(jsonPath("$.status").value("PENDING_PROCESSING"))
    }

    @Test
    fun `filter appeals`() {
        createAppealViaApi()
        mockMvc
            .perform(
                get("/api/v1/appeals")
                    .param("page", "0")
                    .param("size", "10")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `take into work and send operator message`() {
        val id = createAppealViaApi()
        mockMvc
            .perform(
                post("/api/v1/appeals/$id/take")
                    .with(SecurityTestSupport.operator(operatorId)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))

        val msg =
            AppealMessageRequest(
                content = "Ответ оператора",
                channel = AppealChannel.EMAIL,
            )
        mockMvc
            .perform(
                post("/api/v1/appeals/$id/messages")
                    .with(SecurityTestSupport.operator(operatorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(msg)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.content").value("Ответ оператора"))

        mockMvc
            .perform(
                get("/api/v1/appeals/$id")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("WAITING_CLIENT_RESPONSE"))
    }

    @Test
    fun `assign operator and inbound client message`() {
        val id = createAppealViaApi()
        mockMvc.perform(
            post("/api/v1/appeals/$id/take")
                .with(SecurityTestSupport.operator(operatorId)),
        )

        val otherId = UUID.randomUUID()
        inserter.insertSyncedUser(id = otherId, username = "op2", email = "op2@test.local")
        mockMvc
            .perform(
                post("/api/v1/appeals/$id/assign")
                    .with(SecurityTestSupport.operator(operatorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            AssignOperatorRequest(operatorId = otherId),
                        ),
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.assignedOperator.id").value(otherId.toString()))

        val opMsg =
            AppealMessageRequest(
                content = "Ждём клиента",
                channel = AppealChannel.CHAT,
            )
        mockMvc.perform(
            post("/api/v1/appeals/$id/messages")
                .with(SecurityTestSupport.operator(operatorId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opMsg)),
        )

        val inbound =
            AppealMessageRequest(
                content = "Ответ клиента",
                channel = AppealChannel.CHAT,
                externalMessageId = "ext-1",
            )
        mockMvc
            .perform(
                post("/api/v1/appeals/$id/messages/inbound")
                    .with(SecurityTestSupport.operator(operatorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inbound)),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(
                get("/api/v1/appeals/$id")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
    }

    @Test
    fun `PUT update appeal`() {
        val id = createAppealViaApi()
        val update =
            AppealUpdateRequest(
                subject = "Новая тема",
                contactName = "Иван",
            )
        mockMvc
            .perform(
                put("/api/v1/appeals/$id")
                    .with(SecurityTestSupport.operator(operatorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.subject").value("Новая тема"))
    }

    @Test
    fun `invalid status transition returns 409`() {
        val id = createAppealViaApi()
        mockMvc
            .perform(
                patch("/api/v1/appeals/$id/status")
                    .with(SecurityTestSupport.operator(operatorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            ChangeStatusRequest(AppealStatus.WAITING_CLIENT_RESPONSE),
                        ),
                    ),
            ).andExpect(status().isConflict)
    }

    @Test
    fun `close appeal`() {
        val id = createAppealViaApi()
        mockMvc.perform(
            post("/api/v1/appeals/$id/take")
                .with(SecurityTestSupport.operator(operatorId)),
        )
        mockMvc
            .perform(
                post("/api/v1/appeals/$id/close")
                    .with(SecurityTestSupport.operator(operatorId)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CLOSED"))
    }

    @Test
    fun `delete allowed for admin`() {
        val id = createAppealViaApi()
        mockMvc
            .perform(
                delete("/api/v1/appeals/$id")
                    .with(SecurityTestSupport.admin()),
            ).andExpect(status().isNoContent)
    }

    @Test
    fun `get unknown appeal returns 404`() {
        mockMvc
            .perform(
                get("/api/v1/appeals/${UUID.randomUUID()}")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `create with invalid organization returns 404`() {
        val body =
            AppealRequest(
                subject = "X",
                channel = AppealChannel.EMAIL,
                direction = AppealDirection.INBOUND,
                organizationId = UUID.randomUUID(),
            )
        mockMvc
            .perform(
                post("/api/v1/appeals")
                    .with(SecurityTestSupport.operator(operatorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isNotFound)
    }

    private fun createAppealViaApi(): UUID {
        val body =
            AppealRequest(
                subject = "Интеграционное обращение",
                channel = AppealChannel.EMAIL,
                direction = AppealDirection.INBOUND,
            )
        val result =
            mockMvc
                .perform(
                    post("/api/v1/appeals")
                        .with(SecurityTestSupport.operator(operatorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)),
                ).andExpect(status().isCreated)
                .andReturn()
        return UUID.fromString(
            objectMapper.readTree(result.response.contentAsString).get("id").asText(),
        )
    }
}
