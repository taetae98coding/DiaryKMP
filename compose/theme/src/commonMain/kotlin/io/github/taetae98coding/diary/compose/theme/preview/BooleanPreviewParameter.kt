package io.github.taetae98coding.diary.compose.theme.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

public class BooleanPreviewParameter : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(false, true)
}
