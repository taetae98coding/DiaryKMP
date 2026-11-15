package io.github.taetae98coding.diary.feature.memo.ui.detail.share

import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.shareTextOf
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.share.memoICalendarOf
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun MemoDetail.shareTitle(savedTitle: String): String = title.trim().ifEmpty { savedTitle.trim() }

internal fun memoShareTextOf(
    detail: MemoDetail,
    savedTitle: String,
    periodText: String,
): ShareContent.Text {
    val title = detail.shareTitle(savedTitle = savedTitle)

    return ShareContent.Text(
        text = shareTextOf(title, periodText, detail.description),
        title = title,
    )
}

internal fun memoShareCalendarFileOf(
    id: Uuid,
    detail: MemoDetail,
    dateTime: MemoDateTime,
    savedTitle: String,
    timeZone: TimeZone,
    sharedAt: Instant,
): ShareContent.File {
    val title = detail.shareTitle(savedTitle = savedTitle)
    val calendar =
        memoICalendarOf(
            id = id,
            title = title,
            description = detail.description,
            dateTime = dateTime,
            timeZone = timeZone,
            sharedAt = sharedAt,
        )

    return ShareContent.File(
        bytes = calendar.encodeToByteArray(),
        name = title,
        type = ShareFileType.CALENDAR,
        title = title,
    )
}
