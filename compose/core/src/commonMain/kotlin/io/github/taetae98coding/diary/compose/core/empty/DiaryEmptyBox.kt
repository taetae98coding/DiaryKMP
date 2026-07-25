package io.github.taetae98coding.diary.compose.core.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholder
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public const val DIARY_EMPTY_BOX_TEST_TAG: String = "DiaryEmptyBox"

@Composable
public fun DiaryEmptyBox(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: @Composable () -> Unit,
) {
    DiaryPlaceholder(
        icon = icon,
        message = {
            Column(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            ) {
                Text(
                    text = title,
                    style = DiaryTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = DiaryTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        modifier = modifier.testTag(DIARY_EMPTY_BOX_TEST_TAG),
    )
}

@ScreenPreview
@Composable
private fun DiaryEmptyBoxPreview() {
    DiaryTheme {
        Surface {
            DiaryEmptyBox(
                title = "아직 메모가 없습니다",
                description = "추가 버튼으로 새 메모를 만들 수 있습니다",
                icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}
