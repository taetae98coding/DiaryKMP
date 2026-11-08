import CoreSpotlight
import DiaryIos
import UIKit

// Spotlight 결과로 앱을 새로 시작했는지는 장면이 연결될 때 넘어오는 사용자 활동으로만 알 수 있어, SwiftUI 대신 장면 대리 객체가 받는다.
// Spotlight 색인은 항목을 여는 딥링크 주소를 항목 식별자로 담으므로, 고른 항목의 식별자를 그대로 연다.
final class SpotlightSceneDelegate: NSObject, UIWindowSceneDelegate {
    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        if let deepLink = connectionOptions.userActivities.lazy.compactMap(Self.deepLink(of:)).first {
            DiaryDeepLink.shared.openOnLaunch(deepLink: deepLink)
        }
    }

    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
        if let deepLink = Self.deepLink(of: userActivity) {
            DiaryDeepLink.shared.open(deepLink: deepLink)
        }
    }

    private static func deepLink(of userActivity: NSUserActivity) -> String? {
        guard userActivity.activityType == CSSearchableItemActionType else { return nil }

        return userActivity.userInfo?[CSSearchableItemActivityIdentifier] as? String
    }
}
