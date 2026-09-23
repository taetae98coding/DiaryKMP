package io.github.taetae98coding.diary.compose.memo.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
public class MemoListState(
    initialToday: LocalDate? = null,
) {
    public var today: LocalDate? by mutableStateOf(initialToday)
        private set

    public fun updateToday() {
        today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
}

@Composable
public fun rememberMemoListState(initialToday: LocalDate? = null): MemoListState = remember { MemoListState(initialToday = initialToday) }
