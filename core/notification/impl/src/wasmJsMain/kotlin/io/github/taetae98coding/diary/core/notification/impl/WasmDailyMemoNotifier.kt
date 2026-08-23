@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.notification.impl

internal class WasmDailyMemoNotifier : DailyMemoNotifier {
    override suspend fun notifyDailyMemo() {
        showDailyMemoNotification(
            title = dailyMemoNotificationTitle(),
            tag = DAILY_MEMO_NOTIFICATION_TAG,
        )
    }

    companion object {
        private const val DAILY_MEMO_NOTIFICATION_TAG = "daily-memo"
    }
}

// 같은 tag를 쓰면 브라우저가 앞서 표시한 알림을 새 알림으로 대신하므로 여러 탭에서 발생해도 하나만 남는다.
@Suppress("UnusedParameter")
private fun showDailyMemoNotification(
    title: String,
    tag: String,
): Unit =
    js(
        """
        (() => {
            if (typeof Notification === 'undefined' || Notification.permission !== 'granted') {
                return;
            }
            const notification = new Notification(title, { tag: tag, silent: true });
            notification.onclick = function () {
                window.focus();
                notification.close();
            };
        })()
        """,
    )
