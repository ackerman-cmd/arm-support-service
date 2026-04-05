package com.base.armsupportservice.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName::class)
@EmbeddedKafka(
    partitions = 1,
    topics = [
        "user-sync-test",
        "email.conversation.created.test",
        "email.conversation.matched.test",
        "email.inbound.persisted.test",
        "email.outbound.requested.test",
        "email.outbound.sent.test",
        "email.outbound.failed.test",
    ],
)
abstract class AbstractIntegrationTest {
    companion object {
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> =
            PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
                withDatabaseName("user_service")
                withUsername("test")
                withPassword("test")
                withInitScript("db/init.sql")
                start()
            }
    }
}
