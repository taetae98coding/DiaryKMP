import Foundation
import UniformTypeIdentifiers

// core/navigation의 ScreenDeepLink.SHARE_KIND_* 값과 같아야 한다.
enum ShareContentKind: String, Sendable {
    case text
    case contact
    case event
}

struct ShareContent: Sendable {
    let kind: ShareContentKind
    let text: String
    let subject: String?
}

enum ShareReadResult: Sendable {
    case content(ShareContent)
    case unsupported
    case unreadable
}

// 공유 하나에서 앱에 넘길 원문을 꺼낸다. 원문을 다듬거나 어느 화면을 열지 정하는 일은 앱이 하므로 여기서는 받은 그대로 넘긴다.
enum ShareContentReader {
    // 활성화 규칙이 공유 하나만 받으므로 첫 첨부를 쓰고, 파일과 글이 함께 오면 파일을 쓴다.
    static func read(items: [NSExtensionItem]) async -> ShareReadResult {
        let attachments = items.flatMap { item in (item.attachments ?? []).map { (item, $0) } }

        if let (_, provider) = attachments.first(where: { conforms($0.1, to: [UTType.vCard]) }) {
            return await readFileText(provider: provider, types: [UTType.vCard], kind: .contact) { removingPhoto(vCard: $0) }
        }
        if let (_, provider) = attachments.first(where: { conforms($0.1, to: calendarTypes) }) {
            return await readFileText(provider: provider, types: calendarTypes, kind: .event) { $0 }
        }
        if attachments.contains(where: { !isReceivable($0.1) }) {
            return .unsupported
        }
        if let (item, provider) = attachments.first(where: { isWebUrl($0.1) }) {
            guard let url = try? await loadUrl(provider: provider) else { return .unreadable }

            let text = url.absoluteString
            return .content(ShareContent(kind: .text, text: text, subject: subject(of: item, excluding: text, includesContentText: true)))
        }
        if let (item, provider) = attachments.first(where: { conforms($0.1, to: [UTType.plainText]) }) {
            let text: String
            do {
                text = try await loadText(provider: provider)
            } catch is TextFileError {
                return .unsupported
            } catch {
                return .unreadable
            }

            // 글을 공유하면 많은 앱이 글 자체를 내용 문구에도 담으므로, 제목에는 항목 제목만 쓴다.
            return .content(ShareContent(kind: .text, text: text, subject: subject(of: item, excluding: text, includesContentText: false)))
        }
        return .unreadable
    }

    // 활성화 규칙은 글이 하나 있으면 다른 첨부의 종류를 보지 않으므로, 연락처·일정·웹 주소·글이 아닌 첨부가 함께 왔는지 여기서 확인한다.
    private static func isReceivable(_ provider: NSItemProvider) -> Bool {
        conforms(provider, to: [UTType.vCard] + calendarTypes) || isWebUrl(provider) || (conforms(provider, to: [UTType.plainText]) && !conforms(provider, to: [UTType.fileURL]))
    }

    private static let calendarTypes = [UTType.calendarEvent, UTType("com.apple.ical.ics")].compactMap { $0 }

    private static func conforms(_ provider: NSItemProvider, to types: [UTType]) -> Bool {
        typeIdentifier(of: provider, conformingTo: types) != nil
    }

    private static func typeIdentifier(of provider: NSItemProvider, conformingTo types: [UTType]) -> String? {
        provider.registeredTypeIdentifiers.first { identifier in
            guard let type = UTType(identifier) else { return false }

            return types.contains { type.conforms(to: $0) }
        }
    }

    // 파일 주소도 public.url을 따르므로, 웹 주소만 글로 받는다. 활성화 규칙도 같은 기준이다.
    private static func isWebUrl(_ provider: NSItemProvider) -> Bool {
        conforms(provider, to: [UTType.url]) && !conforms(provider, to: [UTType.fileURL])
    }

    // 공유 제목은 항목 제목을 먼저 쓰고, 링크 공유에서는 브라우저가 페이지 제목을 담는 내용 문구도 쓴다. 공유한 글과 같은 문구는 제목이 아니다.
    private static func subject(of item: NSExtensionItem, excluding text: String, includesContentText: Bool) -> String? {
        let candidates = [item.attributedTitle?.string, includesContentText ? item.attributedContentText?.string : nil]

        return candidates.lazy
            .compactMap { $0 }
            .first { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && $0 != text }
    }

    private static func readFileText(
        provider: NSItemProvider,
        types: [UTType],
        kind: ShareContentKind,
        transform: (String) -> String
    ) async -> ShareReadResult {
        guard
            let identifier = typeIdentifier(of: provider, conformingTo: types),
            let data = try? await loadData(provider: provider, typeIdentifier: identifier),
            let text = decodedText(data)
        else { return .unreadable }

        return .content(ShareContent(kind: kind, text: transform(text), subject: nil))
    }

    // 연락처 사진은 base64로 길게 담겨 앱에 넘기는 주소를 크게 만들고 앱도 쓰지 않으므로, 이어지는 줄까지 PHOTO 속성을 뺀다.
    private nonisolated static func removingPhoto(vCard: String) -> String {
        var isInPhoto = false

        // Swift 문자열은 \r\n을 한 글자로 보므로, 줄 끝 표기를 그대로 두려고 UTF-16 단위로 자르는 Foundation 분리를 쓴다.
        return vCard
            .components(separatedBy: "\n")
            .filter { line in
                if line.first == " " || line.first == "\t" {
                    return !isInPhoto
                }

                isInPhoto = isPhotoProperty(line)
                return !isInPhoto
            }
            .joined(separator: "\n")
    }

    // 속성 이름 앞에는 item1.처럼 묶음 이름이 붙을 수 있고, 이름 뒤에는 매개변수(;)나 값(:)이 온다.
    private nonisolated static func isPhotoProperty(_ line: String) -> Bool {
        guard let end = line.firstIndex(where: { $0 == ";" || $0 == ":" }) else { return false }

        let name = line[..<end].split(separator: ".").last ?? ""
        return name.uppercased() == "PHOTO"
    }

    private nonisolated static func decodedText(_ data: Data) -> String? {
        String(data: data, encoding: .utf8) ?? String(data: data, encoding: .utf16)
    }

    private static func loadData(provider: NSItemProvider, typeIdentifier: String) async throws -> Data {
        try await withCheckedThrowingContinuation { continuation in
            _ = provider.loadDataRepresentation(forTypeIdentifier: typeIdentifier) { @Sendable data, error in
                if let data {
                    continuation.resume(returning: data)
                } else {
                    continuation.resume(throwing: error ?? CocoaError(.fileReadUnknown))
                }
            }
        }
    }

    private static func loadUrl(provider: NSItemProvider) async throws -> URL {
        try await withCheckedThrowingContinuation { continuation in
            provider.loadItem(forTypeIdentifier: UTType.url.identifier) { @Sendable item, error in
                switch item {
                case let url as URL:
                    continuation.resume(returning: url)
                case let string as String:
                    if let url = URL(string: string) {
                        continuation.resume(returning: url)
                    } else {
                        continuation.resume(throwing: CocoaError(.fileReadCorruptFile))
                    }
                case let data as Data:
                    if let url = URL(dataRepresentation: data, relativeTo: nil) {
                        continuation.resume(returning: url)
                    } else {
                        continuation.resume(throwing: CocoaError(.fileReadCorruptFile))
                    }
                default:
                    continuation.resume(throwing: error ?? CocoaError(.fileReadUnknown))
                }
            }
        }
    }

    // 글은 앱에 따라 문자열, 서식 있는 글, 데이터나 파일 주소로 온다.
    private static func loadText(provider: NSItemProvider) async throws -> String {
        try await withCheckedThrowingContinuation { continuation in
            provider.loadItem(forTypeIdentifier: UTType.plainText.identifier) { @Sendable item, error in
                let text: String? = switch item {
                case let string as String:
                    string
                case let attributed as NSAttributedString:
                    attributed.string
                case let data as Data:
                    decodedText(data)
                default:
                    nil
                }

                if let text {
                    continuation.resume(returning: text)
                } else if let url = item as? URL, url.isFileURL {
                    continuation.resume(throwing: TextFileError())
                } else {
                    continuation.resume(throwing: error ?? CocoaError(.fileReadUnknown))
                }
            }
        }
    }
}

// 글 형식이지만 파일로 넘어온 공유다. 글 파일은 받지 않는 파일이므로 읽지 못한 것과 구분한다.
private struct TextFileError: Error {}
