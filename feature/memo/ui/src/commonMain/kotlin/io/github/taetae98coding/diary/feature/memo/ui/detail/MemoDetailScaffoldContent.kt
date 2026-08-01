package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoForm
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCardUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailScaffoldContent(
    onEvent: (MemoFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoDetailUiState = { MemoDetailUiState.Loading },
    state: MemoFormState = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
    placeMapState: DiaryMapState? = null,
    isStandalone: Boolean = true,
    tagUiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
    webUiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
    placeCardUiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is MemoDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            MemoForm(
                onEvent = onEvent,
                state = state,
                placeMapState = placeMapState,
                isStandalone = isStandalone,
                tagUiStateProvider = tagUiStateProvider,
                webUiStateProvider = webUiStateProvider,
                placeCardUiStateProvider = placeCardUiStateProvider,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
    }
}

@ScreenPreview
@Composable
private fun MemoDetailScaffoldContentPreview() {
    DiaryTheme {
        Surface {
            MemoDetailScaffoldContent(
                onEvent = {},
                uiStateProvider = {
                    MemoDetailUiState.Content(
                        id = Uuid.NIL,
                        detail = MemoDetail.EMPTY.copy(title = "메모 제목"),
                        isFinished = false,
                    )
                },
            )
        }
    }
}
