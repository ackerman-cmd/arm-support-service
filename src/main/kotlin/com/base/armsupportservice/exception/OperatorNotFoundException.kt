package com.base.armsupportservice.exception

import java.util.UUID

class OperatorNotFoundException(
    id: UUID,
) : RuntimeException("Оператор с id=$id не найден в системе")
