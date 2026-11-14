package io.github.taetae98coding.diary.work.appaction.routine

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument

private const val CYCLE = "cycle"

public object AppActionRoutineCycleValue {
    public const val DAILY: String = "FREQ=DAILY"
    public const val WEEKDAYS: String = "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR"
    public const val WEEKENDS: String = "FREQ=DAILY;BYDAY=SA,SU"
    public const val WEEKLY: String = "FREQ=WEEKLY;WKST=SU"
    public const val MONTHLY: String = "FREQ=MONTHLY"
}

public val appActionRoutineCycleList: List<RoutineCycle> = listOf(RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKENDS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY)

public fun RoutineCycle.toRoutineCycleValue(): String =
    when (this) {
        RoutineCycle.DAILY -> AppActionRoutineCycleValue.DAILY
        RoutineCycle.WEEKDAYS -> AppActionRoutineCycleValue.WEEKDAYS
        RoutineCycle.WEEKENDS -> AppActionRoutineCycleValue.WEEKENDS
        RoutineCycle.WEEKLY -> AppActionRoutineCycleValue.WEEKLY
        RoutineCycle.MONTHLY -> AppActionRoutineCycleValue.MONTHLY
    }

public fun String.toRoutineCycleOrNull(): RoutineCycle? = appActionRoutineCycleList.firstOrNull { cycle -> cycle.toRoutineCycleValue() == this }

internal fun String?.toRoutineCycleArgumentOrNull(): RoutineCycle? =
    this?.let { value ->
        value.toRoutineCycleOrNull()
            ?: invalidArgument(message = "'$CYCLE' must be one of ${appActionRoutineCycleList.joinToString { cycle -> cycle.toRoutineCycleValue() }}.")
    }
