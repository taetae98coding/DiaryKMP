package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.SearchButton
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
    TopAppBar(
        title = { Text(text = stringResource(Res.string.web_home_title)) },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(WebHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.web_navigate_up_button_content_description),
            )
        },
        actions = {
            SearchButton(
                onClick = { onEvent(WebHomeScaffoldEvent.ClickSearch) },
                contentDescription = stringResource(Res.string.web_home_search_action_content_description),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun WebHomeTopBarPreview() {
    DiaryTheme {
        WebHomeTopBar(onEvent = {})
    }
}
