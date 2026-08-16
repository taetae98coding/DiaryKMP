package io.github.taetae98coding.diary.feature.search.ui

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewMemo(
    title: String,
    color: Long,
    dateTime: MemoDateTime? = null,
): Memo =
    Memo(
        id = Uuid.random(),
        detail =
            MemoDetail(
                title = title,
                description = "",
                color = color,
                dateTime = dateTime,
            ),
        primaryTagId = null,
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewTag(
    emoji: String,
    title: String,
    color: Long,
): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = emoji, title = title, description = "", color = color),
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewPlace(
    title: String,
    color: Long,
    address: String,
): Place =
    Place(
        id = Uuid.random(),
        detail =
            PlaceDetail(
                title = title,
                description = "",
                color = color,
                coordinate = Coordinate(latitude = 37.5665, longitude = 126.9780),
                address = address,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewWeb(
    title: String,
    url: String,
): Web =
    Web(
        id = Uuid.random(),
        detail =
            WebDetail(
                title = title,
                description = "",
                url = url,
                headerList = emptyList(),
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
