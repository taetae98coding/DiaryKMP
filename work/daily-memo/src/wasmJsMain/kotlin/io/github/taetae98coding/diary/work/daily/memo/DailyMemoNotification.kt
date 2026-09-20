@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.NotificationChannel

private const val KOREAN_LANGUAGE_TAG = "ko"

// 브라우저에는 로케일별 문구를 담는 리소스 기능이 없어 문구를 코드에 두고 브라우저 언어로 고른다.
internal actual fun dailyMemoNotification(): Notification {
    val isKorean = browserLanguage().startsWith(KOREAN_LANGUAGE_TAG)

    return Notification(
        id = DAILY_MEMO_NOTIFICATION_ID,
        channel =
            NotificationChannel(
                id = DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
                name = if (isKorean) "일일 메모 알림" else "Daily memo reminder",
                description = if (isKorean) "매일 아침 오늘의 메모를 확인하도록 안내합니다." else "Reminds you every morning to check today's memos.",
                isSilent = true,
            ),
        title = if (isKorean) "오늘의 메모를 확인하세요" else "Check today's memos",
    )
}

private fun browserLanguage(): String = js("navigator.language || ''")
