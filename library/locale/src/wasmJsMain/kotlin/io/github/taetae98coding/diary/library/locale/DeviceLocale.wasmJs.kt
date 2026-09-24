package io.github.taetae98coding.diary.library.locale

internal actual fun platformLanguageTag(): String = navigatorLanguage()

private fun navigatorLanguage(): String = js("navigator.language || ''")
