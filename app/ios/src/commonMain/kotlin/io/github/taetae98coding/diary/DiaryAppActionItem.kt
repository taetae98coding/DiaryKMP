package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb

// Swift export는 NSObject.description과 겹치는 `description` 멤버를 내보내지 않으므로 종류 이름을 붙인다.
public data class DiaryAppActionTag(
    val id: String,
    val emoji: String,
    val title: String,
    val tagDescription: String,
    val color: String,
    val isFinished: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionPlace(
    val id: String,
    val title: String,
    val placeDescription: String,
    val color: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionWeb(
    val id: String,
    val title: String,
    val webDescription: String,
    val url: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionContact(
    val id: String,
    val name: String,
    val nameQualifier: String,
    val contactDescription: String,
    val isFavorite: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

// 검색 동작이 없는 종류의 선택 목록 검색은 첫 페이지만 거른다.
internal const val SEARCH_PAGE_NUMBER: Int = 1

internal fun AppActionTag.toDiary(): DiaryAppActionTag =
    DiaryAppActionTag(
        id = id,
        emoji = emoji,
        title = title,
        tagDescription = description,
        color = color,
        isFinished = isFinished,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

internal fun AppActionPlace.toDiary(): DiaryAppActionPlace =
    DiaryAppActionPlace(
        id = id,
        title = title,
        placeDescription = description,
        color = color,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

internal fun AppActionWeb.toDiary(): DiaryAppActionWeb =
    DiaryAppActionWeb(
        id = id,
        title = title,
        webDescription = description,
        url = url,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

internal fun AppActionContact.toDiary(): DiaryAppActionContact =
    DiaryAppActionContact(
        id = id,
        name = name,
        nameQualifier = nameQualifier,
        contactDescription = description,
        isFavorite = isFavorite,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )
