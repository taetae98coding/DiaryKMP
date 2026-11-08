package io.github.taetae98coding.diary.compose.dday

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun previewDDayToday(): LocalDate = LocalDate(year = 2026, month = 3, day = 20)

public fun previewDDay(
    title: String,
    baseDate: LocalDate = LocalDate(year = 2025, month = 1, day = 15),
    color: Long = 0xFF3A7BD5,
): DDay =
    DDay(
        id = Uuid.random(),
        detail =
            DDayDetail(
                title = title,
                description = "",
                color = color,
                baseDate = baseDate,
                countsBaseDay = true,
                repeats = emptySet(),
                endDate = null,
                overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
