package com.base.armsupportservice.exception

import java.util.UUID

class MailboxNotConfiguredException(
    groupId: UUID,
) : RuntimeException("Для группы назначения с id=$groupId не настроен почтовый ящик")
