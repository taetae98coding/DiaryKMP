package io.github.taetae98coding.diary.compose.routine

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholder
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ScreenPreview
import org.jetbrains.compose.resources.stringResource

private const val FINISHED_ROUTINE_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG: String = "finishedRoutineListDetailPlaceholderIcon"

@Composable
public fun FinishedRoutineListDetailPlaceholder(modifier: Modifier = Modifier) {
    DiaryPlaceholder(
        icon = {
            RoutineIcon(
                modifier =
                    Modifier
                        .size(DiaryPlaceholderDefaults.IconSize)
                        .testTag(FINISHED_ROUTINE_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG),
            )
        },
        message = { Text(text = stringResource(Res.string.routine_finished_list_detail_placeholder_message)) },
        modifier = modifier,
    )
}

@ScreenPreview
@Composable
private fun FinishedRoutineListDetailPlaceholderPreview() {
    DiaryTheme {
        FinishedRoutineListDetailPlaceholder()
    }
}
