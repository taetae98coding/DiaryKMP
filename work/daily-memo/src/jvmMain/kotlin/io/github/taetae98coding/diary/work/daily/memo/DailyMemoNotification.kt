package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.NotificationChannel
import java.util.Locale
import java.util.ResourceBundle

private const val DAILY_MEMO_NOTIFICATION_BUNDLE_NAME = "io/github/taetae98coding/diary/work/daily/memo/DailyMemoNotification"
private const val DAILY_MEMO_NOTIFICATION_TITLE_KEY = "daily_memo_notification_title"
private const val DAILY_MEMO_NOTIFICATION_TITLE_EMPTY_KEY = "daily_memo_notification_title_empty"
private const val DAILY_MEMO_NOTIFICATION_TITLE_ONE_KEY = "daily_memo_notification_title_one"
private const val DAILY_MEMO_NOTIFICATION_TITLE_OTHER_KEY = "daily_memo_notification_title_other"
private const val DAILY_MEMO_NOTIFICATION_CHANNEL_NAME_KEY = "daily_memo_notification_channel_name"
private const val DAILY_MEMO_NOTIFICATION_CHANNEL_DESCRIPTION_KEY = "daily_memo_notification_channel_description"

internal actual fun dailyMemoNotification(content: DailyMemoNotificationContent): Notification {
    val bundle = dailyMemoNotificationBundle(locale = Locale.getDefault())

    return Notification(
        id = DAILY_MEMO_NOTIFICATION_ID,
        channel =
            NotificationChannel(
                id = DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
                name = bundle.getString(DAILY_MEMO_NOTIFICATION_CHANNEL_NAME_KEY),
                description = bundle.getString(DAILY_MEMO_NOTIFICATION_CHANNEL_DESCRIPTION_KEY),
                isSilent = true,
            ),
        title = bundle.dailyMemoNotificationTitle(content = content),
        body = dailyMemoNotificationBody(content = content),
    )
}

internal fun dailyMemoNotificationTitle(
    content: DailyMemoNotificationContent,
    locale: Locale = Locale.getDefault(),
): String = dailyMemoNotificationBundle(locale = locale).dailyMemoNotificationTitle(content = content)

private fun ResourceBundle.dailyMemoNotificationTitle(content: DailyMemoNotificationContent): String =
    when (content) {
        is DailyMemoNotificationContent.Loaded -> {
            when (val count = content.memoList.size) {
                0 -> getString(DAILY_MEMO_NOTIFICATION_TITLE_EMPTY_KEY)
                1 -> getString(DAILY_MEMO_NOTIFICATION_TITLE_ONE_KEY).format(count)
                else -> getString(DAILY_MEMO_NOTIFICATION_TITLE_OTHER_KEY).format(count)
            }
        }

        DailyMemoNotificationContent.Unavailable -> getString(DAILY_MEMO_NOTIFICATION_TITLE_KEY)
    }

private fun dailyMemoNotificationBundle(locale: Locale): ResourceBundle =
    ResourceBundle.getBundle(
        DAILY_MEMO_NOTIFICATION_BUNDLE_NAME,
        locale,
        // 기본 폴백은 요청한 로케일이 없으면 기기의 기본 로케일로 넘어가므로, 한국어 기기에서 영어를 요청하면 한국어가 나온다.
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES),
    )
