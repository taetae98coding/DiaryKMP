package io.github.taetae98coding.diary.feature.web.ui.detail.viewmode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.CodeIcon
import io.github.taetae98coding.diary.compose.core.icon.LinkIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

// 라벨이 현재 방식을 알리므로 아이콘에는 접근성 이름을 두지 않는다.
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
