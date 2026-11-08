package io.github.taetae98coding.diary.work.appaction.dday

import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument

private const val OVERFLOW_POLICY = "overflowPolicy"

public object AppActionDDayOverflowPolicyValue {
    public const val CLAMP_TO_MONTH_END: String = "clamp_to_month_end"
    public const val SKIP: String = "skip"
    public const val NEXT_MONTH_FIRST_DAY: String = "next_month_first_day"
}

public val appActionDDayOverflowPolicyList: List<DDayOverflowPolicy> =
    listOf(DDayOverflowPolicy.CLAMP_TO_MONTH_END, DDayOverflowPolicy.SKIP, DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY)

public fun DDayOverflowPolicy.toDDayOverflowPolicyValue(): String =
    when (this) {
        DDayOverflowPolicy.CLAMP_TO_MONTH_END -> AppActionDDayOverflowPolicyValue.CLAMP_TO_MONTH_END
        DDayOverflowPolicy.SKIP -> AppActionDDayOverflowPolicyValue.SKIP
        DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY -> AppActionDDayOverflowPolicyValue.NEXT_MONTH_FIRST_DAY
    }

public fun String.toDDayOverflowPolicyOrNull(): DDayOverflowPolicy? = appActionDDayOverflowPolicyList.firstOrNull { policy -> policy.toDDayOverflowPolicyValue() == this }

internal fun String?.toDDayOverflowPolicyArgumentOrNull(): DDayOverflowPolicy? =
    this?.let { value ->
        value.toDDayOverflowPolicyOrNull()
            ?: invalidArgument(message = "'$OVERFLOW_POLICY' must be one of ${appActionDDayOverflowPolicyList.joinToString { policy -> policy.toDDayOverflowPolicyValue() }}.")
    }
