package io.github.taetae98coding.diary.core.navigation

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest

public fun interface DeepLinkNavigation {
    /**
     * 주소가 맞으면 백스택을 바꾸고 true, 아니면 아무것도 하지 않고 false.
     * [isLaunch]는 앱이 이 주소를 받으며 새로 시작해 보던 화면이 남아 있지 않은지다.
     */
    public fun open(
        request: DeepLinkRequest,
        navigator: ScreenNavigator,
        isLaunch: Boolean,
    ): Boolean
}

// 앱이 새로 시작했는지와 관계없이 같은 백스택 정책을 쓰는 주소에 쓴다.
public fun <T : Any, R : DeepLinkMatcher.MatchResult<T>> DeepLinkMatcher<T, R>.toDeepLinkNavigation(open: ScreenNavigator.(R) -> Unit): DeepLinkNavigation = toDeepLinkNavigation { result, _ -> open(result) }

public fun <T : Any, R : DeepLinkMatcher.MatchResult<T>> DeepLinkMatcher<T, R>.toDeepLinkNavigation(open: ScreenNavigator.(R, isLaunch: Boolean) -> Unit): DeepLinkNavigation =
    DeepLinkNavigation { request, navigator, isLaunch ->
        val result = matchOrNull(request) ?: return@DeepLinkNavigation false

        navigator.open(result, isLaunch)
        true
    }

// 주소의 값을 화면 키의 값으로 읽지 못하면(날짜가 아닌 date 등) 매처가 예외를 던지므로, 그런 주소는 맞지 않는 주소로 다룬다.
public fun <T : Any, R : DeepLinkMatcher.MatchResult<T>> DeepLinkMatcher<T, R>.matchOrNull(request: DeepLinkRequest): R? =
    try {
        match(request)
    } catch (_: IllegalArgumentException) {
        null
    }

public fun List<DeepLinkNavigation>.open(
    request: ScreenDeepLinkRequest,
    navigator: ScreenNavigator,
): Boolean {
    val deepLinkRequest = DeepLinkRequest(request.deepLink)

    return any { navigation -> navigation.open(deepLinkRequest, navigator, request.isLaunch) }
}
