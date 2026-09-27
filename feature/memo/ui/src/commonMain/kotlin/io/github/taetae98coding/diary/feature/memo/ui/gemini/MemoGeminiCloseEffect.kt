package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.uuid.Uuid

@Composable
internal fun MemoGeminiCloseEffect(
    targetId: Uuid?,
    geminiViewModel: MemoGeminiViewModel,
) {
    // 화면이 재생성되면 Effect가 다시 시작되므로, 시작 여부가 아니라 저장해 둔 이전 대상과 비교해 대상이 바뀐 때만 닫는다.
    var lastTargetId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(targetId, geminiViewModel) {
        val id = targetId?.toString() ?: return@LaunchedEffect

        if (lastTargetId != null && lastTargetId != id) {
            geminiViewModel.close()
        }
        lastTargetId = id
    }
}
