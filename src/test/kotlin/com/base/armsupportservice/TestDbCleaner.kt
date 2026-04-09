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
        jdbc.update("DELETE FROM arm_support.appeal_events")
        jdbc.update("DELETE FROM arm_support.appeal_messages")
        jdbc.update("DELETE FROM arm_support.appeals")
        jdbc.update("DELETE FROM arm_support.assignment_group_operators")
        jdbc.update("DELETE FROM arm_support.skill_group_operators")
        jdbc.update("DELETE FROM arm_support.skill_group_skills")
        jdbc.update("DELETE FROM arm_support.assignment_groups")
        jdbc.update("DELETE FROM arm_support.skill_groups")
        jdbc.update("DELETE FROM arm_support.organizations")
        jdbc.update("DELETE FROM arm_support.synced_users")
    }
}
