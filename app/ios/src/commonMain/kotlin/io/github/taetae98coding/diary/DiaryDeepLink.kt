package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import org.koin.mp.KoinPlatform

// Swift는 Koin을 쓰지 못하므로 알림 대리 객체가 부를 진입점을 여기에 둔다.
public object DiaryDeepLink {
    private val appDeepLink: AppDeepLink by lazy { KoinPlatform.getKoin().get() }

    // Swift가 알림의 userInfo에서 주소를 꺼낼 때 쓰는 키다. 상수를 Swift에 다시 적지 않도록 여기서 내보낸다.
    public val notificationDataKey: String
        get() = ScreenDeepLink.NOTIFICATION_DATA_KEY

    public fun open(deepLink: String) {
        appDeepLink.open(deepLink = deepLink)
    }
}
