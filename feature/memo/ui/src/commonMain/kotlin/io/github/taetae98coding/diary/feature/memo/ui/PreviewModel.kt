package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

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
    latitude: Double,
    longitude: Double,
): Place =
    Place(
        id = Uuid.random(),
        detail =
            PlaceDetail(
                title = title,
                description = "",
                color = color,
                coordinate = Coordinate(latitude = latitude, longitude = longitude),
                address = "",
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

internal fun previewMemoDraft(): MemoDraft =
    MemoDraft(
        title = "주간 회고 정리",
        description = "## 이번 주\n- 한 일\n- 배운 것",
        dateTime = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 9, day = 21)..LocalDate(year = 2026, month = 9, day = 22)),
    )

internal class TagListPreviewParameter : PreviewParameterProvider<List<Tag>> {
    override val values: Sequence<List<Tag>> =
        sequenceOf(
            emptyList(),
            listOf(
                previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                previewTag(emoji = "🏃", title = "운동", color = 0xFFE57373),
                previewTag(emoji = "", title = "공부", color = 0xFF81C784),
            ),
        )
}
