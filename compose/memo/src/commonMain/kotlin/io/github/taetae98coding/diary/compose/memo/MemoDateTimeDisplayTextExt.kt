package io.github.taetae98coding.diary.compose.memo

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime

@Composable
internal fun MemoDateTime.toDisplayText(): String =
    when (this) {
        is MemoDateTime.AllDay -> {
            if (dateRange.start == dateRange.endInclusive) {
                dateRange.start.toDisplayText()
            } else {
                "${dateRange.start.toDisplayText()} ~ ${dateRange.endInclusive.toDisplayText()}"
            }
        }

        is MemoDateTime.DateTime -> {
            val startText = "${start.date.toDisplayText()} ${start.time.toDisplayText()}"

            if (start.date == endInclusive.date) {
                "$startText ~ ${endInclusive.time.toDisplayText()}"
            } else {
                "$startText ~ ${endInclusive.date.toDisplayText()} ${endInclusive.time.toDisplayText()}"
            }
        }
    }
