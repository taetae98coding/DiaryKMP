package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity

internal class DDayOverflowPolicyColumnTypeConverter {
    @ColumnTypeConverter
    fun overflowPolicyToText(overflowPolicy: DDayOverflowPolicyLocalEntity): String = overflowPolicy.persistentValue

    @ColumnTypeConverter
    fun textToOverflowPolicy(value: String): DDayOverflowPolicyLocalEntity = requireNotNull(DDayOverflowPolicyLocalEntity.fromPersistentValue(value)) { "Unknown dday overflow policy: $value" }
}
