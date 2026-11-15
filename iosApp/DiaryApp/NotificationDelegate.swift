import DiaryIos
import UserNotifications

final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()

    private override init() {
        super.init()
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .list]
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        if let value = response.notification.request.content.userInfo[DiaryDeepLink.shared.notificationDataKey] as? String {
            await MainActor.run {
                DiaryDeepLink.shared.open(deepLink: value)
            }
        }
    }
}
