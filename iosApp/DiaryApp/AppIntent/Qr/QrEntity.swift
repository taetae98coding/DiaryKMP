import AppIntents
import DiaryIos

struct QrEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("QR Code", table: "AppIntentQr"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) QR Codes", table: "AppIntentQr")
    )
    static let defaultQuery = QrEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var qrDescription: String

    @Property(title: LocalizedStringResource("Content", table: "AppIntentCore"))
    var value: String

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    init(qr: DiaryAppActionQr) {
        id = qr.id
        title = qr.title
        qrDescription = qr.qrDescription
        value = qr.value
        createdAt = Date(epochMillis: qr.createdAtEpochMillis)
        updatedAt = Date(epochMillis: qr.updatedAtEpochMillis)
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: qrDescription.appIntentNonEmpty.map { "\($0)" },
            image: AppIntentItemImage.symbol("qrcode", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}

struct QrEntityQuery: EntityStringQuery {
    func entities(for identifiers: [QrEntity.ID]) async throws -> [QrEntity] {
        var entityList: [QrEntity] = []
        for identifier in identifiers {
            if let qr = try await appAction({ try await DiaryQrAppAction.shared.get(qrId: identifier) }) {
                entityList.append(QrEntity(qr: qr))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [QrEntity] {
        try await appAction { try await DiaryQrAppAction.shared.search(query: string) }.map(QrEntity.init(qr:))
    }

    func suggestedEntities() async throws -> [QrEntity] {
        try await appAction { try await DiaryQrAppAction.shared.list(pageNumber: AppIntentPage.number(nil)) }.items.map(QrEntity.init(qr:))
    }

    static func require(qrId: String) async throws -> QrEntity {
        guard let qr = try await QrEntityQuery().entities(for: [qrId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the QR code.", table: "AppIntentQr"))
        }
        return qr
    }
}
