package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQr
import io.github.taetae98coding.diary.work.appaction.qr.QrAppAction
import io.github.taetae98coding.diary.work.appaction.qr.filterBySearchQuery

public data class DiaryAppActionQr(
    val id: String,
    val title: String,
    val qrDescription: String,
    val value: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionQrPage(
    val items: List<DiaryAppActionQr>,
    val nextPageNumber: Int?,
)

public object DiaryQrAppAction {
    public suspend fun list(pageNumber: Int): DiaryAppActionQrPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> qrAppAction().list(cursor = cursor) }

        return DiaryAppActionQrPage(items = page.items.map { qr -> qr.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun search(query: String): List<DiaryAppActionQr> =
        readNumberedPage(pageNumber = SEARCH_PAGE_NUMBER) { cursor -> qrAppAction().list(cursor = cursor) }
            .items
            .filterBySearchQuery(query = query)
            .map { qr -> qr.toDiary() }

    public suspend fun get(qrId: String): DiaryAppActionQr? = qrAppAction().get(qrId = qrId)?.toDiary()

    public suspend fun add(
        title: String,
        qrDescription: String?,
        value: String,
    ): String = qrAppAction().add(title = title, description = qrDescription, value = value).id

    public suspend fun update(
        qrId: String,
        title: String?,
        qrDescription: String?,
        value: String?,
    ): Int = qrAppAction().update(qrId = qrId, title = title, description = qrDescription, value = value).changedCount

    public suspend fun delete(qrId: String): Int = qrAppAction().delete(qrId = qrId).changedCount

    public suspend fun restore(qrId: String): Int = qrAppAction().restore(qrId = qrId).changedCount
}

private fun qrAppAction(): QrAppAction = diaryAppAction()

private fun AppActionQr.toDiary(): DiaryAppActionQr =
    DiaryAppActionQr(
        id = id,
        title = title,
        qrDescription = description,
        value = value,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )
