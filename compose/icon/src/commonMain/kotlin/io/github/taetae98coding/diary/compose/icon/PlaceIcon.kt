package io.github.taetae98coding.diary.compose.icon

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.IconPreview

@Composable
public fun PlaceIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = DiaryIcons.LocationOn,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@IconPreview
@Composable
private fun PlaceIconPreview() {
    DiaryTheme {
        PlaceIcon()
    }
}
