import AppIntents
import DiaryIos

struct CalendarMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Calendar Memos", table: "AppIntentCalendar")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Gets the memos shown on the calendar whose period overlaps the date range. Finished memos are included, and the calendar tag filter chosen in the app applies.",
            table: "AppIntentCalendar"
        )
    )

    @Parameter(title: LocalizedStringResource("Start Date", table: "AppIntentCore"), kind: .date)
    var startDate: Date

    @Parameter(
        title: LocalizedStringResource("End Date", table: "AppIntentCore"),
        description: LocalizedStringResource("The end date is included.", table: "AppIntentCore"),
        kind: .date
    )
    var endDate: Date

    static var parameterSummary: some ParameterSummary {
        Summary("Calendar memos from \(\.$startDate) to \(\.$endDate)", table: "AppIntentCalendar")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[CalendarMemoEntity]> & ProvidesDialog {
        let memoList = try await CalendarMemoList.read(startDate: startDate, endDate: endDate)
        return .result(value: memoList, dialog: CalendarMemoList.dialog(count: memoList.count))
    }
}

struct CalendarMemoByDateIntent: AppIntent {
    static let title = LocalizedStringResource("Get Calendar Memos by Date", table: "AppIntentCalendar")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Gets the memos shown on the calendar on the chosen date. Finished memos are included, and the calendar tag filter chosen in the app applies.",
            table: "AppIntentCalendar"
        )
    )

    @Parameter(
        title: LocalizedStringResource("Date", table: "AppIntentCalendar"),
        kind: .date,
        requestValueDialog: IntentDialog(LocalizedStringResource("Which date?", table: "AppIntentCalendar"))
    )
    var date: Date

    static var parameterSummary: some ParameterSummary {
        Summary("Calendar memos on \(\.$date)", table: "AppIntentCalendar")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[CalendarMemoEntity]> & ProvidesDialog {
        let memoList = try await CalendarMemoList.read(startDate: date, endDate: date)
        return .result(value: memoList, dialog: CalendarMemoList.dialog(count: memoList.count))
    }
}

private nonisolated enum CalendarMemoList {
    static func read(startDate: Date, endDate: Date) async throws -> [CalendarMemoEntity] {
        try await appAction {
            try await DiaryCalendarAppAction.shared.memoList(startDateEpochMillis: startDate.epochMillis, endDateEpochMillis: endDate.epochMillis)
        }.map(CalendarMemoEntity.init(memo:))
    }

    static func dialog(count: Int) -> IntentDialog {
        AppIntentResultDialog.list(
            count: count,
            nextPageNumber: nil,
            found: { count in LocalizedStringResource("Found \(count) calendar memos.", table: "AppIntentCalendar") },
            foundWithNextPage: { count, _ in LocalizedStringResource("Found \(count) calendar memos.", table: "AppIntentCalendar") }
        )
    }
}

struct CalendarBirthdayListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Calendar Birthdays", table: "AppIntentCalendar")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Gets contact birthdays in the date range as solar dates. Lunar birthdays appear only for years whose lunar data is saved.",
            table: "AppIntentCalendar"
        )
    )

    @Parameter(title: LocalizedStringResource("Start Date", table: "AppIntentCore"), kind: .date)
    var startDate: Date

    @Parameter(
        title: LocalizedStringResource("End Date", table: "AppIntentCore"),
        description: LocalizedStringResource("The end date is included.", table: "AppIntentCore"),
        kind: .date
    )
    var endDate: Date

    static var parameterSummary: some ParameterSummary {
        Summary("Calendar birthdays from \(\.$startDate) to \(\.$endDate)", table: "AppIntentCalendar")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[BirthdayEntity]> & ProvidesDialog {
        let birthdayList = try await appAction {
            try await DiaryCalendarAppAction.shared.birthdayList(startDateEpochMillis: startDate.epochMillis, endDateEpochMillis: endDate.epochMillis)
        }.map(BirthdayEntity.init(birthday:))
        return .result(
            value: birthdayList,
            dialog: AppIntentResultDialog.list(
                count: birthdayList.count,
                nextPageNumber: nil,
                found: { count in LocalizedStringResource("Found \(count) birthdays.", table: "AppIntentCalendar") },
                foundWithNextPage: { count, _ in LocalizedStringResource("Found \(count) birthdays.", table: "AppIntentCalendar") }
            )
        )
    }
}

struct HolidayListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Holidays", table: "AppIntentCalendar")
    static let description = IntentDescription(
        LocalizedStringResource("Gets a year's holidays as shown on the calendar. Only years saved on this device appear.", table: "AppIntentCalendar")
    )

    @Parameter(title: LocalizedStringResource("Year", table: "AppIntentCore"))
    var year: Int

    static var parameterSummary: some ParameterSummary {
        Summary("Get holidays in \(\.$year)", table: "AppIntentCalendar")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[HolidayEntity]> & ProvidesDialog {
        let holidayList = try await appAction {
            try await DiaryCalendarAppAction.shared.holidayList(year: Int32(clamping: year))
        }.map(HolidayEntity.init(holiday:))
        return .result(
            value: holidayList,
            dialog: AppIntentResultDialog.list(
                count: holidayList.count,
                nextPageNumber: nil,
                found: { count in LocalizedStringResource("Found \(count) holidays.", table: "AppIntentCalendar") },
                foundWithNextPage: { count, _ in LocalizedStringResource("Found \(count) holidays.", table: "AppIntentCalendar") }
            )
        )
    }
}
