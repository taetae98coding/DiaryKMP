import AppIntents
import DiaryIos

// 음력 날짜는 id가 없는 결과라서, 단축어가 값을 꺼내 쓰기만 하는 일시 항목으로 돌려준다.
struct LunarDateEntity: TransientAppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Lunar Date", table: "AppIntentLunar"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Lunar Dates", table: "AppIntentLunar")
    )

    @Property(title: LocalizedStringResource("Solar Date", table: "AppIntentCore"))
    var solarDate: Date

    @Property(title: LocalizedStringResource("Lunar Year", table: "AppIntentCore"))
    var lunarYear: Int

    @Property(title: LocalizedStringResource("Lunar Month", table: "AppIntentCore"))
    var lunarMonth: Int

    @Property(title: LocalizedStringResource("Lunar Day", table: "AppIntentCore"))
    var lunarDay: Int

    @Property(title: LocalizedStringResource("Leap Month", table: "AppIntentCore"))
    var isLeapMonth: Bool

    init() {}

    init(lunarDate: DiaryAppActionLunarDate) {
        solarDate = Date(epochMillis: lunarDate.solarDateEpochMillis)
        lunarYear = Int(lunarDate.lunarYear)
        lunarMonth = Int(lunarDate.lunarMonth)
        lunarDay = Int(lunarDate.lunarDay)
        isLeapMonth = lunarDate.isLeapMonth
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: isLeapMonth
                ? LocalizedStringResource("Lunar leap \(lunarMonth)/\(lunarDay)", table: "AppIntentLunar")
                : LocalizedStringResource("Lunar \(lunarMonth)/\(lunarDay)", table: "AppIntentLunar"),
            subtitle: "\(solarDate.formatted(date: .abbreviated, time: .omitted))",
            image: AppIntentItemImage.symbol("moon", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}
