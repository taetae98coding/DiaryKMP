package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.place.ui.PREVIEW_PLACE_DETAIL
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceForm
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import kotlin.uuid.Uuid

@Composable
internal fun PlaceDetailScaffoldContent(
    onFormEvent: (PlaceFormEvent) -> Unit,
    state: PlaceFormState,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PlaceDetailUiState = { PlaceDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    // 조회 중과 내용 표시 사이에서만 전환한다. 내용이 갱신될 때마다 전환하면 입력 영역의 스크롤 위치가 처음으로 돌아간다.
    DiaryCrossfade(
        targetState = uiStateProvider(),
        modifier = modifier,
        contentKey = { uiState -> uiState::class },
    ) { uiState ->
        when (uiState) {
            is PlaceDetailUiState.Loading -> DiaryLoadingBox(modifier = Modifier.fillMaxSize())

            is PlaceDetailUiState.Content ->
                PlaceForm(
                    onEvent = onFormEvent,
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    isMapDisplayed = uiState.defaultProvider != null,
                    tagUiStateProvider = tagUiStateProvider,
                )
        }
    }
}

@ScreenPreview
@Composable
private fun PlaceDetailScaffoldContentPreview() {
    DiaryTheme {
        Surface {
            PlaceDetailScaffoldContent(
                onFormEvent = {},
                state = rememberPlaceDetailFormState(initialDetail = PREVIEW_PLACE_DETAIL),
                uiStateProvider = {
                    PlaceDetailUiState.Content(
                        id = Uuid.NIL,
                        detail = PREVIEW_PLACE_DETAIL,
                    )
                },
            )
        }
    }
}
