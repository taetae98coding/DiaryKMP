package io.github.taetae98coding.diary.feature.web.ui.add

import kotlin.uuid.Uuid

internal sealed interface WebAddEffect {
    data class AddSucceeded(
        val id: Uuid,
    ) : WebAddEffect

    data object TitleBlank : WebAddEffect

    data object UrlBlank : WebAddEffect

    data object HeaderNameBlank : WebAddEffect
}
