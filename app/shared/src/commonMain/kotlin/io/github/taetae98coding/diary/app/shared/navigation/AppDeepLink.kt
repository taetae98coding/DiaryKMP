package io.github.taetae98coding.diary.app.shared.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// 앱이 시작되며 받은 주소는 화면이 아직 없으므로, 화면이 받을 때까지 가장 최근 주소 하나를 남겨 둔다.
public object AppDeepLink {
    private val channel = Channel<String>(Channel.CONFLATED)

    internal val deepLink: Flow<String> = channel.receiveAsFlow()

    public fun open(deepLink: String) {
        channel.trySend(deepLink)
    }
}
