package io.github.taetae98coding.diary.feature.web.ui.detail.viewmode

import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_response_label
import io.github.taetae98coding.diary.feature.web.ui.web_detail_view_mode_url_label
import org.jetbrains.compose.resources.StringResource

internal fun webDetailViewModeLabel(viewMode: WebDetailViewMode): StringResource =
    when (viewMode) {
        WebDetailViewMode.URL -> Res.string.web_detail_view_mode_url_label
        WebDetailViewMode.RESPONSE -> Res.string.web_detail_view_mode_response_label
    }
