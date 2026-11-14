package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction

public data class DiaryAppActionSearchTagPage(
    val items: List<DiaryAppActionTag>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionSearchPlacePage(
    val items: List<DiaryAppActionPlace>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionSearchWebPage(
    val items: List<DiaryAppActionWeb>,
    val nextPageNumber: Int?,
)

public object DiarySearchAppAction {
    public suspend fun memo(
        query: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> searchAppAction().memo(query = query, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun tag(
        query: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionSearchTagPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> searchAppAction().tag(query = query, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionSearchTagPage(items = page.items.map { tag -> tag.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun place(
        query: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionSearchPlacePage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> searchAppAction().place(query = query, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionSearchPlacePage(items = page.items.map { place -> place.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun web(
        query: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionSearchWebPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> searchAppAction().web(query = query, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionSearchWebPage(items = page.items.map { web -> web.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun dday(
        query: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionDDayPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> searchAppAction().dday(query = query, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionDDayPage(items = page.items.map { dday -> dday.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun routine(
        query: String,
        pageNumber: Int,
    ): DiaryAppActionRoutinePage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> searchAppAction().routine(query = query, cursor = cursor) }

        return DiaryAppActionRoutinePage(items = page.items.map { routine -> routine.toDiary() }, nextPageNumber = page.nextPageNumber)
    }
}

private fun searchAppAction(): SearchAppAction = diaryAppAction()
