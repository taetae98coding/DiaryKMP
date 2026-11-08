import AppIntents
import DiaryIos
import KotlinRuntimeSupport

// Siri와 단축어는 perform()이 던진 오류의 localizedStringResource를 그대로 실패 문구로 보여 준다.
nonisolated enum AppIntentError: Error, CustomLocalizedStringResourceConvertible {
    case failure(reason: String)
    case notFound(LocalizedStringResource)

    var localizedStringResource: LocalizedStringResource {
        switch self {
        case let .failure(reason):
            LocalizedStringResource("Couldn't complete the action. \(reason)", table: "AppIntentCore")
        case let .notFound(message):
            message
        }
    }
}

// Kotlin suspend 함수는 Swift export에서 `async throws`가 되고 Kotlin 예외는 KotlinError(wrapped:)로 던져진다.
// 실패 사유는 데스크톱 연결이 알리는 내용과 같은 Kotlin 예외의 message다.
nonisolated func appAction<T>(_ operation: () async throws -> T) async throws -> T {
    do {
        return try await operation()
    } catch let error as KotlinError {
        let reason = DiaryAppActionFailure.shared.reason(error: error.wrapped) ?? error.description
        throw AppIntentError.failure(reason: reason)
    }
}
