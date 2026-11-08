import AppIntents
import DiaryIos

struct QrListIntent: AppIntent {
    static let title = LocalizedStringResource("Get QR Codes", table: "AppIntentQr")
    static let description = IntentDescription(LocalizedStringResource("Gets saved QR codes by title.", table: "AppIntentQr"))

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get QR Codes", table: "AppIntentQr") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[QrEntity]> & ProvidesDialog {
        let qrPage = try await appAction { try await DiaryQrAppAction.shared.list(pageNumber: AppIntentPage.number(page)) }
        return .result(value: qrPage.items.map(QrEntity.init(qr:)), dialog: qrPage.dialog)
    }
}

struct QrDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get QR Code Details", table: "AppIntentQr")
    static let description = IntentDescription(LocalizedStringResource("Gets a saved QR code.", table: "AppIntentQr"))

    @Parameter(title: LocalizedStringResource("QR Code", table: "AppIntentQr"))
    var qr: QrEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$qr) Details", table: "AppIntentQr")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<QrEntity> {
        .result(value: try await QrEntityQuery.require(qrId: qr.id))
    }
}

private extension DiaryAppActionQrPage {
    nonisolated var dialog: IntentDialog {
        AppIntentResultDialog.list(
            count: items.count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) QR codes.", table: "AppIntentQr") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) QR codes. There's more on page \(nextPage).", table: "AppIntentQr")
            }
        )
    }
}
