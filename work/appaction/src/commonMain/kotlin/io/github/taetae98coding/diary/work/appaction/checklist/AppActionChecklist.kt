package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.work.appaction.core.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import kotlin.jvm.JvmName

public data class AppActionChecklist(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
)

public data class AppActionChecklistSummary(
    val id: String,
    val title: String,
    val description: String,
    val checkedCount: Int,
    val totalCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

public data class AppActionChecklistItem(
    val id: String,
    val content: String,
    val isChecked: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

public data class AppActionChecklistDetail(
    val checklist: AppActionChecklist,
    val items: List<AppActionChecklistItem>,
    val tags: List<AppActionTag>,
)

internal fun Checklist.toAppActionChecklist(): AppActionChecklist =
    AppActionChecklist(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

internal fun ChecklistSummary.toAppActionChecklistSummary(): AppActionChecklistSummary =
    AppActionChecklistSummary(
        id = checklist.id.toString(),
        title = checklist.detail.title,
        description = checklist.detail.description,
        checkedCount = checkedCount,
        totalCount = totalCount,
        createdAt = checklist.createdAt.toString(),
        updatedAt = checklist.updatedAt.toString(),
    )

internal fun ChecklistItem.toAppActionChecklistItem(): AppActionChecklistItem =
    AppActionChecklistItem(
        id = id.toString(),
        content = content,
        isChecked = isChecked,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

@JvmName("filterChecklistBySearchQuery")
public fun List<AppActionChecklistSummary>.filterBySearchQuery(query: String): List<AppActionChecklistSummary> = filterBySearchQuery(query = query) { checklist -> listOf(checklist.title, checklist.description) }

@JvmName("filterChecklistItemBySearchQuery")
public fun List<AppActionChecklistItem>.filterBySearchQuery(query: String): List<AppActionChecklistItem> = filterBySearchQuery(query = query) { item -> listOf(item.content) }
