import AppIntents
import DiaryIos

struct MemoEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Memo", table: "AppIntentMemo"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Memos", table: "AppIntentMemo")
    )
    static let defaultQuery = MemoEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var memoDescription: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("All Day", table: "AppIntentCore"))
    var allDay: Bool?

    @Property(title: LocalizedStringResource("Start", table: "AppIntentCore"))
    var start: Date?

    @Property(title: LocalizedStringResource("End", table: "AppIntentCore"))
    var end: Date?

    @Property(title: LocalizedStringResource("Finished", table: "AppIntentCore"))
    var isFinished: Bool

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 연결된 항목이 없으므로, 대표 태그와 연결된 항목은 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Primary Tag", table: "AppIntentCore"))
    var primaryTag: TagEntity?

    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    @Property(title: LocalizedStringResource("Places", table: "AppIntentCore"))
    var places: [PlaceEntity]

    @Property(title: LocalizedStringResource("Webs", table: "AppIntentCore"))
    var webs: [WebEntity]

    @Property(title: LocalizedStringResource("Contacts", table: "AppIntentCore"))
    var contacts: [ContactEntity]

    init(memo: DiaryAppActionMemo) {
        id = memo.id
        title = memo.title
        memoDescription = memo.memoDescription
        color = memo.color
        allDay = memo.dateTime?.allDay
        start = memo.dateTime.map { dateTime in Date(epochMillis: dateTime.startEpochMillis) }
        end = memo.dateTime.map { dateTime in Date(epochMillis: dateTime.endInclusiveEpochMillis) }
        isFinished = memo.isFinished
        createdAt = Date(epochMillis: memo.createdAtEpochMillis)
        updatedAt = Date(epochMillis: memo.updatedAtEpochMillis)
        primaryTag = nil
        tags = []
        places = []
        webs = []
        contacts = []
    }

    init(detail: DiaryAppActionMemoDetail) {
        self.init(memo: detail.memo)
        tags = detail.tags.map(TagEntity.init(tag:))
        primaryTag = tags.first { tag in tag.id == detail.memo.primaryTagId }
        places = detail.places.map(PlaceEntity.init(place:))
        webs = detail.webs.map(WebEntity.init(web:))
        contacts = detail.contacts.map(ContactEntity.init(contact:))
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: subtitle.map { "\($0)" },
            image: AppIntentItemImage.symbol("note.text", hexColor: color)
        )
    }

    private var subtitle: String? {
        if let allDay, let start, let end {
            return AppIntentPeriod.text(allDay: allDay, start: start, end: end)
        }
        return memoDescription.appIntentNonEmpty
    }
}

struct MemoEntityQuery: EntityStringQuery {
    func entities(for identifiers: [MemoEntity.ID]) async throws -> [MemoEntity] {
        var entityList: [MemoEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryMemoAppAction.shared.get(memoId: identifier) }) {
                entityList.append(MemoEntity(detail: detail))
            }
        }
        return entityList
    }

    // 선택 목록의 검색 칸은 검색 화면의 메모 결과와 같은 메모 검색의 첫 페이지를 보여 준다.
    func entities(matching string: String) async throws -> [MemoEntity] {
        try await appAction {
            try await DiarySearchAppAction.shared.memo(query: string, sort: nil, pageNumber: AppIntentPage.number(nil))
        }.items.map(MemoEntity.init(memo:))
    }

    func suggestedEntities() async throws -> [MemoEntity] {
        let firstPage = AppIntentPage.number(nil)
        let inProgress = try await appAction { try await DiaryMemoAppAction.shared.list(sort: nil, pageNumber: firstPage) }
        let finished = try await appAction { try await DiaryMemoAppAction.shared.finishedList(sort: nil, pageNumber: firstPage) }
        return (inProgress.items + finished.items).map(MemoEntity.init(memo:))
    }

    static func require(memoId: String) async throws -> MemoEntity {
        guard let memo = try await MemoEntityQuery().entities(for: [memoId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the memo.", table: "AppIntentMemo"))
        }
        return memo
    }
}
