package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.FinishButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_finish_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_restart_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailTopBarActions(
    contentProvider: () -> TagDetailUiState.Content,
    onEvent: (TagDetailScaffoldEvent) -> Unit,
) {
    FinishButton(
        isFinishedProvider = { contentProvider().isFinished },
        onClick = {
            if (contentProvider().isFinished) {
                onEvent(TagDetailScaffoldEvent.ClickRestart)
            } else {
                onEvent(TagDetailScaffoldEvent.ClickFinish)
            }
        },
        contentDescription =
            if (contentProvider().isFinished) {
                stringResource(Res.string.tag_detail_restart_button_content_description)
            } else {
                stringResource(Res.string.tag_detail_finish_button_content_description)
            },
        isInProgressProvider = { contentProvider().isFinishInProgress },
    )
    DeleteButton(
        onClick = { onEvent(TagDetailScaffoldEvent.ClickDelete) },
        contentDescription = stringResource(Res.string.tag_detail_delete_button_content_description),
        isInProgressProvider = { contentProvider().isDeleteInProgress },
    )
}

@ComponentPreview
@Composable
private fun TagDetailTopBarActionsPreview() {
    DiaryTheme {
        Surface {
            Row {
                TagDetailTopBarActions(
                    contentProvider = {
                        TagDetailUiState.Content(
                            id = Uuid.NIL,
                            detail = TagDetail.EMPTY.copy(title = "태그 제목"),
                            isFinished = false,
                        )
                    },
                    onEvent = {},
                )
            }
        }
    }
}
