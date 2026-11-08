import AppIntents
import DiaryIos

struct CalendarMemoEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Calendar Memo", table: "AppIntentCalendar"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Calendar Memos", table: "AppIntentCalendar")
    )
    static let defaultQuery = CalendarMemoEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("All Day", table: "AppIntentCore"))
    var allDay: Bool

    @Property(title: LocalizedStringResource("Start", table: "AppIntentCore"))
    var start: Date

    @Property(title: LocalizedStringResource("End", table: "AppIntentCore"))
    var end: Date

    init(memo: DiaryAppActionCalendarMemo) {
        id = memo.id
        title = memo.title
        color = memo.color
        allDay = memo.dateTime.allDay
        start = Date(epochMillis: memo.dateTime.startEpochMillis)
        end = Date(epochMillis: memo.dateTime.endInclusiveEpochMillis)
    }

    init?(memo: DiaryAppActionMemo) {
        guard let dateTime = memo.dateTime else { return nil }
        id = memo.id
        title = memo.title
        color = memo.color
        allDay = dateTime.allDay
        start = Date(epochMillis: dateTime.startEpochMillis)
        end = Date(epochMillis: dateTime.endInclusiveEpochMillis)
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: "\(AppIntentPeriod.text(allDay: allDay, start: start, end: end))",
            image: AppIntentItemImage.symbol("calendar", hexColor: color)
        )
    }
}

// 캘린더 메모는 메모이므로, 저장된 항목은 메모 상세로 다시 찾는다. 기간이 없어진 메모는 캘린더에 보이지 않으므로 찾지 못한다.
struct CalendarMemoEntityQuery: EntityQuery {
    func entities(for identifiers: [CalendarMemoEntity.ID]) async throws -> [CalendarMemoEntity] {
        var entityList: [CalendarMemoEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryMemoAppAction.shared.get(memoId: identifier) }),
               let entity = CalendarMemoEntity(memo: detail.memo)
            {
                entityList.append(entity)
            }
        }
        return entityList
    }
}
