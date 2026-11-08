package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy

public fun DDayOverflowPolicy.toLocal(): DDayOverflowPolicyLocalEntity =
    when (this) {
        DDayOverflowPolicy.CLAMP_TO_MONTH_END -> DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END
        DDayOverflowPolicy.SKIP -> DDayOverflowPolicyLocalEntity.SKIP
        DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY -> DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY
    }

public fun DDayOverflowPolicyLocalEntity.toDomain(): DDayOverflowPolicy =
    when (this) {
        DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END -> DDayOverflowPolicy.CLAMP_TO_MONTH_END
        DDayOverflowPolicyLocalEntity.SKIP -> DDayOverflowPolicy.SKIP
        DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY -> DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY
    }
