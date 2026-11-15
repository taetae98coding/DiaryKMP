import CoreSpotlight
import DiaryIos
import UIKit

// Spotlight 결과나 공유 확장의 주소로 앱을 새로 시작했는지는 장면이 연결될 때 넘어오는 사용자 활동과 주소로만 알 수 있어, SwiftUI 대신 장면 대리 객체가 받는다.
final class DiarySceneDelegate: NSObject, UIWindowSceneDelegate {
    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        let deepLink = connectionOptions.userActivities.lazy.compactMap(Self.deepLink(of:)).first
            ?? connectionOptions.urlContexts.lazy.compactMap { Self.deepLink(of: $0.url) }.first

        if let deepLink {
            DiaryDeepLink.shared.openOnLaunch(deepLink: deepLink)
        }
    }

    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
        if let deepLink = Self.deepLink(of: userActivity) {
            DiaryDeepLink.shared.open(deepLink: deepLink)
        }
    }

    // 공유 확장이 연 주소만 받는다. Google 로그인의 돌아오는 주소는 로그인 SDK의 인증 세션이 직접 받으므로 여기로 오지 않고, 오더라도 열지 않는다.
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        if let deepLink = URLContexts.lazy.compactMap({ Self.deepLink(of: $0.url) }).first {
            DiaryDeepLink.shared.open(deepLink: deepLink)
        }
    }

    // Spotlight 색인은 항목을 여는 딥링크 주소를 항목 식별자로 담으므로, 고른 항목의 식별자를 그대로 연다.
    private static func deepLink(of userActivity: NSUserActivity) -> String? {
        guard userActivity.activityType == CSSearchableItemActionType else { return nil }

        return userActivity.userInfo?[CSSearchableItemActivityIdentifier] as? String
    }

    // 개발용과 운영용 앱을 함께 설치해도 공유 확장이 자기를 담은 앱을 열도록, 공유 확장은 앱의 번들 식별자를 scheme으로 쓴다.
    // 앱 안의 딥링크는 diary scheme으로 해석하므로 scheme만 바꿔 넘긴다. 주소의 나머지는 ScreenDeepLink.SHARE의 형태다.
    private static func deepLink(of url: URL) -> String? {
        guard
            let bundleIdentifier = Bundle.main.bundleIdentifier,
            url.scheme?.lowercased() == bundleIdentifier.lowercased(),
            url.host?.lowercased() == shareHost,
            var components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        else { return nil }

        components.scheme = diaryScheme
        return components.string
    }

    private static let diaryScheme = "diary"
    private static let shareHost = "share"
}
