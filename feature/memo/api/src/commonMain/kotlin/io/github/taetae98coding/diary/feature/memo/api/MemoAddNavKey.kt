package io.github.taetae98coding.diary.feature.memo.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class MemoAddNavKey(
    val primaryTagId: Uuid? = null,
    val initialDateRange: InitialDateRange? = null,
) : ScreenNavKey {
    override val screenName: String get() = "MemoAdd"

    @Serializable
    public data class InitialDateRange(
        val start: LocalDate,
        val endInclusive: LocalDate,
    )
}
