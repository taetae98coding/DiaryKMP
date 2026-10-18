package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarAction
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarNavigateUp
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarTitle
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_home_search_action_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_home_title
import io.github.taetae98coding.diary.feature.web.ui.web_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebHomeTopBar(
    onEvent: (WebHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(Res.string.web_home_title)
    val navigateUpContentDescription = stringResource(Res.string.web_navigate_up_button_content_description)
    val searchContentDescription = stringResource(Res.string.web_home_search_action_content_description)

    DiaryTopBar(
        modifier = modifier,
        title = DiaryTopBarTitle.Text(text = title),
        navigateUp =
            DiaryTopBarNavigateUp(
                contentDescription = navigateUpContentDescription,
                onClick = { onEvent(WebHomeScaffoldEvent.ClickNavigateUp) },
            ),
        actions =
            listOf(
                DiaryTopBarAction.Icon(
                    icon = DiaryTopBarIcon.SEARCH,
                    contentDescription = searchContentDescription,
                    onClick = { onEvent(WebHomeScaffoldEvent.ClickSearch) },
                ),
            ),
    )
}

@ComponentPreview
@Composable
private fun WebHomeTopBarPreview() {
    DiaryTheme {
        WebHomeTopBar(onEvent = {})
    }
}
