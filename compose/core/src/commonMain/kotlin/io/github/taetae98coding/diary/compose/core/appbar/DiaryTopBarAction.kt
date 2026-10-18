package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Immutable

internal sealed interface DiaryTopBarAction {
    val contentDescription: String

    @Immutable
    data class Icon(
        val icon: DiaryTopBarIcon,
        override val contentDescription: String,
        val onClick: () -> Unit,
        val isEnabled: Boolean,
        val isInProgress: Boolean,
        val isVisible: Boolean,
    ) : DiaryTopBarAction

    @Immutable
    data class Toggle(
        val icon: DiaryTopBarIcon,
        override val contentDescription: String,
        val isChecked: Boolean,
        val onClick: () -> Unit,
        val isInProgress: Boolean,
    ) : DiaryTopBarAction

    @Immutable
    data class Applied(
        val icon: DiaryTopBarIcon,
        override val contentDescription: String,
        val isApplied: Boolean,
        val appliedStateDescription: String,
        val onClick: () -> Unit,
    ) : DiaryTopBarAction

    @Immutable
    data class Text(
        val text: String,
        override val contentDescription: String,
        val onClick: () -> Unit,
    ) : DiaryTopBarAction
}
