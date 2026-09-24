import DiaryIos
import FirebaseCore
import FirebaseMessaging
import GoogleMaps
import UIKit
import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        DiaryStartupInitializer.shared.start()

        if let googleMapApiKey = Bundle.main.object(forInfoDictionaryKey: "GMSApiKey") as? String {
            GMSServices.provideAPIKey(googleMapApiKey)
        }

        // FCM 토큰은 APNs 기기 토큰이 있어야 발급되는데, APNs 등록과 그 토큰 전달은 앱 델리게이트로만 할 수 있다.
        application.registerForRemoteNotifications()
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    // 앱이 앞에 있는 동안 도착한 알림도 배너와 알림 센터에 표시한다. 소리와 배지는 쓰지 않는다.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .list]
    }
}
