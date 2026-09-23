package io.github.taetae98coding.diary.feature.web.ui.detail.viewmode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class WebDetailViewModePreviewParameter : PreviewParameterProvider<WebDetailViewMode> {
    override val values: Sequence<WebDetailViewMode> = webDetailViewModeList.asSequence()
}
