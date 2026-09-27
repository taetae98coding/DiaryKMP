package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private const val NO_FAILURE_ID = 0

// 웹 표시 영역은 탭이나 표시 방식을 바꾸면 컴포지션에서 빠지므로, 알린 실패 번호는 그 영역보다 오래 사는 화면이 가진다.
@Stable
public class DiaryWebSessionImportFailureState(
    notifiedFailureId: Int = NO_FAILURE_ID,
) {
    public var notifiedFailureId: Int by mutableIntStateOf(notifiedFailureId)
        private set

    internal fun isNotified(failureId: Int): Boolean = failureId == notifiedFailureId

    internal fun markNotified(failureId: Int) {
        notifiedFailureId = failureId
    }

    public companion object {
        public val Saver: Saver<DiaryWebSessionImportFailureState, Int> =
            Saver(
                save = { state -> state.notifiedFailureId },
                restore = { notifiedFailureId -> DiaryWebSessionImportFailureState(notifiedFailureId = notifiedFailureId) },
            )
    }
}

@Composable
public fun rememberDiaryWebSessionImportFailureState(): DiaryWebSessionImportFailureState =
    rememberSaveable(saver = DiaryWebSessionImportFailureState.Saver) {
        DiaryWebSessionImportFailureState()
    }
