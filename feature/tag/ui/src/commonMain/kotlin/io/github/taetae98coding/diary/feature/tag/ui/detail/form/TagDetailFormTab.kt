package io.github.taetae98coding.diary.feature.tag.ui.detail.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.form.TagForm
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInput
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailFormTab(
    onEvent: (TagDetailFormContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagDetailUiState = { TagDetailUiState.Loading },
    linkUiStateProvider: () -> TagLinkInputUiState = { TagLinkInputUiState() },
    state: TagFormState = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY),
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is TagDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            TagForm(
                state = state,
                modifier = Modifier.fillMaxSize(),
            ) {
                TagLinkInput(
                    onTagClick = { id -> onEvent(TagDetailFormContentEvent.ClickTag(id = id)) },
                    onLinkClick = { onEvent(TagDetailFormContentEvent.ClickLink) },
                    modifier = Modifier.fillMaxWidth(),
                    uiStateProvider = linkUiStateProvider,
                )
            }
        } else {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
    }
}

@ScreenPreview
@Composable
private fun TagDetailFormTabPreview() {
    val detail = TagDetail.EMPTY.copy(title = "태그 제목")

    DiaryTheme {
        Surface {
            TagDetailFormTab(
                onEvent = {},
                uiStateProvider = {
                    TagDetailUiState.Content(
                        id = Uuid.NIL,
                        detail = detail,
                        isFinished = false,
                    )
                },
                state = rememberTagDetailFormState(initialDetail = detail),
            )
        }
    }
}
