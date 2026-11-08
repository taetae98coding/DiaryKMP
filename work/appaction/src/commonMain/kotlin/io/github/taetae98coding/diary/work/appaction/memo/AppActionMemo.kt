package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb

public data class AppActionMemo(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val dateTime: AppActionMemoDateTime?,
    val primaryTagId: String?,
    val isFinished: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

public data class AppActionMemoDetail(
    val memo: AppActionMemo,
    val tags: List<AppActionTag>,
    val places: List<AppActionPlace>,
    val webs: List<AppActionWeb>,
    val contacts: List<AppActionContact>,
)

internal fun Memo.toAppActionMemo(): AppActionMemo =
    AppActionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.toColorHex(),
        dateTime = detail.dateTime?.toAppActionMemoDateTime(),
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
