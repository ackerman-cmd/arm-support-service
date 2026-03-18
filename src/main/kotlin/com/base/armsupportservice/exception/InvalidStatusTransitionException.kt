package com.base.armsupportservice.exception

import com.base.armsupportservice.domain.appeal.AppealStatus

class InvalidStatusTransitionException(
    from: AppealStatus,
    to: AppealStatus,
) : RuntimeException("Недопустимый переход статуса: $from → $to")
