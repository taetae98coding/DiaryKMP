package io.github.taetae98coding.diary.app.shared.navigation

import io.github.taetae98coding.diary.core.navigation.ScreenDeepLinkRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

// 앱이 시작되며 받은 주소는 화면이 아직 없으므로, 화면이 받을 때까지 가장 최근 주소 하나를 남겨 둔다.
@Single
public class AppDeepLink {
    private val channel = Channel<ScreenDeepLinkRequest>(Channel.CONFLATED)

    public val deepLink: Flow<ScreenDeepLinkRequest> = channel.receiveAsFlow()

    // isLaunch는 이 주소로 앱을 새로 시작해 보던 화면이 남아 있지 않을 때만 true로 넘긴다. 플랫폼 진입점만 알 수 있다.
    public fun open(
        deepLink: String,
        isLaunch: Boolean = false,
    ) {
        channel.trySend(ScreenDeepLinkRequest(deepLink = deepLink, isLaunch = isLaunch))
    }
}
