package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebWithHeaders
import io.github.taetae98coding.diary.work.appaction.web.WebAppAction

public data class DiaryAppActionWebPage(
    val items: List<DiaryAppActionWeb>,
    val nextPageNumber: Int?,
)

// 요청 헤더는 입력과 같은 "이름: 값" 글로 넘겨, 단축어에서 꺼낸 값을 그대로 웹 수정에 넘길 수 있게 한다.
public data class DiaryAppActionWebDetail(
    val web: DiaryAppActionWeb,
    val headers: List<String>,
    val tags: List<DiaryAppActionTag>,
)

public object DiaryWebAppAction {
    public suspend fun list(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionWebPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> webAppAction().list(sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionWebPage(items = page.items.map { web -> web.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun get(webId: String): DiaryAppActionWebDetail? =
        webAppAction().get(webId = webId)?.let { detail ->
            DiaryAppActionWebDetail(
                web = detail.web.toDiary(),
                headers = detail.web.headers.map { header -> "${header.name}: ${header.value}" },
                tags = detail.tags.map { tag -> tag.toDiary() },
            )
        }

    public suspend fun memoList(
        webId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> webAppAction().memoList(webId = webId, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun memoFinishedList(
        webId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMemoPage = readMemoPage(pageNumber = pageNumber) { cursor -> webAppAction().memoFinishedList(webId = webId, sort = sort.toSortValue(), cursor = cursor) }

    public suspend fun add(
        title: String,
        url: String,
        webDescription: String?,
        headers: List<String>,
        tagIds: List<String>,
    ): String = webAppAction().add(title = title, url = url, description = webDescription, headers = headers, tagIds = tagIds).id

    public suspend fun update(
        webId: String,
        title: String?,
        webDescription: String?,
        url: String?,
        headers: List<String>?,
    ): Int = webAppAction().update(webId = webId, title = title, description = webDescription, url = url, headers = headers).changedCount

    public suspend fun delete(webId: String): Int = webAppAction().delete(webId = webId).changedCount

    public suspend fun restore(webId: String): Int = webAppAction().restore(webId = webId).changedCount

    public suspend fun tagAdd(
        webId: String,
        tagId: String,
    ): Boolean = webAppAction().tagAdd(webId = webId, tagId = tagId).done

    public suspend fun tagRemove(
        webId: String,
        tagId: String,
    ): Boolean = webAppAction().tagRemove(webId = webId, tagId = tagId).done
}

private fun webAppAction(): WebAppAction = diaryAppAction()

private fun AppActionWebWithHeaders.toDiary(): DiaryAppActionWeb =
    DiaryAppActionWeb(
        id = id,
        title = title,
        webDescription = description,
        url = url,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )
