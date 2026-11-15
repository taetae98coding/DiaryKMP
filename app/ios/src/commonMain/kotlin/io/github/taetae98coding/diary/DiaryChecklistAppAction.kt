package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklist
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistDetail
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItem
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItemSortValue
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistItemAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage

public enum class DiaryAppActionChecklistItemSort {
    ADDED,
    UNCHECKED_FIRST,
}

public data class DiaryAppActionChecklistSummary(
    val id: String,
    val title: String,
    val checklistDescription: String,
    val checkedCount: Int,
    val totalCount: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionChecklistSummaryPage(
    val items: List<DiaryAppActionChecklistSummary>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionChecklist(
    val id: String,
    val title: String,
    val checklistDescription: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionChecklistItem(
    val id: String,
    val content: String,
    val isChecked: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionChecklistItemInChecklist(
    val checklistId: String,
    val item: DiaryAppActionChecklistItem,
)

public data class DiaryAppActionChecklistDetail(
    val checklist: DiaryAppActionChecklist,
    val items: List<DiaryAppActionChecklistItem>,
    val tags: List<DiaryAppActionTag>,
)

public object DiaryChecklistAppAction {
    public suspend fun list(pageNumber: Int): DiaryAppActionChecklistSummaryPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> checklistAppAction().list(cursor = cursor) }

        return DiaryAppActionChecklistSummaryPage(items = page.items.map { summary -> summary.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun get(
        checklistId: String,
        itemSort: DiaryAppActionChecklistItemSort?,
    ): DiaryAppActionChecklistDetail? = checklistAppAction().get(checklistId = checklistId, itemSort = itemSort?.toValue())?.toDiary()

    public suspend fun getIncludingDeleted(
        checklistId: String,
        itemSort: DiaryAppActionChecklistItemSort?,
    ): DiaryAppActionChecklistDetail? = checklistAppAction().getIncludingDeleted(checklistId = checklistId, itemSort = itemSort?.toValue())?.toDiary()

    public suspend fun add(
        title: String,
        checklistDescription: String?,
        items: List<String>,
        tagIds: List<String>,
    ): String = checklistAppAction().add(title = title, description = checklistDescription, items = items, tagIds = tagIds).id

    public suspend fun update(
        checklistId: String,
        title: String?,
        checklistDescription: String?,
    ): Int = checklistAppAction().update(checklistId = checklistId, title = title, description = checklistDescription).changedCount

    public suspend fun reset(checklistId: String): Int = checklistAppAction().reset(checklistId = checklistId).changedCount

    public suspend fun delete(checklistId: String): Int = checklistAppAction().delete(checklistId = checklistId).changedCount

    public suspend fun restore(checklistId: String): Int = checklistAppAction().restore(checklistId = checklistId).changedCount
}

public object DiaryChecklistItemAppAction {
    public suspend fun add(
        checklistId: String,
        content: String,
    ): Int = checklistItemAppAction().add(checklistId = checklistId, content = content).changedCount

    public suspend fun check(itemId: String): Int = checklistItemAppAction().check(itemId = itemId).changedCount

    public suspend fun uncheck(itemId: String): Int = checklistItemAppAction().uncheck(itemId = itemId).changedCount

    public suspend fun delete(itemId: String): Int = checklistItemAppAction().delete(itemId = itemId).changedCount
}

// 검색 동작이 없어 체크리스트와 항목 선택 목록의 검색 칸이 쓴다.
public object DiaryChecklistSearchAppAction {
    public suspend fun search(query: String): List<DiaryAppActionChecklistSummary> =
        searchPageChecklistList()
            .filterBySearchQuery(query = query)
            .map { summary -> summary.toDiary() }

    // 항목 선택 목록과 같이 첫 페이지 체크리스트들의 항목을 체크리스트 순서대로 거른다.
    public suspend fun searchItem(query: String): List<DiaryAppActionChecklistItemInChecklist> =
        searchPageChecklistList().flatMap { summary ->
            checklistAppAction()
                .get(checklistId = summary.id, itemSort = null)
                ?.items
                .orEmpty()
                .filterBySearchQuery(query = query)
                .map { item -> DiaryAppActionChecklistItemInChecklist(checklistId = summary.id, item = item.toDiary()) }
        }
}

private fun checklistAppAction(): ChecklistAppAction = diaryAppAction()

private fun checklistItemAppAction(): ChecklistItemAppAction = diaryAppAction()

private suspend fun searchPageChecklistList(): List<AppActionChecklistSummary> = readNumberedPage(pageNumber = SEARCH_PAGE_NUMBER) { cursor -> checklistAppAction().list(cursor = cursor) }.items

private fun DiaryAppActionChecklistItemSort.toValue(): String =
    when (this) {
        DiaryAppActionChecklistItemSort.ADDED -> AppActionChecklistItemSortValue.ADDED
        DiaryAppActionChecklistItemSort.UNCHECKED_FIRST -> AppActionChecklistItemSortValue.UNCHECKED_FIRST
    }

private fun AppActionChecklistDetail.toDiary(): DiaryAppActionChecklistDetail =
    DiaryAppActionChecklistDetail(
        checklist = checklist.toDiary(),
        items = items.map { item -> item.toDiary() },
        tags = tags.map { tag -> tag.toDiary() },
    )

internal fun AppActionChecklistSummary.toDiary(): DiaryAppActionChecklistSummary =
    DiaryAppActionChecklistSummary(
        id = id,
        title = title,
        checklistDescription = description,
        checkedCount = checkedCount,
        totalCount = totalCount,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

private fun AppActionChecklist.toDiary(): DiaryAppActionChecklist =
    DiaryAppActionChecklist(
        id = id,
        title = title,
        checklistDescription = description,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

private fun AppActionChecklistItem.toDiary(): DiaryAppActionChecklistItem =
    DiaryAppActionChecklistItem(
        id = id,
        content = content,
        isChecked = isChecked,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )
