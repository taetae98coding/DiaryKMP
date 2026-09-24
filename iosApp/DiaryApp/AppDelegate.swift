import DiaryIos
import FirebaseCore
import FirebaseMessaging
import GoogleMaps
import UIKit

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
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}
