package io.github.taetae98coding.diary.feature.core.memo.finished

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholder
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.core.Res
import io.github.taetae98coding.diary.feature.core.entity_memo_finished_list_detail_placeholder_message
import org.jetbrains.compose.resources.stringResource

@Composable
public fun EntityMemoFinishedListDetailPlaceholder(modifier: Modifier = Modifier) {
    DiaryPlaceholder(
        icon = {
            MemoIcon(
                modifier =
                    Modifier
                        .size(DiaryPlaceholderDefaults.IconSize)
                        .testTag(ENTITY_MEMO_FINISHED_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG),
            )
        },
        message = { Text(text = stringResource(Res.string.entity_memo_finished_list_detail_placeholder_message)) },
        modifier = modifier,
    )
}

@ScreenPreview
@Composable
private fun EntityMemoFinishedListDetailPlaceholderPreview() {
    DiaryTheme {
        EntityMemoFinishedListDetailPlaceholder()
    }
}

public const val ENTITY_MEMO_FINISHED_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG: String = "entityMemoFinishedListDetailPlaceholderIcon"
