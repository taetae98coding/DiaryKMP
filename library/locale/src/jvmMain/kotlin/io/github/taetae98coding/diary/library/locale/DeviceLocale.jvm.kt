package io.github.taetae98coding.diary.library.locale

import java.util.Locale

internal actual fun platformLanguageTag(): String = Locale.getDefault().toLanguageTag()
