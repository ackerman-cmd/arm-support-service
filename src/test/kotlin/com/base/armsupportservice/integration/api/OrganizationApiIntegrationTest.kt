package com.base.armsupportservice.integration.api

import com.base.armsupportservice.SecurityTestSupport
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.dto.organization.OrganizationRequest
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

@AutoConfigureMockMvc
class OrganizationApiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
    }

    @Test
    fun `GET organizations without auth returns 401`() {
        mockMvc
            .perform(get("/api/v1/organizations"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET organizations returns page when authorized`() {
        mockMvc
            .perform(
                get("/api/v1/organizations")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `POST organization without write permission returns 403`() {
        val body =
            OrganizationRequest(
                name = "ООО Тест",
                inn = "7707083893",
            )
        mockMvc
            .perform(
                post("/api/v1/organizations")
                    .with(SecurityTestSupport.readOnly())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `POST organization creates resource`() {
        val body =
            OrganizationRequest(
                name = "ООО Новая",
                inn = "7707083893",
            )
        mockMvc
            .perform(
                post("/api/v1/organizations")
                    .with(SecurityTestSupport.operator())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("ООО Новая"))
            .andExpect(jsonPath("$.inn").value("7707083893"))
    }

    @Test
    fun `POST duplicate INN returns 409`() {
        val body =
            OrganizationRequest(
                name = "Первая",
                inn = "7707083893",
            )
        mockMvc.perform(
            post("/api/v1/organizations")
                .with(SecurityTestSupport.operator())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)),
        )
        mockMvc
            .perform(
                post("/api/v1/organizations")
                    .with(SecurityTestSupport.operator())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            OrganizationRequest(name = "Вторая", inn = "7707083893"),
                        ),
                    ),
            ).andExpect(status().isConflict)
    }

    @Test
    fun `GET by id returns organization`() {
        val create =
            OrganizationRequest(
                name = "Для чтения",
                inn = "7707083894",
            )
        val mvcResult =
            mockMvc
                .perform(
                    post("/api/v1/organizations")
                        .with(SecurityTestSupport.operator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)),
                ).andExpect(status().isCreated)
                .andReturn()
        val id = objectMapper.readTree(mvcResult.response.contentAsString).get("id").asText()

        mockMvc
            .perform(
                get("/api/v1/organizations/$id")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Для чтения"))
    }

    @Test
    fun `PUT updates organization`() {
        val create =
            OrganizationRequest(
                name = "Старое имя",
                inn = "7707083895",
            )
        val created =
            mockMvc
                .perform(
                    post("/api/v1/organizations")
                        .with(SecurityTestSupport.operator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)),
                ).andReturn()
        val id = objectMapper.readTree(created.response.contentAsString).get("id").asText()

        val update =
            OrganizationRequest(
                name = "Новое имя",
                inn = "7707083895",
            )
        mockMvc
            .perform(
                put("/api/v1/organizations/$id")
                    .with(SecurityTestSupport.operator())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Новое имя"))
    }

    @Test
    fun `DELETE without admin returns 403`() {
        val create =
            OrganizationRequest(
                name = "X",
                inn = "7707083896",
            )
        val created =
            mockMvc
                .perform(
                    post("/api/v1/organizations")
                        .with(SecurityTestSupport.operator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)),
                ).andReturn()
        val id = objectMapper.readTree(created.response.contentAsString).get("id").asText()

        mockMvc
            .perform(
                delete("/api/v1/organizations/$id")
                    .with(SecurityTestSupport.operator()),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `DELETE with admin returns 204`() {
        val create =
            OrganizationRequest(
                name = "Удалить",
                inn = "7707083897",
            )
        val created =
            mockMvc
                .perform(
                    post("/api/v1/organizations")
                        .with(SecurityTestSupport.operator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)),
                ).andReturn()
        val id = objectMapper.readTree(created.response.contentAsString).get("id").asText()

        mockMvc
            .perform(
                delete("/api/v1/organizations/$id")
                    .with(SecurityTestSupport.admin()),
            ).andExpect(status().isNoContent)
    }

    @Test
    fun `search by name`() {
        val body =
            OrganizationRequest(
                name = "УникальныйПоиск",
                inn = "7707083898",
            )
        mockMvc.perform(
            post("/api/v1/organizations")
                .with(SecurityTestSupport.operator())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)),
        )

        mockMvc
            .perform(
                get("/api/v1/organizations/search")
                    .param("name", "Уникальный")
                    .with(SecurityTestSupport.readOnly()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].name").value("УникальныйПоиск"))
    }

    @Test
    fun `invalid INN returns 400`() {
        val body =
            mapOf(
                "name" to "Bad",
                "inn" to "123",
            )
        mockMvc
            .perform(
                post("/api/v1/organizations")
                    .with(SecurityTestSupport.operator())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)),
            ).andExpect(status().isBadRequest)
    }
}
