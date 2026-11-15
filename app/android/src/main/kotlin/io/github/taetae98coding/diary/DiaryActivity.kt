package io.github.taetae98coding.diary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.taetae98coding.diary.app.shared.App
import io.github.taetae98coding.diary.app.shared.AppDependencies
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import org.koin.android.ext.android.inject

internal class DiaryActivity : ComponentActivity() {
    private val appDeepLink: AppDeepLink by inject()
    private val appDependencies: AppDependencies by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 다시 만들어진 화면은 떠나기 전 화면을 복원하므로, 처음 받은 알림의 주소로 다시 이동하지 않는다.
        if (savedInstanceState == null) {
            openDeepLink(intent = intent, isLaunch = true)
        }
        setContent {
            App(dependencies = appDependencies)
        }
    }

    // 앱이 떠 있는 동안 알림을 선택하면 새 화면을 쌓지 않고 이 화면이 주소를 받는다.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openDeepLink(intent = intent, isLaunch = false)
    }

    // 앱이 표시한 알림은 주소를 data로, 백그라운드에서 FCM SDK가 표시한 알림은 서버가 보낸 값을 extras로 넘긴다.
    // 처음 만들어진 화면은 보던 화면이 남아 있지 않으므로 받은 주소를 앱 시작 주소로 넘긴다.
    private fun openDeepLink(
        intent: Intent,
        isLaunch: Boolean,
    ) {
        val deepLink = intent.dataString ?: intent.getStringExtra(ScreenDeepLink.NOTIFICATION_DATA_KEY).orEmpty()

        if (deepLink.isNotEmpty()) {
            appDeepLink.open(deepLink = deepLink, isLaunch = isLaunch)
        }
    }
}
