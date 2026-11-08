package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb

internal const val OPAQUE = 0xFF000000L
internal const val RGB = 0xFFFFFFL

internal fun Long.expectedHex(): String = "#%06X".format(this and RGB)

internal fun Tag.expected(): AppActionTag =
    AppActionTag(
        id = id.toString(),
        emoji = detail.emoji,
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

internal fun Memo.expected(): AppActionMemo =
    AppActionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        dateTime =
            when (val dateTime = detail.dateTime) {
                null -> null
                is MemoDateTime.AllDay -> AppActionMemoDateTime(allDay = true, start = dateTime.dateRange.start.toString(), endInclusive = dateTime.dateRange.endInclusive.toString())
                is MemoDateTime.DateTime -> AppActionMemoDateTime(allDay = false, start = dateTime.start.toString(), endInclusive = dateTime.endInclusive.toString())
            },
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

internal fun Web.expected(): AppActionWeb =
    AppActionWeb(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        url = detail.url,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

internal fun Place.expected(): AppActionPlace =
    AppActionPlace(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        address = detail.address,
        latitude = detail.coordinate.latitude.takeIf(Double::isFinite),
        longitude = detail.coordinate.longitude.takeIf(Double::isFinite),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
