package io.github.taetae98coding.diary.feature.memo.ui.detail.share

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.memo.toDisplayText
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.toMemoDateTime

@Composable
internal fun MemoFormState.sharePeriodText(): String =
    dateTimeState.value
        .toMemoDateTime()
        ?.toDisplayText()
        .orEmpty()
