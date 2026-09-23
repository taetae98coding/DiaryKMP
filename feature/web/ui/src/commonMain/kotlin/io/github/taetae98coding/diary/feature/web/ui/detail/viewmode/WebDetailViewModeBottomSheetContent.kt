@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.web.ui.detail.viewmode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldEvent
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.rememberWebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_title
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_url_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebDetailViewModeBottomSheetContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.web_detail_view_mode_title),
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .selectableGroup()
                    .styleable(style = DiaryTheme.styles.bottomSheetContent),
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
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).styleable(style = DiaryTheme.styles.bottomSheetRow),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WebDetailViewModeIcon(
            viewMode = viewMode,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(WebDetailViewModeBottomSheetContentDefaults.IconToLabelSpacing))

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
