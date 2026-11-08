package io.github.taetae98coding.diary.work.appaction.appfunction.memo

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionContact
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionContact
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionPlace
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionWeb
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTimeInput
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDetail

/** The period of a memo. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMemoDateTime(
    /** True for an all-day period whose start and endInclusive are dates like 2026-01-01. False for a timed period whose start and endInclusive are date times like 2026-01-01T09:00. */
    val allDay: Boolean,
    /** Start of the period. */
    val start: String,
    /** Last moment of the period, inclusive. */
    val endInclusive: String,
)

/** A period to set on a memo. All-day: allDay true with dates like 2026-01-01. Timed: allDay false with date times like 2026-01-01T09:00. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMemoDateTimeInput(
    /** True for an all-day period, false for a timed period. Required. */
    val allDay: Boolean? = null,
    /** Start of the period. A date like 2026-01-01 when all-day, otherwise a date time like 2026-01-01T09:00. */
    val start: String? = null,
    /** Last day or moment of the period, inclusive. Must not be before start. */
    val endInclusive: String? = null,
)

/** A memo. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMemo(
    /** Id of the memo. */
    val id: String,
    /** Title of the memo. */
    val title: String,
    /** Description of the memo. */
    val description: String,
    /** Color of the memo as a hex color like #4285F4. */
    val color: String,
    /** Period of the memo. Null when the memo has no period. */
    val dateTime: AppFunctionMemoDateTime?,
    /** Id of the primary tag. Null when the memo has no primary tag. */
    val primaryTagId: String?,
    /** Whether the memo is finished. */
    val isFinished: Boolean,
    /** When the memo was created, in ISO-8601. */
    val createdAt: String,
    /** When the memo was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** One page of memos. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMemoPage(
    /** Memos in this page, at most 100. */
    val items: List<AppFunctionMemo>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** A memo with its linked tags, places, webs and contacts. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMemoDetail(
    /** The memo. */
    val memo: AppFunctionMemo,
    /** Tags linked to the memo. */
    val tags: List<AppFunctionTag>,
    /** Places linked to the memo. */
    val places: List<AppFunctionPlace>,
    /** Webs linked to the memo. */
    val webs: List<AppFunctionWeb>,
    /** Contacts linked to the memo. */
    val contacts: List<AppFunctionContact>,
)

internal fun AppActionMemoDateTime.toAppFunctionMemoDateTime(): AppFunctionMemoDateTime = AppFunctionMemoDateTime(allDay = allDay, start = start, endInclusive = endInclusive)

internal fun AppFunctionMemoDateTimeInput.toAppActionMemoDateTimeInput(): AppActionMemoDateTimeInput = AppActionMemoDateTimeInput(allDay = allDay, start = start, endInclusive = endInclusive)

internal fun AppActionMemo.toAppFunctionMemo(): AppFunctionMemo =
    AppFunctionMemo(
        id = id,
        title = title,
        description = description,
        color = color,
        dateTime = dateTime?.toAppFunctionMemoDateTime(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionPage<AppActionMemo>.toAppFunctionMemoPage(): AppFunctionMemoPage = AppFunctionMemoPage(items = items.map { memo -> memo.toAppFunctionMemo() }, nextCursor = nextCursor)

internal fun AppActionMemoDetail.toAppFunctionMemoDetail(): AppFunctionMemoDetail =
    AppFunctionMemoDetail(
        memo = memo.toAppFunctionMemo(),
        tags = tags.map { tag -> tag.toAppFunctionTag() },
        places = places.map { place -> place.toAppFunctionPlace() },
        webs = webs.map { web -> web.toAppFunctionWeb() },
        contacts = contacts.map { contact -> contact.toAppFunctionContact() },
    )
