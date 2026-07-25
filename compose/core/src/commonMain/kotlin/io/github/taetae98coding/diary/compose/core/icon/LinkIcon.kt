package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun LinkIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = Icons.Rounded.Link,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@IconPreview
@Composable
private fun LinkIconPreview() {
    DiaryTheme {
        LinkIcon()
    }
}
