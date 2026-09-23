package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.compose.core.icon.CalendarIcon
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.icon.MoreIcon
import io.github.taetae98coding.diary.compose.core.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.core.icon.TagIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

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
