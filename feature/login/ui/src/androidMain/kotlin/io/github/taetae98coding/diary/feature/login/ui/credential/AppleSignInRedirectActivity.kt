package io.github.taetae98coding.diary.feature.login.ui.credential

import android.app.Activity
import android.content.Intent
import android.os.Bundle

internal class AppleSignInRedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.data?.let(AppleSignInRedirectRelay::deliver)

        // 브라우저 탭이 이 Activity 아래에 남아 있으므로, 앱의 시작 Activity를 CLEAR_TOP으로 다시 띄워 그 위를 함께 걷어낸다.
        packageManager
            .getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            ?.let(::startActivity)

        finish()
    }
}
