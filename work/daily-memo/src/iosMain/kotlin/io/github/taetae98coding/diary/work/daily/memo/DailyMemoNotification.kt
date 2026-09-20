package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.NotificationChannel
import platform.Foundation.NSBundle

private const val DAILY_MEMO_NOTIFICATION_TITLE_KEY = "daily_memo_notification_title"
private const val DAILY_MEMO_NOTIFICATION_TITLE_EMPTY_KEY = "daily_memo_notification_title_empty"
private const val DAILY_MEMO_NOTIFICATION_TITLE_ONE_KEY = "daily_memo_notification_title_one"
private const val DAILY_MEMO_NOTIFICATION_TITLE_OTHER_KEY = "daily_memo_notification_title_other"
private const val COUNT_PLACEHOLDER = "%d"

// 날짜별로 다른 식별자를 써야 앞으로 7일의 알림이 각각 대기하고, 같은 날짜를 다시 제출하면 그 날짜만 대신한다.
internal fun dailyMemoNotification(upcoming: UpcomingDailyMemoNotification): Notification =
    Notification(
        id = "$DAILY_MEMO_NOTIFICATION_ID-${upcoming.date}",
        // iOS에는 알림 채널이 없어 채널 이름과 설명을 표시하지 않는다.
        channel =
            NotificationChannel(
                id = DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
                name = "",
                description = "",
                isSilent = true,
            ),
        title = dailyMemoNotificationTitle(content = upcoming.content),
        body = dailyMemoNotificationBody(content = upcoming.content),
    )

private fun dailyMemoNotificationTitle(content: DailyMemoNotificationContent): String =
    when (content) {
        is DailyMemoNotificationContent.Loaded -> {
            when (val count = content.memoList.size) {
                0 -> localizedString(key = DAILY_MEMO_NOTIFICATION_TITLE_EMPTY_KEY)
                1 -> localizedString(key = DAILY_MEMO_NOTIFICATION_TITLE_ONE_KEY).replace(COUNT_PLACEHOLDER, count.toString())
                else -> localizedString(key = DAILY_MEMO_NOTIFICATION_TITLE_OTHER_KEY).replace(COUNT_PLACEHOLDER, count.toString())
            }
        }

        DailyMemoNotificationContent.Unavailable -> localizedString(key = DAILY_MEMO_NOTIFICATION_TITLE_KEY)
    }

private fun localizedString(key: String): String = NSBundle.mainBundle.localizedStringForKey(key, key, null)
