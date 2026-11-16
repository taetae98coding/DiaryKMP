package io.github.taetae98coding.diary.feature.web.ui.detail.viewmode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.icon.CodeIcon
import io.github.taetae98coding.diary.compose.icon.LinkIcon
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
internal fun WebDetailViewModeIcon(
    viewMode: WebDetailViewMode,
    modifier: Modifier = Modifier,
) {
    when (viewMode) {
        WebDetailViewMode.URL -> LinkIcon(modifier = modifier)
        WebDetailViewMode.RESPONSE -> CodeIcon(modifier = modifier)
    }
}

@ComponentPreview
@Composable
private fun WebDetailViewModeIconPreview() {
    DiaryTheme {
        WebDetailViewModeIcon(viewMode = WebDetailViewMode.URL)
    }
}
