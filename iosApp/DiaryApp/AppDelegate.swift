import FirebaseMessaging
import UIKit

// FCM 설치 ID 등록은 APNs 기기 토큰이 있어야 알림으로 이어지는데, APNs 등록과 그 토큰 전달은 SwiftUI로 받을 수 없어 앱 델리게이트에 이것만 남긴다.
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}
