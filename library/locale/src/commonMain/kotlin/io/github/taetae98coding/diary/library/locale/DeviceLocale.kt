package io.github.taetae98coding.diary.library.locale

public object DeviceLocale {
    public fun currentLanguageTag(): String = platformLanguageTag()

    public fun currentRegionCode(): String = platformRegionCode().uppercase()
}

internal expect fun platformLanguageTag(): String

internal expect fun platformRegionCode(): String
