package com.base.armsupportservice.integration.api

import com.base.armsupportservice.SecurityTestSupport
import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.dto.group.GroupOperatorsRequest
import com.base.armsupportservice.dto.group.SkillGroupRequest
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
class SkillGroupApiIntegrationTest : AbstractIntegrationTest() {
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
    fun `POST skill-group succeeds for operator`() {
        mockMvc
            .perform(
                post("/api/v1/skill-groups")
                    .with(SecurityTestSupport.operator())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            SkillGroupRequest(name = "S"),
                        ),
                    ),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("S"))
    }

    @Test
    fun `admin creates skill group with skills`() {
        val body =
            SkillGroupRequest(
                name = "ДБО",
                skills = setOf("IB_MOBILE_DBO", "ACCESS_RECOVERY"),
            )
        mockMvc
            .perform(
                post("/api/v1/skill-groups")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("ДБО"))
            .andExpect(jsonPath("$.skills.length()").value(2))
    }

    @Test
    fun `GET by id`() {
        val g =
            inserter.insertSkillGroup(
                name = "Чтение",
                skills = mutableSetOf("A"),
            )
        mockMvc
            .perform(
                get("/api/v1/skill-groups/${g.id}")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.skills[0]").value("A"))
    }

    @Test
    fun `addOperators`() {
        val g = inserter.insertSkillGroup(name = "Команда")
        val op = inserter.insertSyncedUser()
        mockMvc
            .perform(
                post("/api/v1/skill-groups/${g.id}/operators")
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
    fun `PUT replaces skills`() {
        val g =
            inserter.insertSkillGroup(
                name = "Обновить",
                skills = mutableSetOf("OLD"),
            )
        val body =
            SkillGroupRequest(
                name = "Обновить",
                skills = setOf("NEW1", "NEW2"),
            )
        mockMvc
            .perform(
                put("/api/v1/skill-groups/${g.id}")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.skills.length()").value(2))
    }

    @Test
    fun `delete skill group`() {
        val g = inserter.insertSkillGroup(name = "Удалить")
        mockMvc
            .perform(
                delete("/api/v1/skill-groups/${g.id}")
                    .with(SecurityTestSupport.admin()),
            ).andExpect(status().isNoContent)
    }

    @Test
    fun `unknown operator returns 404`() {
        val body =
            SkillGroupRequest(
                name = "X",
                operatorIds = setOf(UUID.randomUUID()),
            )
        mockMvc
            .perform(
                post("/api/v1/skill-groups")
                    .with(SecurityTestSupport.admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isNotFound)
    }
}
