import DiaryIos
import FirebaseCore
import GoogleMaps
import SwiftUI
import UserNotifications

@main
struct DiaryApp: App {
    // APNs 기기 토큰은 앱 델리게이트로만 받을 수 있어 그 일만 UIKit 앱 델리게이트에 맡긴다.
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    init() {
        FirebaseApp.configure()
        DiaryStartupInitializer.shared.start()

        if let googleMapApiKey = Bundle.main.object(forInfoDictionaryKey: "GMSApiKey") as? String {
            GMSServices.provideAPIKey(googleMapApiKey)
        }

        // 알림을 눌러 앱을 처음 띄운 경우에도 응답을 받으려면 앱이 시작을 마치기 전에 대리 객체를 정해야 한다.
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
        FileUploadBackgroundEvents.shared.observe()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        // 시스템이 파일 올리기 전송의 이벤트를 넘기려고 앱을 깨웠다. 전송 세션은 앱 시작 시점에 이미 다시 만들어져 이벤트를 받으므로,
        // 이벤트를 모두 받았다는 알림이 올 때까지 기다렸다가 끝내야 시스템이 앱을 다시 멈춘다.
        .backgroundTask(.urlSession(FileUploadBackgroundEvents.sessionIdentifier)) {
            await FileUploadBackgroundEvents.shared.waitUntilFinished()
        }
    }
}
