package io.github.taetae98coding.diary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.taetae98coding.diary.app.shared.App
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink

internal class DiaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 다시 만들어진 화면은 떠나기 전 화면을 복원하므로, 처음 받은 알림의 주소로 다시 이동하지 않는다.
        if (savedInstanceState == null) {
            openDeepLink(intent = intent)
        }
        setContent {
            App()
        }
    }

    // 앱이 떠 있는 동안 알림을 선택하면 새 화면을 쌓지 않고 이 화면이 주소를 받는다.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openDeepLink(intent = intent)
    }

    private fun openDeepLink(intent: Intent) {
        intent.dataString?.let(AppDeepLink::open)
    }
}
