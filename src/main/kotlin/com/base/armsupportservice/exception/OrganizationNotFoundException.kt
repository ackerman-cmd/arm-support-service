package com.base.armsupportservice.exception

import java.util.UUID

class OrganizationNotFoundException(
    id: UUID,
) : RuntimeException("Организация с id=$id не найдена")
