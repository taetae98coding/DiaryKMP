import AppIntents
import DiaryIos

struct AddQrIntent: AppIntent {
    static let title = LocalizedStringResource("Add QR Code", table: "AppIntentQr")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new QR code. Enter the exact text to encode as the content.", table: "AppIntentQr")
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the QR code title?", table: "AppIntentQr"))
    )
    var title: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var qrDescription: String?

    @Parameter(
        title: LocalizedStringResource("Content", table: "AppIntentCore"),
        description: LocalizedStringResource("The exact text to encode, such as a URL, text or Wi-Fi information.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What should the QR code contain?", table: "AppIntentQr"))
    )
    var value: String

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add QR code \(\.$title) \(\.$value)", table: "AppIntentQr") {
            \.$qrDescription
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<QrEntity> & ProvidesDialog {
        let qrId = try await appAction {
            try await DiaryQrAppAction.shared.add(title: title, qrDescription: qrDescription, value: value, tagIds: (tags ?? []).map(\.id))
        }
        return .result(
            value: try await QrEntityQuery.require(qrId: qrId),
            dialog: IntentDialog(LocalizedStringResource("Added the QR code.", table: "AppIntentQr"))
        )
    }
}

struct EditQrIntent: AppIntent {
    static let title = LocalizedStringResource("Edit QR Code", table: "AppIntentQr")
    static let description = IntentDescription(
        LocalizedStringResource("Changes the title, description or content of a QR code. Values left empty stay the same.", table: "AppIntentQr")
    )

    @Parameter(title: LocalizedStringResource("QR Code", table: "AppIntentQr"))
    var qr: QrEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var qrDescription: String?

    @Parameter(
        title: LocalizedStringResource("Content", table: "AppIntentCore"),
        description: LocalizedStringResource("The exact text to encode, such as a URL, text or Wi-Fi information.", table: "AppIntentCore")
    )
    var value: String?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$qr)", table: "AppIntentQr") {
            \.$title
            \.$qrDescription
            \.$value
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await QrEntityQuery.require(qrId: qr.id)
        let changedCount = try await appAction {
            try await DiaryQrAppAction.shared.update(qrId: qr.id, title: title, qrDescription: qrDescription, value: value)
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the QR code.", table: "AppIntentQr"))
        )
    }
}
