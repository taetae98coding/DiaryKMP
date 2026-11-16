package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.compose.icon.CalendarIcon
import io.github.taetae98coding.diary.compose.icon.MemoIcon
import io.github.taetae98coding.diary.compose.icon.MoreIcon
import io.github.taetae98coding.diary.compose.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.icon.TagIcon
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
internal fun TopLevelNavigationIcon(
    topLevelNavigation: TopLevelNavigation,
    modifier: Modifier = Modifier,
) {
    when (topLevelNavigation) {
        TopLevelNavigation.Memo -> {
            MemoIcon(
                modifier = modifier,
                contentDescription = topLevelNavigation.label(),
            )
        }

        TopLevelNavigation.Tag -> {
            TagIcon(
                modifier = modifier,
                contentDescription = topLevelNavigation.label(),
            )
        }

        TopLevelNavigation.Calendar -> {
            CalendarIcon(
                modifier = modifier,
                contentDescription = topLevelNavigation.label(),
            )
        }

        TopLevelNavigation.Routine -> {
            RoutineIcon(
                modifier = modifier,
                contentDescription = topLevelNavigation.label(),
            )
        }

        TopLevelNavigation.More -> {
            MoreIcon(
                modifier = modifier,
                contentDescription = topLevelNavigation.label(),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun TopLevelNavigationIconPreview() {
    DiaryTheme {
        Surface {
            TopLevelNavigationIcon(topLevelNavigation = TopLevelNavigation.Calendar)
        }
    }
}
