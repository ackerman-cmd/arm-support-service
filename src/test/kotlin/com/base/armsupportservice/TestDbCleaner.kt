package com.base.armsupportservice

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TestDbCleaner(
    private val jdbc: JdbcTemplate,
) {
    @Transactional
    fun clearAllTables() {
        jdbc.execute("""DELETE FROM "arm_support"."synced_users"""")
    }
}
