import AppIntents
import DiaryIos

struct AddContactIntent: AppIntent {
    static let title = LocalizedStringResource("Add Contact", table: "AppIntentContact")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new contact. The new contact isn't a favorite.", table: "AppIntentContact")
    )

    @Parameter(
        title: LocalizedStringResource("Name", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current name.", table: "AppIntentCore")
    )
    var name: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var contactDescription: String?

    @Parameter(
        title: LocalizedStringResource("Height (cm)", table: "AppIntentCore"),
        description: LocalizedStringResource("Up to one decimal place, like 172.5.", table: "AppIntentCore")
    )
    var heightCm: Double?

    @Parameter(
        title: LocalizedStringResource("Shoe Size (mm)", table: "AppIntentCore"),
        description: LocalizedStringResource("From 0 to 999, like 270.", table: "AppIntentCore")
    )
    var footSizeMm: Int?

    @Parameter(
        title: LocalizedStringResource("Birthday", table: "AppIntentCore"),
        description: LocalizedStringResource(
            "For a lunar birthday, choose the lunar year, month and day as is. The birth year is set to its year.",
            table: "AppIntentCore"
        ),
        kind: .date
    )
    var birthday: Date?

    @Parameter(title: LocalizedStringResource("Birthday Calendar", table: "AppIntentCore"))
    var birthdayCalendar: ContactBirthdayCalendarAppEnum?

    @Parameter(
        title: LocalizedStringResource("Birth Year", table: "AppIntentCore"),
        description: LocalizedStringResource("Use when only the year is known.", table: "AppIntentCore")
    )
    var birthYear: Int?

    @Parameter(title: LocalizedStringResource("Hometown", table: "AppIntentCore"))
    var hometown: String?

    @Parameter(title: LocalizedStringResource("How We Met", table: "AppIntentCore"))
    var howWeMet: String?

    @Parameter(
        title: LocalizedStringResource("Phone Numbers", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, they replace all current numbers. An empty list removes them all.", table: "AppIntentCore")
    )
    var phoneNumbers: [String]?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add contact \(\.$name)", table: "AppIntentContact") {
            \.$contactDescription
            \.$heightCm
            \.$footSizeMm
            \.$birthday
            \.$birthdayCalendar
            \.$birthYear
            \.$hometown
            \.$howWeMet
            \.$phoneNumbers
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<ContactEntity> & ProvidesDialog {
        let contactId = try await appAction {
            try await DiaryContactAppAction.shared.add(
                name: name,
                contactDescription: contactDescription,
                heightCm: heightCm,
                footSizeMm: footSizeMm.map(Int32.init(clamping:)),
                birthdayEpochMillis: birthday?.epochMillis,
                birthdayCalendar: birthdayCalendar?.kotlin,
                birthYear: birthYear.map(Int32.init(clamping:)),
                hometown: hometown,
                howWeMet: howWeMet,
                phoneNumbers: phoneNumbers ?? [],
                tagIds: (tags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await ContactEntityQuery.require(contactId: contactId),
            dialog: IntentDialog(LocalizedStringResource("Added the contact.", table: "AppIntentContact"))
        )
    }
}

struct EditContactIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Contact", table: "AppIntentContact")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Changes the details of a contact. Values left empty stay the same, and values with Remove turned on are removed.",
            table: "AppIntentContact"
        )
    )

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

    @Parameter(
        title: LocalizedStringResource("Name", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current name.", table: "AppIntentCore")
    )
    var name: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var contactDescription: String?

    @Parameter(
        title: LocalizedStringResource("Height (cm)", table: "AppIntentCore"),
        description: LocalizedStringResource("Up to one decimal place, like 172.5.", table: "AppIntentCore")
    )
    var heightCm: Double?

    @Parameter(
        title: LocalizedStringResource("Remove Height", table: "AppIntentCore"),
        description: LocalizedStringResource("Turn on to remove the value.", table: "AppIntentCore")
    )
    var clearHeight: Bool?

    @Parameter(
        title: LocalizedStringResource("Shoe Size (mm)", table: "AppIntentCore"),
        description: LocalizedStringResource("From 0 to 999, like 270.", table: "AppIntentCore")
    )
    var footSizeMm: Int?

    @Parameter(
        title: LocalizedStringResource("Remove Shoe Size", table: "AppIntentCore"),
        description: LocalizedStringResource("Turn on to remove the value.", table: "AppIntentCore")
    )
    var clearFootSize: Bool?

    @Parameter(
        title: LocalizedStringResource("Birthday", table: "AppIntentCore"),
        description: LocalizedStringResource(
            "For a lunar birthday, choose the lunar year, month and day as is. The birth year is set to its year.",
            table: "AppIntentCore"
        ),
        kind: .date
    )
    var birthday: Date?

    @Parameter(title: LocalizedStringResource("Birthday Calendar", table: "AppIntentCore"))
    var birthdayCalendar: ContactBirthdayCalendarAppEnum?

    @Parameter(
        title: LocalizedStringResource("Remove Birthday", table: "AppIntentCore"),
        description: LocalizedStringResource("Turn on to remove the value.", table: "AppIntentCore")
    )
    var clearBirthday: Bool?

    @Parameter(
        title: LocalizedStringResource("Birth Year", table: "AppIntentCore"),
        description: LocalizedStringResource("Use when only the year is known.", table: "AppIntentCore")
    )
    var birthYear: Int?

    @Parameter(
        title: LocalizedStringResource("Remove Birth Year", table: "AppIntentCore"),
        description: LocalizedStringResource("Turn on to remove the value.", table: "AppIntentCore")
    )
    var clearBirthYear: Bool?

    @Parameter(title: LocalizedStringResource("Hometown", table: "AppIntentCore"))
    var hometown: String?

    @Parameter(title: LocalizedStringResource("How We Met", table: "AppIntentCore"))
    var howWeMet: String?

    @Parameter(
        title: LocalizedStringResource("Phone Numbers", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, they replace all current numbers. An empty list removes them all.", table: "AppIntentCore")
    )
    var phoneNumbers: [String]?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$contact)", table: "AppIntentContact") {
            \.$name
            \.$contactDescription
            \.$heightCm
            \.$clearHeight
            \.$footSizeMm
            \.$clearFootSize
            \.$birthday
            \.$birthdayCalendar
            \.$clearBirthday
            \.$birthYear
            \.$clearBirthYear
            \.$hometown
            \.$howWeMet
            \.$phoneNumbers
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await ContactEntityQuery.require(contactId: contact.id)
        let changedCount = try await appAction {
            try await DiaryContactAppAction.shared.update(
                contactId: contact.id,
                name: name,
                contactDescription: contactDescription,
                heightCm: heightCm,
                clearHeight: clearHeight,
                footSizeMm: footSizeMm.map(Int32.init(clamping:)),
                clearFootSize: clearFootSize,
                birthdayEpochMillis: birthday?.epochMillis,
                birthdayCalendar: birthdayCalendar?.kotlin,
                clearBirthday: clearBirthday,
                birthYear: birthYear.map(Int32.init(clamping:)),
                clearBirthYear: clearBirthYear,
                hometown: hometown,
                howWeMet: howWeMet,
                phoneNumbers: phoneNumbers
            )
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the contact.", table: "AppIntentContact"))
        )
    }
}
