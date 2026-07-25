package io.github.taetae98coding.diary.compose.core.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
public data class DiaryDimens(
    val screenHorizontalPadding: Dp,
    val screenVerticalPadding: Dp,
    val itemSpacing: Dp,
    val componentSpacing: Dp,
    val chipAreaHeight: Dp,
    val pickerListHeight: Dp,
) {
    public val screenPaddingValues: PaddingValues =
        PaddingValues(
            horizontal = screenHorizontalPadding,
            vertical = screenVerticalPadding,
        )
}

internal val LocalDiaryDimens = staticCompositionLocalOf<DiaryDimens> { error("DiaryDimens is not provided. Wrap your content in DiaryTheme.") }
