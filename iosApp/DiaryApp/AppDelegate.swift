import FirebaseMessaging
import UIKit

// FCM 설치 ID 등록은 APNs 기기 토큰이 있어야 알림으로 이어지는데, APNs 등록과 그 토큰 전달은 SwiftUI로 받을 수 없어 앱 델리게이트가 맡는다.
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        application.registerForRemoteNotifications()
        return true
    }

    // SwiftUI 장면이 받지 못하는 장면 연결 시점의 사용자 활동을 받도록 장면 대리 객체를 정한다.
    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(name: nil, sessionRole: connectingSceneSession.role)
        configuration.delegateClass = SpotlightSceneDelegate.self
        return configuration
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}
