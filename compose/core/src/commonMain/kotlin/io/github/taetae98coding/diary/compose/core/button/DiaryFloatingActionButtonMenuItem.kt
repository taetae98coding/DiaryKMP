package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.ui.graphics.vector.ImageVector

public class DiaryFloatingActionButtonMenuItem(
    public val text: String,
    public val icon: ImageVector,
    public val onClick: () -> Unit,
)
