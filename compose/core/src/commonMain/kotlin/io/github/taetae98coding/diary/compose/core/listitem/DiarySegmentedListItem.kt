@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.compose.core.listitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiarySegmentedListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    count: Int = 1,
    content: @Composable () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = DiarySegmentedListItemDefaults.shapes(index = index, count = count),
        modifier = modifier.fillMaxWidth(),
        colors = DiarySegmentedListItemDefaults.colors(),
        content = content,
    )
}

// Material 3의 SegmentedListItem은 클릭·선택·체크 오버로드만 있어, 누를 수 없는 항목은 같은 모양과 색으로 직접 그린다.
@Composable
public fun DiaryStaticSegmentedListItem(
    modifier: Modifier = Modifier,
    index: Int = 0,
    count: Int = 1,
    content: @Composable () -> Unit,
) {
    val colors = DiarySegmentedListItemDefaults.colors()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DiarySegmentedListItemDefaults.shapes(index = index, count = count).shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
    ) {
        Box(
            modifier =
                Modifier
                    .defaultMinSize(minHeight = DiarySegmentedListItemDefaults.MinHeight)
                    .padding(ListItemDefaults.ContentPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            content()
        }
    }
}

@Composable
public fun DiarySelectableSegmentedListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    index: Int = 0,
    count: Int = 1,
    content: @Composable () -> Unit,
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = DiarySegmentedListItemDefaults.shapes(index = index, count = count),
        modifier = modifier.fillMaxWidth(),
        colors = DiarySegmentedListItemDefaults.colors(),
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        content = content,
    )
}

@Composable
public fun DiaryCheckableSegmentedListItem(
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    index: Int = 0,
    count: Int = 1,
    supportingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = DiarySegmentedListItemDefaults.shapes(index = index, count = count),
        modifier = modifier.fillMaxWidth(),
        colors = DiarySegmentedListItemDefaults.colors(),
        leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
        supportingContent = supportingContent,
        content = content,
    )
}

@ComponentPreview
@Composable
private fun DiarySegmentedListItemPreview() {
    DiaryTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap)) {
                listOf("공휴일", "지도", "Gemini").forEachIndexed { index, label ->
                    DiarySegmentedListItem(
                        onClick = {},
                        index = index,
                        count = 3,
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryStaticSegmentedListItemPreview() {
    DiaryTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap)) {
                listOf("http://192.168.0.10:27180", "http://10.0.0.5:27180").forEachIndexed { index, label ->
                    DiaryStaticSegmentedListItem(
                        index = index,
                        count = 2,
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiarySelectableSegmentedListItemPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFirstSelected: Boolean,
) {
    DiaryTheme {
        Surface {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
            ) {
                DiarySelectableSegmentedListItem(
                    onClick = {},
                    selected = isFirstSelected,
                    index = 0,
                    count = 2,
                ) {
                    Text(text = "네이버")
                }

                DiarySelectableSegmentedListItem(
                    onClick = {},
                    selected = !isFirstSelected,
                    index = 1,
                    count = 2,
                ) {
                    Text(text = "Google")
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryCheckableSegmentedListItemPreview() {
    DiaryTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap)) {
                DiaryCheckableSegmentedListItem(
                    onCheckedChange = {},
                    checked = true,
                    index = 0,
                    count = 2,
                    supportingContent = { Text(text = "한국") },
                ) {
                    Text(text = "기기값")
                }

                DiaryCheckableSegmentedListItem(
                    onCheckedChange = {},
                    index = 1,
                    count = 2,
                ) {
                    Text(text = "미국")
                }
            }
        }
    }
}
