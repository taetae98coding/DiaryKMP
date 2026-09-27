package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

// 길게 누르기를 받지 않으면 손을 뗄 때 누름으로 처리되므로, 길게 누르기를 비워 두어 날짜 선택과 메모 이동에만 쓰이게 한다.
internal fun Modifier.calendarClick(onClick: () -> Unit): Modifier =
    combinedClickable(
        role = Role.Button,
        hapticFeedbackEnabled = false,
        onLongClick = {},
        onClick = onClick,
    )
