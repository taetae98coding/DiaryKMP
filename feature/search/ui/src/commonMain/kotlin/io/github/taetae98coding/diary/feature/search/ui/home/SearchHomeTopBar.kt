package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarNavigateUp
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarTitle
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.ui.Res
import io.github.taetae98coding.diary.feature.search.ui.search_home_query_input_placeholder
import io.github.taetae98coding.diary.feature.search.ui.search_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchHomeTopBar(
    onEvent: (SearchHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SearchHomeScaffoldState = rememberSearchHomeScaffoldState(),
) {
    val placeholder = stringResource(Res.string.search_home_query_input_placeholder)
    val navigateUpContentDescription = stringResource(Res.string.search_navigate_up_button_content_description)

    DiaryTopBar(
        modifier = modifier,
        title =
            DiaryTopBarTitle.Search(
                state = state.queryState,
                placeholder = placeholder,
                focusRequester = state.focusRequester,
            ),
        navigateUp =
            DiaryTopBarNavigateUp(
                contentDescription = navigateUpContentDescription,
                onClick = { onEvent(SearchHomeScaffoldEvent.ClickNavigateUp) },
            ),
    )
}

@ComponentPreview
@Composable
private fun SearchHomeTopBarPreview() {
    DiaryTheme {
        SearchHomeTopBar(onEvent = {})
    }
}
