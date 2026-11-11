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

    // 목록 결과에는 데스크톱 연결과 같이 연결된 태그가 없으므로, 태그는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    init(qr: DiaryAppActionQr) {
        self.init(qr: qr, tags: [])
    }

    init(detail: DiaryAppActionQrDetail) {
        self.init(qr: detail.qr, tags: detail.tags)
    }

    private init(qr: DiaryAppActionQr, tags: [DiaryAppActionTag]) {
        id = qr.id
        title = qr.title
        qrDescription = qr.qrDescription
        value = qr.value
        createdAt = Date(epochMillis: qr.createdAtEpochMillis)
        updatedAt = Date(epochMillis: qr.updatedAtEpochMillis)
        self.tags = tags.map(TagEntity.init(tag:))
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
    // ‘삭제한 QR 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [QrEntity.ID]) async throws -> [QrEntity] {
        var entityList: [QrEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryQrAppAction.shared.getIncludingDeleted(qrId: identifier) }) {
                entityList.append(QrEntity(detail: detail))
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

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(qrId: String) async throws -> QrEntity {
        guard let detail = try await appAction({ try await DiaryQrAppAction.shared.get(qrId: qrId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the QR code.", table: "AppIntentQr"))
        }
        return QrEntity(detail: detail)
    }
}
