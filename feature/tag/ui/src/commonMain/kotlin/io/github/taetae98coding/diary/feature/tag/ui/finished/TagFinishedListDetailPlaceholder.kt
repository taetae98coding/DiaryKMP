package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.icon.TagIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholder
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_finished_list_detail_placeholder_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagFinishedListDetailPlaceholder(modifier: Modifier = Modifier) {
    DiaryPlaceholder(
        icon = {
            TagIcon(
                modifier =
                    Modifier
                        .size(DiaryPlaceholderDefaults.IconSize)
                        .testTag(TAG_FINISHED_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG),
            )
        },
        message = { Text(text = stringResource(Res.string.tag_finished_list_detail_placeholder_message)) },
        modifier = modifier,
    )
}

@ScreenPreview
@Composable
private fun TagFinishedListDetailPlaceholderPreview() {
    DiaryTheme {
        TagFinishedListDetailPlaceholder()
    }
}

internal const val TAG_FINISHED_LIST_DETAIL_PLACEHOLDER_ICON_TEST_TAG: String = "tagFinishedListDetailPlaceholderIcon"
