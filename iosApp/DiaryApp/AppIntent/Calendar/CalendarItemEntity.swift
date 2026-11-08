import AppIntents
import DiaryIos

// 생일과 공휴일은 id가 없는 결과라서, 단축어가 값을 꺼내 쓰기만 하는 일시 항목으로 돌려준다.
struct BirthdayEntity: TransientAppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Birthday", table: "AppIntentCalendar"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Birthdays", table: "AppIntentCalendar")
    )

    @Property(title: LocalizedStringResource("Name", table: "AppIntentCore"))
    var name: String

    @Property(title: LocalizedStringResource("How We Met", table: "AppIntentCore"))
    var nameQualifier: String

    @Property(title: LocalizedStringResource("Birthday", table: "AppIntentCore"))
    var date: Date

    init() {}

    init(birthday: DiaryAppActionCalendarBirthday) {
        name = birthday.name
        nameQualifier = birthday.nameQualifier
        date = Date(epochMillis: birthday.dateEpochMillis)
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: nameQualifier.isEmpty
                ? LocalizedStringResource("🎂 \(name)", table: "AppIntentCalendar")
                : LocalizedStringResource("🎂 \(name) (\(nameQualifier))", table: "AppIntentCalendar"),
            subtitle: "\(date.formatted(date: .abbreviated, time: .omitted))",
            image: AppIntentItemImage.symbol("gift", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}

struct HolidayEntity: TransientAppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Holiday", table: "AppIntentCalendar"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Holidays", table: "AppIntentCalendar")
    )

    @Property(title: LocalizedStringResource("Name", table: "AppIntentCore"))
    var name: String

    @Property(title: LocalizedStringResource("Day Off", table: "AppIntentCore"))
    var isHoliday: Bool

    @Property(title: LocalizedStringResource("Start Date", table: "AppIntentCore"))
    var start: Date

    @Property(title: LocalizedStringResource("End Date", table: "AppIntentCore"))
    var end: Date

    init() {}

    init(holiday: DiaryAppActionHoliday) {
        name = holiday.name
        isHoliday = holiday.isHoliday
        start = Date(epochMillis: holiday.startEpochMillis)
        end = Date(epochMillis: holiday.endInclusiveEpochMillis)
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(name)",
            subtitle: "\(AppIntentPeriod.text(allDay: true, start: start, end: end))",
            image: AppIntentItemImage.symbol("flag", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}
