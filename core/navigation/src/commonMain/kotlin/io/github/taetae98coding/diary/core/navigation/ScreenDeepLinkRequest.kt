package io.github.taetae98coding.diary.core.navigation

// 앱 밖에서 받은 주소다. isLaunch는 플랫폼 진입점이 이 주소로 앱을 새로 시작했는지 알려 준다.
public data class ScreenDeepLinkRequest(
    val deepLink: String,
    val isLaunch: Boolean = false,
)
