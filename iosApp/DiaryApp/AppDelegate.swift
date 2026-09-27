import DiaryIos
import FirebaseCore
import FirebaseMessaging
import GoogleMaps
import UIKit
import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate {
    // 파일 올리기 전송 세션이 이벤트를 모두 전달했다고 알리는 이름이다.
    // core:network:impl의 BackgroundSessionFileUploadTransport가 보내는 이름과 같아야 한다.
    private static let fileUploadEventsFinishedNotification = Notification.Name("io.github.taetae98coding.diary.fileUpload.backgroundEventsFinished")

    // 알림에 담긴 앱 화면 주소의 키다. core:navigation의 ScreenDeepLink.USER_INFO_KEY와 같아야 한다.
    private static let deepLinkUserInfoKey = "deepLink"

    private var fileUploadEventsCompletionHandler: (() -> Void)?

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

        NotificationCenter.default.addObserver(
            forName: Self.fileUploadEventsFinishedNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            self?.fileUploadEventsCompletionHandler?()
            self?.fileUploadEventsCompletionHandler = nil
        }
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    // 시스템이 파일 올리기 전송의 이벤트를 넘기려고 앱을 깨웠다. 전송 세션은 앱 시작 시점에 이미 다시 만들어져 이벤트를 받으므로,
    // 이벤트를 모두 받았다는 알림이 올 때까지 완료 처리기를 들고 있다가 부른다.
    func application(
        _ application: UIApplication,
        handleEventsForBackgroundURLSession identifier: String,
        completionHandler: @escaping () -> Void
    ) {
        fileUploadEventsCompletionHandler = completionHandler
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
