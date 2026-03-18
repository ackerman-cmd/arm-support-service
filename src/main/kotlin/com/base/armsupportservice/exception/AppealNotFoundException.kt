package com.base.armsupportservice.exception

import java.util.UUID

class AppealNotFoundException(
    id: UUID,
) : RuntimeException("Обращение с id=$id не найдено")
