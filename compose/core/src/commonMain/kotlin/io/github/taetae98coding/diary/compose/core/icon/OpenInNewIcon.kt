package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun OpenInNewIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@IconPreview
@Composable
private fun OpenInNewIconPreview() {
    DiaryTheme {
        OpenInNewIcon()
    }
}
