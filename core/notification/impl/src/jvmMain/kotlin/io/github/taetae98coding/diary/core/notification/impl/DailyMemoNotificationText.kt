package io.github.taetae98coding.diary.core.notification.impl

import java.util.Locale
import java.util.ResourceBundle

private const val DAILY_MEMO_NOTIFICATION_BUNDLE_NAME = "io/github/taetae98coding/diary/core/notification/impl/DailyMemoNotification"
private const val DAILY_MEMO_NOTIFICATION_TITLE_KEY = "daily_memo_notification_title"

internal fun dailyMemoNotificationTitle(locale: Locale = Locale.getDefault()): String =
    ResourceBundle
        .getBundle(
            DAILY_MEMO_NOTIFICATION_BUNDLE_NAME,
            locale,
            // 기본 폴백은 요청한 로케일이 없으면 기기의 기본 로케일로 넘어가므로, 한국어 기기에서 영어를 요청하면 한국어가 나온다.
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES),
        ).getString(DAILY_MEMO_NOTIFICATION_TITLE_KEY)
