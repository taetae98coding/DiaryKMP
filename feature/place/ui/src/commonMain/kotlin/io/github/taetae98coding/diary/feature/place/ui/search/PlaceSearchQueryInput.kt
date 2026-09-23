package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import io.github.taetae98coding.diary.compose.core.icon.SearchIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_search_clear_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_search_query_input_content_description
import io.github.taetae98coding.diary.feature.place.ui.place_search_query_input_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceSearchQueryInput(
    state: PlaceSearchDialogState,
    modifier: Modifier = Modifier,
) {
    val queryContentDescription = stringResource(Res.string.place_search_query_input_content_description)

    Card(modifier = modifier) {
        ClearTextField(
            state = state.queryState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester)
                    .semantics { contentDescription = queryContentDescription },
            placeholder = { Text(text = stringResource(Res.string.place_search_query_input_placeholder)) },
            leadingIcon = { SearchIcon() },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            lineLimits = TextFieldLineLimits.SingleLine,
            clearButtonContentDescription = stringResource(Res.string.place_search_clear_button_content_description),
        )
    }
}

@ComponentPreview
@Composable
private fun PlaceSearchQueryInputPreview() {
    DiaryTheme {
        Surface {
            PlaceSearchQueryInput(state = rememberPlaceSearchDialogState(initialProvider = DiaryMapProvider.NAVER))
        }
    }
}
