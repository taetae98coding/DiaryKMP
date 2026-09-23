package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.Res
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.top_level_navigation_calendar_label
import io.github.taetae98coding.diary.app.shared.top_level_navigation_memo_label
import io.github.taetae98coding.diary.app.shared.top_level_navigation_more_label
import io.github.taetae98coding.diary.app.shared.top_level_navigation_routine_label
import io.github.taetae98coding.diary.app.shared.top_level_navigation_tag_label
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TopLevelNavigationLabel(
    topLevelNavigation: TopLevelNavigation,
    modifier: Modifier = Modifier,
) {
    Text(
        text = topLevelNavigation.label(),
        modifier = modifier,
    )
}

@Composable
internal fun TopLevelNavigation.label(): String =
    when (this) {
        TopLevelNavigation.Memo -> stringResource(Res.string.top_level_navigation_memo_label)
        TopLevelNavigation.Tag -> stringResource(Res.string.top_level_navigation_tag_label)
        TopLevelNavigation.Calendar -> stringResource(Res.string.top_level_navigation_calendar_label)
        TopLevelNavigation.Routine -> stringResource(Res.string.top_level_navigation_routine_label)
        TopLevelNavigation.More -> stringResource(Res.string.top_level_navigation_more_label)
    }

@ComponentPreview
@Composable
private fun TopLevelNavigationLabelPreview() {
    DiaryTheme {
        Surface {
            TopLevelNavigationLabel(topLevelNavigation = TopLevelNavigation.Calendar)
        }
    }
}
