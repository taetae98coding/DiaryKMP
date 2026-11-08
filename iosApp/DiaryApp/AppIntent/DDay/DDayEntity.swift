import AppIntents
import DiaryIos

struct DDayEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("D-Day", table: "AppIntentDDay"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) D-Days", table: "AppIntentDDay")
    )
    static let defaultQuery = DDayEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var ddayDescription: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("Start Date", table: "AppIntentDDay"))
    var baseDate: Date

    @Property(title: LocalizedStringResource("Count Start Date as Day 1", table: "AppIntentDDay"))
    var countsBaseDay: Bool

    @Property(title: LocalizedStringResource("Repeat Intervals", table: "AppIntentDDay"))
    var repeats: [String]

    @Property(title: LocalizedStringResource("End Date", table: "AppIntentDDay"))
    var endDate: Date?

    @Property(title: LocalizedStringResource("Missing Dates", table: "AppIntentDDay"))
    var overflowPolicy: DDayOverflowPolicyAppEnum

    @Property(title: LocalizedStringResource("Status", table: "AppIntentDDay"))
    var status: DDayStatusAppEnum

    @Property(title: LocalizedStringResource("Days Left", table: "AppIntentDDay"))
    var remainingDays: Int?

    @Property(title: LocalizedStringResource("Days Passed", table: "AppIntentDDay"))
    var passedDays: Int?

    @Property(title: LocalizedStringResource("Full Years", table: "AppIntentDDay"))
    var years: Int?

    @Property(title: LocalizedStringResource("Full Months", table: "AppIntentDDay"))
    var months: Int?

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 연결된 태그가 없으므로, 태그는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    init(dday: DiaryAppActionDDay) {
        self.init(dday: dday, tags: [])
    }

    init(detail: DiaryAppActionDDayDetail) {
        self.init(dday: detail.dday, tags: detail.tags)
    }

    private init(dday: DiaryAppActionDDay, tags: [DiaryAppActionTag]) {
        id = dday.id
        title = dday.title
        ddayDescription = dday.ddayDescription
        color = dday.color
        baseDate = Date(epochMillis: dday.baseDateEpochMillis)
        countsBaseDay = dday.countsBaseDay
        repeats = dday.repeats
        endDate = dday.endDateEpochMillis.map(Date.init(epochMillis:))
        overflowPolicy = DDayOverflowPolicyAppEnum(kotlin: dday.overflowPolicy)
        status = DDayStatusAppEnum(kotlin: dday.status)
        remainingDays = dday.remainingDays.map(Int.init)
        passedDays = dday.passedDays.map(Int.init)
        years = dday.years.map(Int.init)
        months = dday.months.map(Int.init)
        createdAt = Date(epochMillis: dday.createdAtEpochMillis)
        updatedAt = Date(epochMillis: dday.updatedAtEpochMillis)
        self.tags = tags.map(TagEntity.init(tag:))
    }

    // 카드 문구는 동작을 실행한 시점의 오늘로 계산한 값이다.
    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: "\(DDayLabel.card(status: status, remainingDays: remainingDays, passedDays: passedDays, years: years, months: months))",
            image: AppIntentItemImage.symbol("hourglass", hexColor: color)
        )
    }
}
