package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun VisibilityOffIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = Icons.Rounded.VisibilityOff,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@IconPreview
@Composable
private fun VisibilityOffIconPreview() {
    DiaryTheme {
        VisibilityOffIcon()
    }
}
