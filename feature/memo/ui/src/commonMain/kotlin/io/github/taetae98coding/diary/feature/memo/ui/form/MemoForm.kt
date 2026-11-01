package io.github.taetae98coding.diary.feature.memo.ui.form

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.layout.isCompactWidth
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toCoordinate
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInput
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCard
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCardUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceFillHeightCard
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInput
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInput
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState

@Composable
internal fun MemoForm(
    onEvent: (MemoFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoFormState = rememberMemoAddFormState(),
    placeMapState: DiaryMapState? = null,
    isStandalone: Boolean = true,
    tagUiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
    webUiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
    contactUiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
    placeCardUiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
) {
    val scrollState = rememberScrollState()

    if (isStandalone && !isCompactWidth()) {
        SplitForm(
            onEvent = onEvent,
            modifier = modifier,
            state = state,
            scrollState = scrollState,
            placeMapState = placeMapState,
            tagUiStateProvider = tagUiStateProvider,
            webUiStateProvider = webUiStateProvider,
            contactUiStateProvider = contactUiStateProvider,
            placeCardUiStateProvider = placeCardUiStateProvider,
        )
    } else {
        ColumnForm(
            onEvent = onEvent,
            modifier = modifier,
            state = state,
            scrollState = scrollState,
            placeMapState = placeMapState,
            tagUiStateProvider = tagUiStateProvider,
            webUiStateProvider = webUiStateProvider,
            contactUiStateProvider = contactUiStateProvider,
            placeCardUiStateProvider = placeCardUiStateProvider,
        )
    }
}

@Composable
private fun SplitForm(
    onEvent: (MemoFormEvent) -> Unit,
    state: MemoFormState,
    scrollState: ScrollState,
    placeMapState: DiaryMapState?,
    tagUiStateProvider: () -> MemoTagInputUiState,
    webUiStateProvider: () -> MemoWebInputUiState,
    contactUiStateProvider: () -> MemoContactInputUiState,
    placeCardUiStateProvider: () -> MemoPlaceCardUiState,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        MemoInputColumn(
            onEvent = onEvent,
            state = state,
            scrollState = scrollState,
            tagUiStateProvider = tagUiStateProvider,
            webUiStateProvider = webUiStateProvider,
            contactUiStateProvider = contactUiStateProvider,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1F),
            content = {},
        )
        MemoPlaceFillHeightCard(
            onPlaceClick = { id -> onEvent(MemoFormEvent.ClickPlace(id = id)) },
            onAddClick = { onEvent(MemoFormEvent.ClickPlaceAdd(coordinate = placeMapState?.coordinate?.toCoordinate())) },
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1F)
                    .padding(
                        top = DiaryTheme.dimens.screenVerticalPadding,
                        bottom = DiaryTheme.dimens.screenVerticalPadding,
                        end = DiaryTheme.dimens.screenHorizontalPadding,
                    ),
            mapState = placeMapState,
            uiStateProvider = placeCardUiStateProvider,
        )
    }
}

@Composable
private fun ColumnForm(
    onEvent: (MemoFormEvent) -> Unit,
    state: MemoFormState,
    scrollState: ScrollState,
    placeMapState: DiaryMapState?,
    tagUiStateProvider: () -> MemoTagInputUiState,
    webUiStateProvider: () -> MemoWebInputUiState,
    contactUiStateProvider: () -> MemoContactInputUiState,
    placeCardUiStateProvider: () -> MemoPlaceCardUiState,
    modifier: Modifier = Modifier,
) {
    MemoInputColumn(
        onEvent = onEvent,
        state = state,
        scrollState = scrollState,
        tagUiStateProvider = tagUiStateProvider,
        webUiStateProvider = webUiStateProvider,
        contactUiStateProvider = contactUiStateProvider,
        modifier = modifier.fillMaxSize(),
    ) {
        MemoPlaceCard(
            onPlaceClick = { id -> onEvent(MemoFormEvent.ClickPlace(id = id)) },
            onAddClick = { onEvent(MemoFormEvent.ClickPlaceAdd(coordinate = placeMapState?.coordinate?.toCoordinate())) },
            modifier = Modifier.fillMaxWidth(),
            mapState = placeMapState,
            uiStateProvider = placeCardUiStateProvider,
        )
    }
}

@Composable
private fun MemoInputColumn(
    onEvent: (MemoFormEvent) -> Unit,
    state: MemoFormState,
    scrollState: ScrollState,
    tagUiStateProvider: () -> MemoTagInputUiState,
    webUiStateProvider: () -> MemoWebInputUiState,
    contactUiStateProvider: () -> MemoContactInputUiState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DiaryInputColumn(
        modifier = modifier,
        scrollState = scrollState,
    ) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryColorInput(
            state = state.colorState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDateTimeInput(
            state = state.dateTimeState,
            modifier = Modifier.fillMaxWidth(),
        )
        MemoTagInput(
            uiStateProvider = tagUiStateProvider,
            onTagClick = { id -> onEvent(MemoFormEvent.ClickTag(id = id)) },
            onAddClick = { onEvent(MemoFormEvent.ClickTagAdd) },
            modifier = Modifier.fillMaxWidth(),
        )
        MemoWebInput(
            uiStateProvider = webUiStateProvider,
            onWebClick = { id -> onEvent(MemoFormEvent.ClickWeb(id = id)) },
            onAddClick = { onEvent(MemoFormEvent.ClickWebAdd) },
            modifier = Modifier.fillMaxWidth(),
        )
        MemoContactInput(
            uiStateProvider = contactUiStateProvider,
            onContactClick = { id -> onEvent(MemoFormEvent.ClickContact(id = id)) },
            onAddClick = { onEvent(MemoFormEvent.ClickContactAdd) },
            modifier = Modifier.fillMaxWidth(),
        )
        content()
    }
}

@ScreenPreview
@Composable
private fun MemoFormPreview() {
    DiaryTheme {
        Surface {
            MemoForm(onEvent = {})
        }
    }
}
