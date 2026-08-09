package io.github.taetae98coding.diary.compose.map

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun DiaryMapProviderSelector(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    val providerContentDescription = diaryMapProviderContentDescription()

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.semantics { contentDescription = providerContentDescription },
    ) {
        diaryMapProviderList.forEachIndexed { index, provider ->
            SegmentedButton(
                selected = provider == state.provider,
                onClick = { state.select(provider) },
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = diaryMapProviderList.size,
                    ),
                label = { Text(text = provider.label()) },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryMapProviderSelectorPreview() {
    DiaryTheme {
        DiaryMapProviderSelector()
    }
}
