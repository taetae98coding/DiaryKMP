package io.github.taetae98coding.diary.core.notification.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Frame
import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

internal class JvmDailyMemoNotifier : DailyMemoNotifier {
    private var trayIcon: TrayIcon? = null

    override suspend fun notifyDailyMemo() {
        withContext(Dispatchers.Default) {
            // 데스크톱은 알림을 띄울 창구가 트레이 아이콘뿐이라, 알림을 표시하는 동안에는 아이콘이 트레이에 남아 있어야 한다.
            val icon = trayIcon ?: addTrayIcon()?.also { added -> trayIcon = added } ?: return@withContext

            icon.displayMessage(dailyMemoNotificationTitle(), null, TrayIcon.MessageType.NONE)
        }
    }

    private fun addTrayIcon(): TrayIcon? {
        val image = trayIconImage() ?: return null
        val icon =
            TrayIcon(image).apply {
                isImageAutoSize = true
                addActionListener { bringAppToFront() }
            }

        return runCatching {
            SystemTray.getSystemTray().add(icon)
            icon
        }.getOrNull()
    }

    private fun trayIconImage(): Image? {
        if (!SystemTray.isSupported()) return null

        return javaClass.getResource(TRAY_ICON_RESOURCE)?.let { url -> Toolkit.getDefaultToolkit().getImage(url) }
    }

    private fun bringAppToFront() {
        Frame
            .getFrames()
            .firstOrNull { frame -> frame.isDisplayable }
            ?.apply {
                if (state == Frame.ICONIFIED) state = Frame.NORMAL
                toFront()
                requestFocus()
            }
    }

    companion object {
        private const val TRAY_ICON_RESOURCE = "daily_memo_notification_tray_icon.png"
    }
}
