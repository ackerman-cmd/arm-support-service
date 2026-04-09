package com.base.armsupportservice.dto.mailbox

import java.util.UUID

data class ActiveMailboxResponse(
    val groupId: UUID,
    val groupType: String,
    val groupName: String,
    val email: String,
)
