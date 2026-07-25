package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun RemoveIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = Icons.Rounded.Remove,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@IconPreview
@Composable
private fun RemoveIconPreview() {
    DiaryTheme {
        RemoveIcon()
    }
}
