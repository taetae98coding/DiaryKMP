package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayOverflowPolicyRemoteEntity

internal fun DDayOverflowPolicyLocalEntity.toRemote(): DDayOverflowPolicyRemoteEntity =
    when (this) {
        DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END -> DDayOverflowPolicyRemoteEntity.CLAMP_TO_MONTH_END
        DDayOverflowPolicyLocalEntity.SKIP -> DDayOverflowPolicyRemoteEntity.SKIP
        DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY -> DDayOverflowPolicyRemoteEntity.NEXT_MONTH_FIRST_DAY
    }

internal fun DDayOverflowPolicyRemoteEntity.toLocal(): DDayOverflowPolicyLocalEntity =
    when (this) {
        DDayOverflowPolicyRemoteEntity.CLAMP_TO_MONTH_END -> DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END
        DDayOverflowPolicyRemoteEntity.SKIP -> DDayOverflowPolicyLocalEntity.SKIP
        DDayOverflowPolicyRemoteEntity.NEXT_MONTH_FIRST_DAY -> DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY
    }
