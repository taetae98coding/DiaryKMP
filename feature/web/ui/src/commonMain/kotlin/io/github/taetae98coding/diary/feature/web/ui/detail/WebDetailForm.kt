package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.web.ui.form.WebForm
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail

internal const val WEB_DETAIL_FORM_TEST_TAG: String = "WebDetailForm"

@Composable
internal fun WebDetailForm(
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebFormState = rememberWebDetailFormState(),
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    Box(modifier = modifier.testTag(WEB_DETAIL_FORM_TEST_TAG)) {
        WebForm(
            onEvent = onFormEvent,
            modifier = Modifier.fillMaxSize(),
            state = state,
            tagUiStateProvider = tagUiStateProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun WebDetailFormPreview() {
    DiaryTheme {
        Surface {
            WebDetailForm(
                onFormEvent = {},
                modifier = Modifier.fillMaxSize(),
                state = rememberWebDetailFormState(initialDetail = previewWebDetail()),
            )
        }
    }
}
