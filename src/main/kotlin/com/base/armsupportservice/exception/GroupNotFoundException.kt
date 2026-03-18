package com.base.armsupportservice.exception

import java.util.UUID

class GroupNotFoundException(
    id: UUID,
) : RuntimeException("Группа с id=$id не найдена")
