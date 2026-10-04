package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

internal object DiaryRefreshableGridDefaults {
    const val COLUMN_COUNT: Int = 2

    val ItemArrangement: Arrangement.HorizontalOrVertical
        @Composable
        get() = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing)

    @Composable
    fun contentPadding(bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding): PaddingValues =
        PaddingValues(
            start = DiaryTheme.dimens.screenHorizontalPadding,
            top = DiaryTheme.dimens.screenVerticalPadding,
            end = DiaryTheme.dimens.screenHorizontalPadding,
            bottom = bottomPadding,
        )
}
