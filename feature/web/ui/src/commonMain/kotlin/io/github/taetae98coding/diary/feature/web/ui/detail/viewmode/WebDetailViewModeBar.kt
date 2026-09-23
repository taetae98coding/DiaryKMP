package io.github.taetae98coding.diary.feature.web.ui.detail.viewmode

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.icon.DropDownIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldEvent
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.rememberWebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_content_description
import org.jetbrains.compose.resources.stringResource

private val LABEL_SPACING = 4.dp

@Composable
internal fun WebDetailViewModeBar(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
) {
    val contentDescription = stringResource(Res.string.web_detail_view_mode_content_description)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = { onEvent(WebDetailScaffoldEvent.ClickViewMode) },
            modifier = Modifier.semantics { this.contentDescription = contentDescription },
            shape = CircleShape,
        ) {
            val viewMode = state.viewMode

            WebDetailViewModeIcon(
                viewMode = viewMode,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.width(LABEL_SPACING))
            Text(text = stringResource(webDetailViewModeLabel(viewMode = viewMode)))
            Spacer(modifier = Modifier.width(LABEL_SPACING))
            DropDownIcon(modifier = Modifier.size(ButtonDefaults.IconSize))
        }
    }
}

@ComponentPreview
@Composable
private fun WebDetailViewModeBarPreview(
    @PreviewParameter(WebDetailViewModePreviewParameter::class) viewMode: WebDetailViewMode,
) {
    DiaryTheme {
        Surface {
            WebDetailViewModeBar(
                onEvent = {},
                state = rememberWebDetailScaffoldState(initialViewMode = viewMode),
            )
        }
    }
}
