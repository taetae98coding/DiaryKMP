package io.github.taetae98coding.diary.library.locale

internal actual fun platformLanguageTag(): String = navigatorLanguage()

// 브라우저는 지역 설정을 따로 노출하지 않아 언어 태그의 지역 하위 태그를 쓴다.
internal actual fun platformRegionCode(): String =
    navigatorLanguage()
        .split('-')
        .drop(1)
        .firstOrNull { subtag -> subtag.length == 2 && subtag.all { character -> character.isLetter() } }
        .orEmpty()

private fun navigatorLanguage(): String = js("navigator.language || ''")
