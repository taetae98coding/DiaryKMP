package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction

public data class DiaryAppActionMemoPage(
    val items: List<DiaryAppActionMemo>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionContactMemoPage(
    val items: List<DiaryAppActionMemo>,
    val nextPageNumber: Int?,
)

public object DiaryMemoListAppAction {
    public suspend fun list(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> memoListAppAction().list(sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun finishedList(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> memoListAppAction().finishedList(sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun placeList(
        placeId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> memoListAppAction().placeList(placeId = placeId, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun placeFinishedList(
        placeId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> memoListAppAction().placeFinishedList(placeId = placeId, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun webList(
        webId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> memoListAppAction().webList(webId = webId, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun webFinishedList(
        webId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> memoListAppAction().webFinishedList(webId = webId, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun contactList(
        contactId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionContactMemoPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> memoListAppAction().contactList(contactId = contactId, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionContactMemoPage(items = page.items.map { memo -> memo.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun contactFinishedList(
        contactId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionContactMemoPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> memoListAppAction().contactFinishedList(contactId = contactId, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionContactMemoPage(items = page.items.map { memo -> memo.toDiary() }, nextPageNumber = page.nextPageNumber)
    }
}

private fun memoListAppAction(): MemoListAppAction = diaryAppAction()

internal suspend fun readMemoPage(
    pageNumber: Int,
    read: suspend (cursor: String?) -> AppActionPage<AppActionMemo>,
): DiaryAppActionMemoPage {
    val page = readNumberedPage(pageNumber = pageNumber, read = read)

    return DiaryAppActionMemoPage(items = page.items.map { memo -> memo.toDiary() }, nextPageNumber = page.nextPageNumber)
}
