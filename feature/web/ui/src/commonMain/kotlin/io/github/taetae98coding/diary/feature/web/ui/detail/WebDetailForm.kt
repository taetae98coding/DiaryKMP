package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.form.WebForm
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.web_detail_update_button_content_description
import org.jetbrains.compose.resources.stringResource

internal const val WEB_DETAIL_FORM_TEST_TAG: String = "WebDetailForm"

@Composable
internal fun WebDetailForm(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebFormState = rememberWebDetailFormState(),
    isChangedProvider: () -> Boolean = { false },
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    Box(modifier = modifier.testTag(WEB_DETAIL_FORM_TEST_TAG)) {
        WebForm(
            onEvent = onFormEvent,
            modifier = Modifier.fillMaxSize(),
            state = state,
            tagUiStateProvider = tagUiStateProvider,
        )

        DiaryScaleVisibility(
            visible = isChangedProvider(),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(DiaryTheme.dimens.screenPaddingValues),
        ) {
            FloatingCheckButton(
                onClick = { onEvent(WebDetailScaffoldEvent.ClickUpdate) },
                contentDescription = stringResource(Res.string.web_detail_update_button_content_description),
                isInProgressProvider = { (uiStateProvider() as? WebDetailUiState.Content)?.isUpdateInProgress == true },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun WebDetailFormPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isChanged: Boolean,
) {
    DiaryTheme {
        Surface {
            WebDetailForm(
                onEvent = {},
                onFormEvent = {},
                modifier = Modifier.fillMaxSize(),
                state = rememberWebDetailFormState(initialDetail = previewWebDetail()),
                isChangedProvider = { isChanged },
            )
        }
    }
}
