import AppIntents
import DiaryIos

// 한 디데이가 범위 안의 여러 날짜에 나타나므로 id가 겹치지 않는 일시 항목으로 돌려주고, 디데이 자체는 값으로 담는다.
struct CalendarDDayEntity: TransientAppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Calendar D-Day", table: "AppIntentCalendar"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Calendar D-Days", table: "AppIntentCalendar")
    )

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("Date", table: "AppIntentCalendar"))
    var date: Date

    @Property(title: LocalizedStringResource("Date Label", table: "AppIntentCalendar"))
    var dateLabel: String

    @Property(title: LocalizedStringResource("D-Day", table: "AppIntentDDay"))
    var dday: DDayEntity?

    init() {}

    init(dday: DiaryAppActionCalendarDDay, entity: DDayEntity?) {
        title = dday.title
        color = dday.color
        date = Date(epochMillis: dday.dateEpochMillis)
        dateLabel = CalendarDDayEntity.dateLabel(kind: dday.labelKind, count: dday.labelCount.map(Int.init) ?? 0)
        self.dday = entity
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: LocalizedStringResource("🎉 \(title) \(dateLabel)", table: "AppIntentCalendar"),
            subtitle: "\(date.formatted(date: .abbreviated, time: .omitted))",
            image: AppIntentItemImage.symbol("calendar", hexColor: color)
        )
    }

    private static func dateLabel(kind: DiaryAppActionCalendarDDayLabelKind, count: Int) -> String {
        switch kind {
        case .BASE_DAY: DDayLabel.dDay
        case .DAYS: DDayLabel.days(count)
        case .MONTHS: DDayLabel.months(count)
        case .YEARS: DDayLabel.anniversaryYears(count)
        }
    }
}
