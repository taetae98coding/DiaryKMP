package io.github.taetae98coding.diary.work.appaction.appfunction.checklist

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklist
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistDetail
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItem
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage

/** A checklist. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionChecklist(
    /** Id of the checklist. */
    val id: String,
    /** Title of the checklist. */
    val title: String,
    /** Description of the checklist. */
    val description: String,
    /** When the checklist was created, in ISO-8601. */
    val createdAt: String,
    /** When the checklist was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A checklist with how many of its items are checked. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionChecklistSummary(
    /** Id of the checklist. */
    val id: String,
    /** Title of the checklist. */
    val title: String,
    /** Description of the checklist. */
    val description: String,
    /** Number of checked items. */
    val checkedCount: Int,
    /** Number of all items. */
    val totalCount: Int,
    /** When the checklist was created, in ISO-8601. */
    val createdAt: String,
    /** When the checklist was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** An item of a checklist. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionChecklistItem(
    /** Id of the checklist item. */
    val id: String,
    /** Content of the item. */
    val content: String,
    /** Whether the item is checked. */
    val isChecked: Boolean,
    /** When the item was created, in ISO-8601. */
    val createdAt: String,
    /** When the item was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A checklist with its items and linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionChecklistDetail(
    /** The checklist. */
    val checklist: AppFunctionChecklist,
    /** Items of the checklist that are not deleted. */
    val items: List<AppFunctionChecklistItem>,
    /** Tags linked to the checklist. */
    val tags: List<AppFunctionTag>,
)

/** One page of checklists. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionChecklistSummaryPage(
    /** Checklists in this page, at most 100. */
    val items: List<AppFunctionChecklistSummary>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionPage<AppActionChecklistSummary>.toAppFunctionChecklistSummaryPage(): AppFunctionChecklistSummaryPage = AppFunctionChecklistSummaryPage(items = items.map { summary -> summary.toAppFunctionChecklistSummary() }, nextCursor = nextCursor)

internal fun AppActionChecklistDetail.toAppFunctionChecklistDetail(): AppFunctionChecklistDetail =
    AppFunctionChecklistDetail(
        checklist = checklist.toAppFunctionChecklist(),
        items = items.map { item -> item.toAppFunctionChecklistItem() },
        tags = tags.map { tag -> tag.toAppFunctionTag() },
    )

private fun AppActionChecklist.toAppFunctionChecklist(): AppFunctionChecklist = AppFunctionChecklist(id = id, title = title, description = description, createdAt = createdAt, updatedAt = updatedAt)

private fun AppActionChecklistSummary.toAppFunctionChecklistSummary(): AppFunctionChecklistSummary =
    AppFunctionChecklistSummary(
        id = id,
        title = title,
        description = description,
        checkedCount = checkedCount,
        totalCount = totalCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

private fun AppActionChecklistItem.toAppFunctionChecklistItem(): AppFunctionChecklistItem = AppFunctionChecklistItem(id = id, content = content, isChecked = isChecked, createdAt = createdAt, updatedAt = updatedAt)
