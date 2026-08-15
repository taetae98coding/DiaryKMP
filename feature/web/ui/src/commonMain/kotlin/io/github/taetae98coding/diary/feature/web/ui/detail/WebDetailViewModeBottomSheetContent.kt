package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_title
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_url_description
import org.jetbrains.compose.resources.stringResource

private val CONTENT_HORIZONTAL_PADDING = 24.dp
private val CONTENT_BOTTOM_PADDING = 16.dp
private val TITLE_VERTICAL_PADDING = 12.dp
private val ROW_MIN_HEIGHT = 56.dp
private val ROW_SPACING = 16.dp

@Composable
internal fun WebDetailViewModeBottomSheetContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.web_detail_view_mode_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING, vertical = TITLE_VERTICAL_PADDING),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(bottom = CONTENT_BOTTOM_PADDING),
        ) {
            webDetailViewModeList.forEach { item ->
                ViewModeRow(
                    onClick = { onEvent(WebDetailScaffoldEvent.SelectViewMode(viewMode = item)) },
                    modifier = Modifier.fillMaxWidth(),
                    viewMode = item,
                    isSelectedProvider = { item == state.viewMode },
                )
            }
        }
    }
}

@Composable
private fun ViewModeRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewMode: WebDetailViewMode = WebDetailViewMode.URL,
    isSelectedProvider: () -> Boolean = { false },
) {
    val isSelected = isSelectedProvider()

    Row(
        modifier =
            modifier
                .heightIn(min = ROW_MIN_HEIGHT)
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(horizontal = CONTENT_HORIZONTAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WebDetailViewModeIcon(
            viewMode = viewMode,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ROW_SPACING))

        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = stringResource(webDetailViewModeLabel(viewMode = viewMode)),
                style = DiaryTheme.typography.bodyLarge,
            )

            if (viewMode == WebDetailViewMode.URL) {
                Text(
                    text = stringResource(Res.string.web_detail_view_mode_url_description),
                    style = DiaryTheme.typography.bodySmall,
                    color = DiaryTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        RadioButton(
            selected = isSelected,
            onClick = null,
        )
    }
}

@ScreenPreview
@Composable
private fun WebDetailViewModeBottomSheetContentPreview(
    @PreviewParameter(WebDetailViewModePreviewParameter::class) viewMode: WebDetailViewMode,
) {
    DiaryTheme {
        WebDetailViewModeBottomSheetContent(
            onEvent = {},
            state = rememberWebDetailScaffoldState(initialViewMode = viewMode),
        )
    }
}
