@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.DiarySearchInputField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.ui.Res
import io.github.taetae98coding.diary.feature.search.ui.search_home_query_input_placeholder
import io.github.taetae98coding.diary.feature.search.ui.search_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

internal const val SEARCH_HOME_QUERY_INPUT_TEST_TAG: String = "SearchHomeQueryInput"

@Composable
internal fun SearchHomeTopBar(
    onEvent: (SearchHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SearchHomeScaffoldState = rememberSearchHomeScaffoldState(),
) {
    TopAppBar(
        title = {
            DiarySearchInputField(
                placeholder = stringResource(Res.string.search_home_query_input_placeholder),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG),
                state = state.queryState,
                focusRequester = state.focusRequester,
            )
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(SearchHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.search_navigate_up_button_content_description),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun SearchHomeTopBarPreview() {
    DiaryTheme {
        SearchHomeTopBar(onEvent = {})
    }
}
