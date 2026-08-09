package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class PlaceHomeViewModePreviewParameter : PreviewParameterProvider<PlaceHomeViewMode> {
    override val values: Sequence<PlaceHomeViewMode> = placeHomeViewModeList.asSequence()
}
