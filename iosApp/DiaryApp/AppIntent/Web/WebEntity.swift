import AppIntents
import DiaryIos

struct WebEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Web", table: "AppIntentWeb"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Webs", table: "AppIntentWeb")
    )
    static let defaultQuery = WebEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var webDescription: String

    @Property(title: LocalizedStringResource("URL", table: "AppIntentCore"))
    var url: String

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 요청 헤더와 연결된 태그가 없으므로, 둘은 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Request Headers", table: "AppIntentCore"))
    var headers: [String]

    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    init(web: DiaryAppActionWeb) {
        self.init(web: web, headers: [], tags: [])
    }

    init(detail: DiaryAppActionWebDetail) {
        self.init(web: detail.web, headers: detail.headers, tags: detail.tags)
    }

    private init(web: DiaryAppActionWeb, headers: [String], tags: [DiaryAppActionTag]) {
        id = web.id
        title = web.title
        webDescription = web.webDescription
        url = web.url
        createdAt = Date(epochMillis: web.createdAtEpochMillis)
        updatedAt = Date(epochMillis: web.updatedAtEpochMillis)
        self.headers = headers
        self.tags = tags.map(TagEntity.init(tag:))
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: url.appIntentNonEmpty.map { "\($0)" },
            image: AppIntentItemImage.symbol("globe", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}
