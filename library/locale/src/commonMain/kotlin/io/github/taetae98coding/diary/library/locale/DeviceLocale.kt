package io.github.taetae98coding.diary.library.locale

public object DeviceLocale {
    public fun currentLanguageTag(): String = platformLanguageTag()
}

internal expect fun platformLanguageTag(): String
