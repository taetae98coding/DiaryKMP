package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Immutable

public sealed interface DiaryTopBarAction {
    public val contentDescription: String

    // isVisible을 false로 바꾸면 크기·투명도 전환을 거쳐 사라지고, 목록에서 빼면 전환 없이 곧바로 사라진다.
    @Immutable
    public data class Icon(
        val icon: DiaryTopBarIcon,
        override val contentDescription: String,
        val onClick: () -> Unit,
        val isEnabled: Boolean = true,
        val isInProgress: Boolean = false,
        val isVisible: Boolean = true,
    ) : DiaryTopBarAction

    @Immutable
    public data class Toggle(
        val icon: DiaryTopBarIcon,
        override val contentDescription: String,
        val isChecked: Boolean,
        val onClick: () -> Unit,
        val isInProgress: Boolean = false,
    ) : DiaryTopBarAction

    @Immutable
    public data class Applied(
        val icon: DiaryTopBarIcon,
        override val contentDescription: String,
        val isApplied: Boolean,
        val appliedStateDescription: String,
        val onClick: () -> Unit,
    ) : DiaryTopBarAction

    @Immutable
    public data class Text(
        val text: String,
        override val contentDescription: String,
        val onClick: () -> Unit,
    ) : DiaryTopBarAction
}
