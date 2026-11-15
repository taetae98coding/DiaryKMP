package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity

internal class RoutineViewScopeColumnTypeConverter {
    @ColumnTypeConverter
    fun viewScopeToText(viewScope: RoutineViewScopeLocalEntity): String = viewScope.persistentValue

    @ColumnTypeConverter
    fun textToViewScope(value: String): RoutineViewScopeLocalEntity = requireNotNull(RoutineViewScopeLocalEntity.fromPersistentValue(value)) { "Unknown routine view scope: $value" }
}
