package io.github.taetae98coding.diary.feature.memo.ui

import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime

internal fun DiaryDateTimeInputValue?.toMemoDateTime(): MemoDateTime? =
    when (this) {
        null -> null
        is DiaryDateTimeInputValue.AllDay -> MemoDateTime.AllDay(dateRange = dateRange)
        is DiaryDateTimeInputValue.DateTime -> MemoDateTime.DateTime(start = start, endInclusive = endInclusive)
    }

internal fun MemoDateTime?.toDiaryDateTimeInputValue(): DiaryDateTimeInputValue? =
    when (this) {
        null -> null
        is MemoDateTime.AllDay -> DiaryDateTimeInputValue.AllDay(dateRange = dateRange)
        is MemoDateTime.DateTime -> DiaryDateTimeInputValue.DateTime(start = start, endInclusive = endInclusive)
    }
