package io.github.taetae98coding.diary.compose.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.IconPreview

@Composable
public fun ProfileIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = DiaryIcons.AccountCircle,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@IconPreview
@Composable
private fun ProfileIconPreview() {
    DiaryTheme {
        ProfileIcon()
    }
}
