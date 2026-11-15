import AppIntents
import DiaryIos

struct RoutineEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Routine", table: "AppIntentRoutine"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Routines", table: "AppIntentRoutine")
    )
    static let defaultQuery = RoutineEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var routineDescription: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("Start Date", table: "AppIntentRoutine"))
    var startDate: Date

    @Property(title: LocalizedStringResource("Repeat", table: "AppIntentRoutine"))
    var cycle: RoutineCycleAppEnum

    @Property(title: LocalizedStringResource("Unit", table: "AppIntentRoutine"))
    var unit: String

    @Property(title: LocalizedStringResource("Goal", table: "AppIntentRoutine"))
    var goalAmount: Int?

    @Property(title: LocalizedStringResource("Amount per Record", table: "AppIntentRoutine"))
    var amountPerRecord: Int

    @Property(title: LocalizedStringResource("Finished", table: "AppIntentCore"))
    var isFinished: Bool

    @Property(title: LocalizedStringResource("Status", table: "AppIntentRoutine"))
    var status: RoutineStatusAppEnum

    @Property(title: LocalizedStringResource("Period Total", table: "AppIntentRoutine"))
    var cycleAmount: Int?

    @Property(title: LocalizedStringResource("Progress", table: "AppIntentRoutine"))
    var achievementRate: Int?

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 연결된 태그가 없으므로, 태그는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    init(routine: DiaryAppActionRoutine) {
        self.init(routine: routine, tags: [])
    }

    init(detail: DiaryAppActionRoutineDetail) {
        self.init(routine: detail.routine, tags: detail.tags)
    }

    private init(routine: DiaryAppActionRoutine, tags: [DiaryAppActionTag]) {
        id = routine.id
        title = routine.title
        routineDescription = routine.routineDescription
        color = routine.color
        startDate = Date(epochMillis: routine.startDateEpochMillis)
        cycle = RoutineCycleAppEnum(kotlin: routine.cycle)
        unit = routine.unit
        goalAmount = routine.goalAmount.map(Int.init)
        amountPerRecord = Int(routine.amountPerRecord)
        isFinished = routine.isFinished
        status = RoutineStatusAppEnum(kotlin: routine.status)
        cycleAmount = routine.cycleAmount.map(Int.init)
        achievementRate = routine.achievementRate.map(Int.init)
        createdAt = Date(epochMillis: routine.createdAtEpochMillis)
        updatedAt = Date(epochMillis: routine.updatedAtEpochMillis)
        self.tags = tags.map(TagEntity.init(tag:))
    }

    // 카드 문구와 달성률은 동작을 실행한 시점의 오늘로 계산한 값이다.
    var displayRepresentation: DisplayRepresentation {
        let card = RoutineLabel.card(isFinished: isFinished, status: status, cycle: cycle, unit: unit, cycleAmount: cycleAmount, goalAmount: goalAmount)
        return DisplayRepresentation(
            title: "\(title)",
            subtitle: "\(RoutineLabel.cardWithRate(card: card, achievementRate: achievementRate))",
            image: AppIntentItemImage.symbol("repeat", hexColor: color)
        )
    }
}

struct RoutineRecordEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Routine Record", table: "AppIntentRoutine"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Routine Records", table: "AppIntentRoutine")
    )
    static let defaultQuery = RoutineRecordEntityQuery()

    // 기록 하나를 찾는 동작이 없어 기록은 그 루틴의 기록 목록에서만 찾을 수 있으므로, 식별자에 루틴 id를 함께 담는다.
    let id: String

    @Property(title: LocalizedStringResource("Date", table: "AppIntentRoutine"))
    var date: Date

    @Property(title: LocalizedStringResource("Amount", table: "AppIntentRoutine"))
    var amount: Int

    @Property(title: LocalizedStringResource("Unit", table: "AppIntentRoutine"))
    var unit: String

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    init(record: DiaryAppActionRoutineRecord) {
        id = RoutineRecordEntityID(routineId: record.routineId, recordId: record.id).rawValue
        date = Date(epochMillis: record.dateEpochMillis)
        amount = Int(record.amount)
        unit = record.unit
        createdAt = Date(epochMillis: record.createdAtEpochMillis)
        updatedAt = Date(epochMillis: record.updatedAtEpochMillis)
    }

    nonisolated var recordId: String {
        RoutineRecordEntityID(rawValue: id)?.recordId ?? id
    }

    nonisolated var routineId: String? {
        RoutineRecordEntityID(rawValue: id)?.routineId
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(RoutineLabel.amount(amount, unit: unit))",
            subtitle: "\(date.formatted(date: .abbreviated, time: .omitted))",
            image: AppIntentItemImage.symbol("checkmark.circle", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}

nonisolated struct RoutineRecordEntityID {
    let routineId: String
    let recordId: String

    init(routineId: String, recordId: String) {
        self.routineId = routineId
        self.recordId = recordId
    }

    init?(rawValue: String) {
        let parts = rawValue.split(separator: "/", maxSplits: 1).map(String.init)
        guard parts.count == 2 else { return nil }
        routineId = parts[0]
        recordId = parts[1]
    }

    var rawValue: String {
        "\(routineId)/\(recordId)"
    }
}
