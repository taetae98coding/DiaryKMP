package io.github.taetae98coding.diary.compose.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview

public data object DiaryTheme {
    public val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    public val dimens: DiaryDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalDiaryDimens.current

    public val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    public val styles: DiaryStyles = DiaryStyles
}

@Composable
public fun DiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val dimens =
        DiaryDimens(
            screenHorizontalPadding = 16.dp,
            screenVerticalPadding = 16.dp,
            itemSpacing = 8.dp,
            componentSpacing = 12.dp,
            chipAreaHeight = 150.dp,
            pickerListHeight = 288.dp,
            bottomSheetHorizontalPadding = 24.dp,
            bottomSheetTitleVerticalPadding = 12.dp,
            bottomSheetBottomPadding = 16.dp,
            cardContentPadding = 16.dp,
        )

    CompositionLocalProvider(LocalDiaryDimens provides dimens) {
        MaterialExpressiveTheme(
            colorScheme = if (darkTheme) darkColorScheme() else expressiveLightColorScheme(),
            content = content,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryThemePreview() {
    DiaryTheme {
        Surface {
            Text(
                text = "Diary",
                style = DiaryTheme.typography.titleLargeEmphasized,
            )
        }
    }
}
