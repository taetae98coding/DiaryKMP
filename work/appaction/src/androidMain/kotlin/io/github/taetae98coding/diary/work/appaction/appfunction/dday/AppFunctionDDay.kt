package io.github.taetae98coding.diary.work.appaction.appfunction.dday

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDay
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayCount
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayDetail

/** The days left or passed of a D-Day, counted from today. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionDDayCount(
    /** upcoming when the base date has not come yet, today on the base date, passed after it. */
    val status: String,
    /** Days left until the base date. Present only when upcoming. */
    val remainingDays: Int?,
    /** Days passed since the base date. Present only when passed. */
    val passedDays: Int?,
    /** Full years between today and the base date. Absent when today. */
    val years: Int?,
    /** Full months left over after the years. Absent when today. */
    val months: Int?,
)

/** A D-Day. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionDDay(
    /** Id of the D-Day. */
    val id: String,
    /** Title of the D-Day. */
    val title: String,
    /** Description of the D-Day. */
    val description: String,
    /** Color of the D-Day as a hex color like #4285F4. */
    val color: String,
    /** Base date of the D-Day, like 2026-01-31. */
    val baseDate: String,
    /** Whether the base date counts as day 1. */
    val countsBaseDay: Boolean,
    /** Repeat intervals as ISO 8601 periods like P100D, P18M or P1Y. */
    val repeats: List<String>,
    /** Last date anniversaries are shown. Null when there is no end date. */
    val endDate: String?,
    /** What to do when a monthly anniversary falls on a date the month does not have: clamp_to_month_end, skip or next_month_first_day. */
    val overflowPolicy: String,
    /** When the D-Day was created, in ISO-8601. */
    val createdAt: String,
    /** When the D-Day was last updated, in ISO-8601. */
    val updatedAt: String,
    /** The days left or passed, counted from today. */
    val count: AppFunctionDDayCount,
)

/** A D-Day with its linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionDDayDetail(
    /** The D-Day. */
    val dday: AppFunctionDDay,
    /** Tags linked to the D-Day. */
    val tags: List<AppFunctionTag>,
)

/** One page of D-Days. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionDDayPage(
    /** D-Days in this page, at most 100. */
    val items: List<AppFunctionDDay>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionPage<AppActionDDay>.toAppFunctionDDayPage(): AppFunctionDDayPage = AppFunctionDDayPage(items = items.map { dday -> dday.toAppFunctionDDay() }, nextCursor = nextCursor)

internal fun AppActionDDayDetail.toAppFunctionDDayDetail(): AppFunctionDDayDetail = AppFunctionDDayDetail(dday = dday.toAppFunctionDDay(), tags = tags.map { tag -> tag.toAppFunctionTag() })

internal fun AppActionDDay.toAppFunctionDDay(): AppFunctionDDay =
    AppFunctionDDay(
        id = id,
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats,
        endDate = endDate,
        overflowPolicy = overflowPolicy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        count = count.toAppFunctionDDayCount(),
    )

private fun AppActionDDayCount.toAppFunctionDDayCount(): AppFunctionDDayCount =
    AppFunctionDDayCount(
        status = status,
        remainingDays = remainingDays,
        passedDays = passedDays,
        years = years,
        months = months,
    )
