package io.github.taetae98coding.diary.feature.core.memo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.ListEntryButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.core.Res
import io.github.taetae98coding.diary.feature.core.entity_detail_memo_finished_list_action_label
import org.jetbrains.compose.resources.stringResource

@Composable
public fun EntityDetailMemoFinishedListButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListEntryButton(
        onClick = onClick,
        label = stringResource(Res.string.entity_detail_memo_finished_list_action_label),
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun EntityDetailMemoFinishedListButtonPreview() {
    DiaryTheme {
        EntityDetailMemoFinishedListButton(onClick = {})
    }
}
