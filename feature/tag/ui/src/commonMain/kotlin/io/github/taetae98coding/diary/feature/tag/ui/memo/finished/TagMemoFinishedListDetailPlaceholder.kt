package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

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
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_detail_placeholder_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagMemoFinishedListDetailPlaceholder(modifier: Modifier = Modifier) {
    DiaryPlaceholder(
        icon = {
            MemoIcon(
                modifier =
                    Modifier
                        .size(DiaryPlaceholderDefaults.IconSize)
                        .testTag(TAG_MEMO_FINISHED_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG),
            )
        },
        message = { Text(text = stringResource(Res.string.tag_memo_finished_list_detail_placeholder_message)) },
        modifier = modifier,
    )
}

@ScreenPreview
@Composable
private fun TagMemoFinishedListDetailPlaceholderPreview() {
    DiaryTheme {
        TagMemoFinishedListDetailPlaceholder()
    }
}

internal const val TAG_MEMO_FINISHED_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG: String = "tagMemoFinishedListDetailPlaceholderIcon"
