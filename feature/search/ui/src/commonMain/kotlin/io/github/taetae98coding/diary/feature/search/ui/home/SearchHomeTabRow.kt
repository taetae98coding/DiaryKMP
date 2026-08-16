package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.ui.Res
import io.github.taetae98coding.diary.feature.search.ui.search_home_tab_row_content_description
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchHomeTabRow(
    modifier: Modifier = Modifier,
    state: SearchHomeScaffoldState = rememberSearchHomeScaffoldState(),
) {
    val tabRowContentDescription = stringResource(Res.string.search_home_tab_row_content_description)
    val coroutineScope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = searchHomeTypeList.indexOf(state.type),
        modifier = modifier.semantics { contentDescription = tabRowContentDescription },
    ) {
        searchHomeTypeList.forEach { type ->
            Tab(
                selected = type == state.type,
                onClick = { coroutineScope.launch { state.select(type) } },
                text = { Text(text = stringResource(type.labelResource)) },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun SearchHomeTabRowPreview() {
    DiaryTheme {
        SearchHomeTabRow()
    }
}
