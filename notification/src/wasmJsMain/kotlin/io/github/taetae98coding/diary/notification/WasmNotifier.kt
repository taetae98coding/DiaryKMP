@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.notification

internal class WasmNotifier : Notifier {
    override suspend fun notify(notification: Notification) {
        showNotification(
            title = notification.title,
            tag = notification.id,
            isSilent = notification.channel.isSilent,
        )
    }
}

// 같은 tag를 쓰면 브라우저가 앞서 표시한 알림을 새 알림으로 대신하므로 여러 탭에서 발생해도 하나만 남는다.
@Suppress("UnusedParameter")
private fun showNotification(
    title: String,
    tag: String,
    isSilent: Boolean,
): Unit =
    js(
        """
        (() => {
            if (typeof Notification === 'undefined' || Notification.permission !== 'granted') {
                return;
            }
            const notification = new Notification(title, { tag: tag, silent: isSilent });
            notification.onclick = function () {
                window.focus();
                notification.close();
            };
        })()
        """,
    )
