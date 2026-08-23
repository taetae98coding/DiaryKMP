@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.notification.impl

private const val KOREAN_LANGUAGE_TAG = "ko"

// 브라우저에는 로케일별 문구를 담는 리소스 기능이 없어 문구를 코드에 두고 브라우저 언어로 고른다.
internal fun dailyMemoNotificationTitle(): String =
    if (browserLanguage().startsWith(KOREAN_LANGUAGE_TAG)) {
        "오늘의 메모를 확인하세요"
    } else {
        "Check today's memos"
    }

private fun browserLanguage(): String = js("navigator.language || ''")
