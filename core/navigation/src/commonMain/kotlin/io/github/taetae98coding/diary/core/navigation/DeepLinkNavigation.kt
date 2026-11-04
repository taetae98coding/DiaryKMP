package io.github.taetae98coding.diary.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest

public fun interface DeepLinkNavigation {
    /** 주소가 맞으면 백스택을 바꾸고 true, 아니면 아무것도 하지 않고 false. */
    public fun open(
        request: DeepLinkRequest,
        backStack: NavBackStack<ScreenNavKey>,
    ): Boolean
}

public fun <T : Any, R : DeepLinkMatcher.MatchResult<T>> DeepLinkMatcher<T, R>.toDeepLinkNavigation(open: NavBackStack<ScreenNavKey>.(R) -> Unit): DeepLinkNavigation =
    DeepLinkNavigation { request, backStack ->
        val result = matchOrNull(request) ?: return@DeepLinkNavigation false

        backStack.open(result)
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
    deepLink: String,
    backStack: NavBackStack<ScreenNavKey>,
): Boolean {
    val request = DeepLinkRequest(deepLink)

    return any { navigation -> navigation.open(request, backStack) }
}
