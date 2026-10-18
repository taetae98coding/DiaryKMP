import Foundation

// 시스템이 파일 올리기 전송의 이벤트를 모두 넘겼는지 기다린다.
final class FileUploadBackgroundEvents {
    static let shared = FileUploadBackgroundEvents()

    // 파일 올리기 전송 세션의 식별자다. core:network:impl의 BackgroundSessionFileUploadTransport가 쓰는 식별자와 같아야 한다.
    static let sessionIdentifier = "io.github.taetae98coding.diary.fileUpload"

    // 파일 올리기 전송 세션이 이벤트를 모두 전달했다고 알리는 이름이다.
    // core:network:impl의 FileUploadSessionDelegate가 보내는 이름과 같아야 한다.
    private static let finishedNotification = Notification.Name("io.github.taetae98coding.diary.fileUpload.backgroundEventsFinished")

    private var observer: NSObjectProtocol?
    private var continuation: CheckedContinuation<Void, Never>?

    // 전송 세션이 기다리기 시작하기 전에 이벤트를 모두 넘길 수 있으므로, 먼저 온 알림은 다음 기다림에서 바로 끝낸다.
    private var isFinished = false

    private init() {}

    func observe() {
        guard observer == nil else { return }

        observer = NotificationCenter.default.addObserver(
            forName: Self.finishedNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            MainActor.assumeIsolated {
                self?.finish()
            }
        }
    }

    func waitUntilFinished() async {
        if isFinished {
            isFinished = false
            return
        }

        await withCheckedContinuation { continuation in
            self.continuation = continuation
        }
    }

    private func finish() {
        guard let continuation else {
            isFinished = true
            return
        }

        self.continuation = nil
        continuation.resume()
    }
}
