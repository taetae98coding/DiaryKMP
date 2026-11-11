package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagDetail
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagScopeValue
import io.github.taetae98coding.diary.work.appaction.tag.TagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction

public enum class DiaryAppActionTagScope {
    SELF,
    CHILD,
    DESCENDANT,
}

public data class DiaryAppActionTagPage(
    val items: List<DiaryAppActionTag>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionTagDetail(
    val tag: DiaryAppActionTag,
    val linkedTags: List<DiaryAppActionTag>,
)

public data class DiaryAppActionTagMemoPage(
    val items: List<DiaryAppActionMemo>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionTagWebPage(
    val items: List<DiaryAppActionWeb>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionTagPlacePage(
    val items: List<DiaryAppActionPlace>,
    val nextPageNumber: Int?,
)

public object DiaryTagAppAction {
    public suspend fun list(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionTagPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagAppAction().list(sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionTagPage(items = page.items.map { tag -> tag.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun finishedList(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionTagPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagAppAction().finishedList(sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionTagPage(items = page.items.map { tag -> tag.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun get(tagId: String): DiaryAppActionTagDetail? = tagAppAction().get(tagId = tagId)?.toDiary()

    public suspend fun getIncludingDeleted(tagId: String): DiaryAppActionTagDetail? = tagAppAction().getIncludingDeleted(tagId = tagId)?.toDiary()

    public suspend fun add(
        title: String,
        emoji: String?,
        tagDescription: String?,
        color: String?,
        linkedTagIds: List<String>,
    ): String = tagAppAction().add(title = title, emoji = emoji, description = tagDescription, color = color, linkedTagIds = linkedTagIds).id

    public suspend fun update(
        tagId: String,
        emoji: String?,
        title: String?,
        tagDescription: String?,
        color: String?,
    ): Int = tagAppAction().update(tagId = tagId, emoji = emoji, title = title, description = tagDescription, color = color).changedCount

    public suspend fun finish(tagId: String): Int = tagAppAction().finish(tagId = tagId).changedCount

    public suspend fun restart(tagId: String): Int = tagAppAction().restart(tagId = tagId).changedCount

    public suspend fun delete(tagId: String): Int = tagAppAction().delete(tagId = tagId).changedCount

    public suspend fun restore(tagId: String): Int = tagAppAction().restore(tagId = tagId).changedCount
}

public object DiaryTagLinkAppAction {
    public suspend fun add(
        tagId: String,
        linkedTagId: String,
    ): Boolean = tagLinkAppAction().add(tagId = tagId, linkedTagId = linkedTagId).done

    public suspend fun remove(
        tagId: String,
        linkedTagId: String,
    ): Boolean = tagLinkAppAction().remove(tagId = tagId, linkedTagId = linkedTagId).done
}

public object DiaryTagItemAppAction {
    public suspend fun memoList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionTagMemoPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().memoList(tagId = tagId, scope = scope.toScopeValue(), sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionTagMemoPage(items = page.items.map { memo -> memo.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun memoFinishedList(
        tagId: String,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionTagMemoPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().memoFinishedList(tagId = tagId, sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionTagMemoPage(items = page.items.map { memo -> memo.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun webList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionTagWebPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().webList(tagId = tagId, scope = scope.toScopeValue(), sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionTagWebPage(items = page.items.map { web -> web.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun placeList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionTagPlacePage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().placeList(tagId = tagId, scope = scope.toScopeValue(), sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionTagPlacePage(items = page.items.map { place -> place.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun contactList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionContactPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().contactList(tagId = tagId, scope = scope.toScopeValue(), sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionContactPage(items = page.items.map { contact -> contact.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun checklistList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        pageNumber: Int,
    ): DiaryAppActionChecklistSummaryPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().checklistList(tagId = tagId, scope = scope.toScopeValue(), cursor = cursor) }

        return DiaryAppActionChecklistSummaryPage(items = page.items.map { summary -> summary.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun musicList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMusicPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().musicList(tagId = tagId, scope = scope.toScopeValue(), sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionMusicPage(items = page.items.map { music -> music.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun qrList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        pageNumber: Int,
    ): DiaryAppActionQrPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().qrList(tagId = tagId, scope = scope.toScopeValue(), cursor = cursor) }

        return DiaryAppActionQrPage(items = page.items.map { qr -> qr.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun ddayList(
        tagId: String,
        scope: DiaryAppActionTagScope?,
        pageNumber: Int,
    ): DiaryAppActionDDayPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> tagItemAppAction().ddayList(tagId = tagId, scope = scope.toScopeValue(), cursor = cursor) }

        return DiaryAppActionDDayPage(items = page.items.map { dday -> dday.toDiary() }, nextPageNumber = page.nextPageNumber)
    }
}

private fun tagAppAction(): TagAppAction = diaryAppAction()

private fun tagLinkAppAction(): TagLinkAppAction = diaryAppAction()

private fun tagItemAppAction(): TagItemAppAction = diaryAppAction()

private fun AppActionTagDetail.toDiary(): DiaryAppActionTagDetail = DiaryAppActionTagDetail(tag = tag.toDiary(), linkedTags = linkedTags.map { linkedTag -> linkedTag.toDiary() })

private fun DiaryAppActionTagScope?.toScopeValue(): String? =
    when (this) {
        null -> null
        DiaryAppActionTagScope.SELF -> AppActionTagScopeValue.SELF
        DiaryAppActionTagScope.CHILD -> AppActionTagScopeValue.CHILD
        DiaryAppActionTagScope.DESCENDANT -> AppActionTagScopeValue.DESCENDANT
    }
