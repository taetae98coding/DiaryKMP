import AppIntents
import DiaryIos

struct ContactEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Contact", table: "AppIntentContact"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Contacts", table: "AppIntentContact")
    )
    static let defaultQuery = ContactEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Name", table: "AppIntentCore"))
    var name: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var contactDescription: String

    @Property(title: LocalizedStringResource("Favorite", table: "AppIntentCore"))
    var isFavorite: Bool

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 이름, 설명, 즐겨찾기만 있으므로, 나머지 정보는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Height (cm)", table: "AppIntentCore"))
    var heightCm: Double?

    @Property(title: LocalizedStringResource("Shoe Size (mm)", table: "AppIntentCore"))
    var footSizeMm: Int?

    @Property(title: LocalizedStringResource("Birthday", table: "AppIntentCore"))
    var birthday: Date?

    @Property(title: LocalizedStringResource("Birthday Calendar", table: "AppIntentCore"))
    var birthdayCalendar: ContactBirthdayCalendarAppEnum?

    @Property(title: LocalizedStringResource("Birth Year", table: "AppIntentCore"))
    var birthYear: Int?

    @Property(title: LocalizedStringResource("Hometown", table: "AppIntentCore"))
    var hometown: String

    @Property(title: LocalizedStringResource("How We Met", table: "AppIntentCore"))
    var howWeMet: String

    @Property(title: LocalizedStringResource("Phone Numbers", table: "AppIntentCore"))
    var phoneNumbers: [String]

    private let nameQualifier: String

    init(contact: DiaryAppActionContact) {
        id = contact.id
        nameQualifier = contact.nameQualifier
        name = contact.name
        contactDescription = contact.contactDescription
        isFavorite = contact.isFavorite
        createdAt = Date(epochMillis: contact.createdAtEpochMillis)
        updatedAt = Date(epochMillis: contact.updatedAtEpochMillis)
        heightCm = nil
        footSizeMm = nil
        birthday = nil
        birthdayCalendar = nil
        birthYear = nil
        hometown = ""
        howWeMet = ""
        phoneNumbers = []
    }

    // 상세 결과에는 같은 이름을 구별하는 글이 없으므로, 이름만 보여 준다.
    init(detail: DiaryAppActionContactDetail) {
        id = detail.id
        nameQualifier = ""
        name = detail.name
        contactDescription = detail.contactDescription
        isFavorite = detail.isFavorite
        createdAt = Date(epochMillis: detail.createdAtEpochMillis)
        updatedAt = Date(epochMillis: detail.updatedAtEpochMillis)
        heightCm = detail.heightCm
        footSizeMm = detail.footSizeMm.map(Int.init)
        birthday = detail.birthdayEpochMillis.map(Date.init(epochMillis:))
        birthdayCalendar = detail.birthdayCalendar.map(ContactBirthdayCalendarAppEnum.init(kotlin:))
        birthYear = detail.birthYear.map(Int.init)
        hometown = detail.hometown
        howWeMet = detail.howWeMet
        phoneNumbers = detail.phoneNumbers
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: nameQualifier.isEmpty ? "\(name)" : LocalizedStringResource("\(name) (\(nameQualifier))", table: "AppIntentContact"),
            subtitle: contactDescription.appIntentNonEmpty.map { "\($0)" },
            image: AppIntentItemImage.symbol("person.crop.circle", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}
