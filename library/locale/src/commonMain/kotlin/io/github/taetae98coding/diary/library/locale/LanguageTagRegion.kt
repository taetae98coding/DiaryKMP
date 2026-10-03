package io.github.taetae98coding.diary.library.locale

internal fun String.languageTagRegionCode(): String =
    split('-')
        .drop(1)
        .firstOrNull { subtag -> subtag.length == REGION_SUBTAG_LENGTH && subtag.all { character -> character.isLetter() } }
        .orEmpty()

private const val REGION_SUBTAG_LENGTH = 2
