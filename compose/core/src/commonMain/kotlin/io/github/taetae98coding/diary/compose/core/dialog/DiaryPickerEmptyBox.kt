package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public const val DIARY_PICKER_EMPTY_BOX_TEST_TAG: String = "DiaryPickerEmptyBox"

@Composable
public fun DiaryPickerEmptyBox(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(DIARY_PICKER_EMPTY_BOX_TEST_TAG)
                .padding(vertical = DiaryTheme.dimens.screenVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            CompositionLocalProvider(LocalContentColor provides DiaryTheme.colorScheme.onSurfaceVariant) {
                Text(
                    text = title,
                    style = DiaryTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = description,
                    style = DiaryTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryPickerEmptyBoxPreview() {
    DiaryTheme {
        Surface {
            DiaryPickerEmptyBox(
                title = "검색 결과가 없습니다",
                description = "다른 검색어로 찾아보세요",
            )
        }
    }
}
