package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailFormArea(
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    formState: WebFormState = rememberWebDetailFormState(),
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is WebDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            WebDetailForm(
                onFormEvent = onFormEvent,
                modifier = Modifier.fillMaxSize(),
                state = formState,
                tagUiStateProvider = tagUiStateProvider,
            )
        } else {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
    }
}

@ComponentPreview
@Composable
private fun WebDetailFormAreaPreview() {
    DiaryTheme {
        Surface {
            WebDetailFormArea(
                onFormEvent = {},
                modifier = Modifier.fillMaxSize(),
                formState = rememberWebDetailFormState(initialDetail = previewWebDetail()),
                uiStateProvider = { WebDetailUiState.Content(id = Uuid.NIL, detail = previewWebDetail()) },
            )
        }
    }
}
