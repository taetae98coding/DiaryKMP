package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarActionScope
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.share_button_content_description
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
public fun DiaryTopBarActionScope.ShareAction(
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    IconAction(
        icon = DiaryTopBarIcon.SHARE,
        contentDescription = stringResource(Res.string.share_button_content_description),
        onClick = onClick,
        isEnabled = isEnabled,
    )
}

@ComponentPreview
@Composable
private fun ShareActionPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isEnabled: Boolean,
) {
    DiaryTheme {
        DiaryTopBar(
            title = { Title(text = "제목") },
            actions = { ShareAction(onClick = {}, isEnabled = isEnabled) },
        )
    }
}
