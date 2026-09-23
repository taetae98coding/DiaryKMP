package io.github.taetae98coding.diary.compose.map.provider

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState

@Composable
internal fun DiaryMapProviderTabRow(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    val providerContentDescription = diaryMapProviderContentDescription()

    PrimaryTabRow(
        selectedTabIndex = diaryMapProviderList.indexOf(state.provider),
        modifier = modifier.semantics { contentDescription = providerContentDescription },
    ) {
        diaryMapProviderList.forEach { provider ->
            Tab(
                selected = provider == state.provider,
                onClick = { state.select(provider) },
                text = { Text(text = provider.label()) },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryMapProviderTabRowPreview() {
    DiaryTheme {
        DiaryMapProviderTabRow()
    }
}
