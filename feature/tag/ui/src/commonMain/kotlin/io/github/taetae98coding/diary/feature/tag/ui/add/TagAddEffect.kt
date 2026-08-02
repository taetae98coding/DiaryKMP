package io.github.taetae98coding.diary.feature.tag.ui.add

import kotlin.uuid.Uuid

internal sealed interface TagAddEffect {
    data class AddSucceeded(
        val id: Uuid,
    ) : TagAddEffect

    data object TitleBlank : TagAddEffect
}
