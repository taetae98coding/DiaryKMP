import AppIntents
import DiaryIos

struct DeleteQrIntent: AppIntent {
    static let title = LocalizedStringResource("Delete QR Code", table: "AppIntentQr")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a QR code. Run Restore Deleted QR Code with the same QR code to undo.", table: "AppIntentQr")
    )

    @Parameter(title: LocalizedStringResource("QR Code", table: "AppIntentQr"))
    var qr: QrEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$qr)", table: "AppIntentQr")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await QrEntityQuery.require(qrId: qr.id)
        let changedCount = try await appAction { try await DiaryQrAppAction.shared.delete(qrId: qr.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the QR code.", table: "AppIntentQr"))
        )
    }
}

struct RestoreQrIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted QR Code", table: "AppIntentQr")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted QR code.", table: "AppIntentQr"))

    @Parameter(title: LocalizedStringResource("QR Code to Restore", table: "AppIntentQr"))
    var qr: QrEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$qr)", table: "AppIntentQr")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryQrAppAction.shared.restore(qrId: qr.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the QR code.", table: "AppIntentQr"))
        )
    }
}

struct LinkTagToQrIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to QR Code", table: "AppIntentQr")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a QR code.", table: "AppIntentQr"))

    @Parameter(title: LocalizedStringResource("QR Code", table: "AppIntentQr"))
    var qr: QrEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentQr"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$qr)", table: "AppIntentQr")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await QrEntityQuery.require(qrId: qr.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.add(kind: .QR, entityId: qr.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromQrIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from QR Code", table: "AppIntentQr")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a QR code.", table: "AppIntentQr"))

    @Parameter(title: LocalizedStringResource("QR Code", table: "AppIntentQr"))
    var qr: QrEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentQr"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$qr)", table: "AppIntentQr")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await QrEntityQuery.require(qrId: qr.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.remove(kind: .QR, entityId: qr.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
