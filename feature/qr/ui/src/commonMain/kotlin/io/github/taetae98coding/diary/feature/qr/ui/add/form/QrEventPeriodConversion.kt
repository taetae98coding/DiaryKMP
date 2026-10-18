package io.github.taetae98coding.diary.feature.qr.ui.add.form

import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.domain.qr.content.QrEventPeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal fun DiaryDateTimeInputValue.toQrEventPeriod(): QrEventPeriod =
    when (this) {
        is DiaryDateTimeInputValue.AllDay -> QrEventPeriod.AllDay(start = dateRange.start, endInclusive = dateRange.endInclusive)
        is DiaryDateTimeInputValue.DateTime -> QrEventPeriod.DateTime(start = start, endInclusive = endInclusive)
    }

internal fun QrEventPeriod.toDiaryDateTimeInputValue(): DiaryDateTimeInputValue =
    when (this) {
        is QrEventPeriod.AllDay -> DiaryDateTimeInputValue.AllDay(dateRange = start..endInclusive)
        is QrEventPeriod.DateTime -> DiaryDateTimeInputValue.DateTime(start = start, endInclusive = endInclusive)
    }

internal fun Clock.todayEventPeriod(): QrEventPeriod {
    val today = now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return QrEventPeriod.AllDay(start = today, endInclusive = today)
}
