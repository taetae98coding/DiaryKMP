package io.github.taetae98coding.diary.feature.memo.api

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class MemoAddNavKey(
    val primaryTagId: Uuid? = null,
    val initialDateRange: InitialDateRange? = null,
) : NavKey {
    @Serializable
    public data class InitialDateRange(
        val start: LocalDate,
        val endInclusive: LocalDate,
    )
}
