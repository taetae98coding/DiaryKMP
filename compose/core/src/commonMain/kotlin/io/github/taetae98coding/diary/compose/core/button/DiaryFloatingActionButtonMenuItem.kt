package io.github.taetae98coding.diary.compose.core.button

public class DiaryFloatingActionButtonMenuItem(
    public val text: String,
    public val icon: DiaryFloatingActionButtonMenuIcon,
    public val onClick: () -> Unit,
)
