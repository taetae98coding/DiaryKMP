package io.github.taetae98coding.diary.compose.theme.preview

import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

@PreviewScreenSizes
@PreviewLightDark
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class ScreenPreview
