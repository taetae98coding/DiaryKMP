package io.github.taetae98coding.diary.feature.tag.ui.home.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_filter_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_filter_top_level_only_label
import org.jetbrains.compose.resources.stringResource

private val CONTENT_HORIZONTAL_PADDING = 24.dp
private val CONTENT_BOTTOM_PADDING = 16.dp
private val ROW_MIN_HEIGHT = 48.dp

@Composable
internal fun TagHomeFilterBottomSheetContent(
    onEvent: (TagHomeFilterBottomSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagHomeFilterUiState = { TagHomeFilterUiState() },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.tag_home_filter_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING, vertical = 12.dp),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = CONTENT_BOTTOM_PADDING),
        ) {
            TopLevelOnlyRow(
                onCheckedChange = { isTopLevelOnly -> onEvent(TagHomeFilterBottomSheetEvent.SetTopLevelOnly(isTopLevelOnly = isTopLevelOnly)) },
                modifier = Modifier.fillMaxWidth(),
                isTopLevelOnlyProvider = { uiStateProvider().isTopLevelOnly },
            )
        }
    }
}

@Composable
private fun TopLevelOnlyRow(
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isTopLevelOnlyProvider: () -> Boolean = { false },
) {
    val isTopLevelOnly = isTopLevelOnlyProvider()

    Row(
        // 누름 배경이 좌우 여백까지 채우도록 여백을 toggleable 안쪽에 둔다.
        modifier =
            modifier
                .heightIn(min = ROW_MIN_HEIGHT)
                .toggleable(
                    value = isTopLevelOnly,
                    onValueChange = onCheckedChange,
                    role = Role.Switch,
                ).padding(horizontal = CONTENT_HORIZONTAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.tag_home_filter_top_level_only_label),
            modifier = Modifier.weight(1F),
            style = DiaryTheme.typography.bodyLarge,
        )

        Switch(
            checked = isTopLevelOnly,
            onCheckedChange = null,
        )
    }
}

@ScreenPreview
@Composable
private fun TagHomeFilterBottomSheetContentPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isTopLevelOnly: Boolean,
) {
    DiaryTheme {
        TagHomeFilterBottomSheetContent(
            onEvent = {},
            uiStateProvider = { TagHomeFilterUiState(isTopLevelOnly = isTopLevelOnly) },
        )
    }
}
