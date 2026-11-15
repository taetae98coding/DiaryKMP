import UIKit

// 공유 시트에서 Diary를 고르면 시스템이 만드는 진입점이다. 공유 내용을 읽는 일은 짧게 끝나므로 진행 표시 없이 빈 화면을 둔 채 Diary 앱으로 넘긴다.
// 공유 확장은 Kotlin을 쓰지 않으므로 주소의 형태와 키는 core/navigation의 ScreenDeepLink.SHARE와 같아야 한다.
final class ShareViewController: UIViewController {
    private var isStarted = false

    // 응답자 사슬에서 앱 객체를 찾으려면 화면이 창에 붙어 있어야 하므로, 화면이 보인 뒤에 시작한다.
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        guard !isStarted else { return }
        isStarted = true

        let items = extensionContext?.inputItems.compactMap { $0 as? NSExtensionItem } ?? []
        Task {
            switch await ShareContentReader.read(items: items) {
            case .content(let content) where !content.text.isEmpty:
                openApp(content: content)
            case .content, .unsupported, .unreadable:
                extensionContext?.completeRequest(returningItems: nil)
            }
        }
    }

    private func openApp(content: ShareContent) {
        guard
            let url = Self.deepLink(content: content),
            let application = Self.application(from: self)
        else {
            showOpenFailedAlert()
            return
        }

        // 공유 확장에는 앱을 여는 공개 API가 없어, 응답자 사슬에서 찾은 앱 객체로 연다. iOS 18부터 openURL: 선택자는 동작하지 않는다.
        application.open(url, options: [:]) { [weak self] isOpened in
            guard let self else { return }

            if isOpened {
                extensionContext?.completeRequest(returningItems: nil)
            } else {
                showOpenFailedAlert()
            }
        }
    }

    private func showOpenFailedAlert() {
        showAlert(
            title: String(localized: "Couldn't open the app", comment: "Title of the alert shown when the share extension could not open the app"),
            message: String(localized: "Please try sharing again.", comment: "Message of the alert shown when the share extension could not open the app")
        )
    }

    // 공유 내용은 남기지 않으므로 다시 시도하는 버튼 없이 확인 버튼 하나만 두고, 누르면 경고와 공유 시트를 함께 닫는다.
    private func showAlert(title: String, message: String) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let confirm = UIAlertAction(
            title: String(localized: "OK", comment: "Button that closes the share alert and the share sheet"),
            style: .default
        ) { [weak self] _ in
            self?.extensionContext?.completeRequest(returningItems: nil)
        }
        alert.addAction(confirm)
        alert.preferredAction = confirm
        present(alert, animated: true)
    }

    private static func application(from responder: UIResponder) -> UIApplication? {
        var current: UIResponder? = responder
        while let next = current {
            if let application = next as? UIApplication {
                return application
            }
            current = next.next
        }
        return nil
    }

    // 개발용과 운영용 앱을 함께 설치해도 자기를 담은 앱을 열도록, 앱이 자기 번들 식별자로 등록한 scheme을 쓴다. 앱은 scheme을 diary로 바꿔 연다.
    private static func deepLink(content: ShareContent) -> URL? {
        guard
            let appScheme = containingAppBundleIdentifier,
            let kind = percentEncoded(content.kind.rawValue),
            let text = percentEncoded(content.text),
            let subject = percentEncoded(content.subject ?? "")
        else { return nil }

        return URL(string: "\(appScheme)://\(shareHost)?\(kindKey)=\(kind)&\(textKey)=\(text)&\(subjectKey)=\(subject)")
    }

    // 공유 확장의 번들 식별자는 앱의 번들 식별자 뒤에 .share를 붙인 것이다.
    private static var containingAppBundleIdentifier: String? {
        guard let identifier = Bundle.main.bundleIdentifier, identifier.hasSuffix(extensionSuffix) else { return nil }

        return String(identifier.dropLast(extensionSuffix.count))
    }

    // 받는 쪽이 +를 공백으로 읽는 해석기여도 값이 바뀌지 않도록 비예약 문자만 남기고 모두 인코딩한다.
    private static func percentEncoded(_ value: String) -> String? {
        value.addingPercentEncoding(withAllowedCharacters: unreservedCharacters)
    }

    private static let unreservedCharacters = CharacterSet(charactersIn: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~")
    private static let extensionSuffix = ".share"
    private static let shareHost = "share"
    private static let kindKey = "kind"
    private static let textKey = "text"
    private static let subjectKey = "subject"
}
