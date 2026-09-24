package io.github.taetae98coding.diary.library.locale

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.Foundation.preferredLanguages

// preferredLanguages는 BCP 47 태그이고, 비어 있을 때만 쓰는 localeIdentifier는 `ko_KR` 꼴이라 구분자를 맞춘다.
internal actual fun platformLanguageTag(): String =
    NSLocale.preferredLanguages.firstOrNull() as? String
        ?: NSLocale.currentLocale.localeIdentifier.replace('_', '-')

internal actual fun platformRegionCode(): String = NSLocale.currentLocale.countryCode.orEmpty()
