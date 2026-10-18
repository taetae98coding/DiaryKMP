import DiaryIos
import UserNotifications

final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()

    // 알림에 담긴 앱 화면 주소의 키다. core:navigation의 ScreenDeepLink.USER_INFO_KEY와 같아야 한다.
    private static let deepLinkUserInfoKey = "deepLink"

    private override init() {
        super.init()
    }

    // 앱이 앞에 있는 동안 도착한 알림도 배너와 알림 센터에 표시한다. 소리와 배지는 쓰지 않는다.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .list]
    }

    // 알림에 앱 화면 주소가 담겨 있으면 그 화면을 연다. 주소가 없는 알림은 앱만 연다.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        if let deepLink = response.notification.request.content.userInfo[Self.deepLinkUserInfoKey] as? String {
            await MainActor.run {
                DiaryDeepLink.shared.open(deepLink: deepLink)
            }
        }
    }
}
