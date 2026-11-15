package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity

internal class RoutineCycleColumnTypeConverter {
    @ColumnTypeConverter
    fun cycleToText(cycle: RoutineCycleLocalEntity): String = cycle.persistentValue

    @ColumnTypeConverter
    fun textToCycle(value: String): RoutineCycleLocalEntity = requireNotNull(RoutineCycleLocalEntity.fromPersistentValue(value)) { "Unknown routine cycle: $value" }
}
