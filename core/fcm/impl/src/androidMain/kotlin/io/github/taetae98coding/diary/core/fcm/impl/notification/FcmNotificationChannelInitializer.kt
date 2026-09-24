package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.content.Context
import androidx.startup.Initializer

// 앱이 백그라운드일 때는 FCM SDK가 알림을 표시하는데, 채널이 없으면 기본 채널로 떨어져 디자인이 정한 이름과 중요도를 잃는다.
// 그래서 첫 알림이 오기 전에 앱 시작 시점에 채널을 만든다.
internal class FcmNotificationChannelInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        context.createDailyMemoNotificationChannel()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
