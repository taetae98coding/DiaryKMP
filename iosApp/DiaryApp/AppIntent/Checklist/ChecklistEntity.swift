import AppIntents
import DiaryIos

struct ChecklistEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Checklist", table: "AppIntentChecklist"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Checklists", table: "AppIntentChecklist")
    )
    static let defaultQuery = ChecklistEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var checklistDescription: String

    // 체크한 수와 전체 수는 목록 결과에만, 항목은 상세 결과에만 있다.
    @Property(title: LocalizedStringResource("Checked Count", table: "AppIntentCore"))
    var checkedCount: Int?

    @Property(title: LocalizedStringResource("Total Count", table: "AppIntentCore"))
    var totalCount: Int?

    @Property(title: LocalizedStringResource("Items", table: "AppIntentCore"))
    var items: [ChecklistItemEntity]

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    init(summary: DiaryAppActionChecklistSummary) {
        id = summary.id
        title = summary.title
        checklistDescription = summary.checklistDescription
        checkedCount = Int(summary.checkedCount)
        totalCount = Int(summary.totalCount)
        items = []
        createdAt = Date(epochMillis: summary.createdAtEpochMillis)
        updatedAt = Date(epochMillis: summary.updatedAtEpochMillis)
    }

    init(detail: DiaryAppActionChecklistDetail) {
        id = detail.checklist.id
        title = detail.checklist.title
        checklistDescription = detail.checklist.checklistDescription
        checkedCount = nil
        totalCount = nil
        items = detail.items.map { item in ChecklistItemEntity(checklistId: detail.checklist.id, item: item) }
        createdAt = Date(epochMillis: detail.checklist.createdAtEpochMillis)
        updatedAt = Date(epochMillis: detail.checklist.updatedAtEpochMillis)
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: subtitle.map { "\($0)" },
            image: AppIntentItemImage.symbol("checklist", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }

    private var subtitle: String? {
        if let checkedCount, let totalCount {
            return "\(checkedCount)/\(totalCount)"
        }
        return checklistDescription.appIntentNonEmpty
    }
}

struct ChecklistItemEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Checklist Item", table: "AppIntentChecklist"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Checklist Items", table: "AppIntentChecklist")
    )
    static let defaultQuery = ChecklistItemEntityQuery()

    // 항목 하나를 찾는 동작이 없어 항목은 체크리스트 상세에서만 찾을 수 있으므로, 식별자에 체크리스트 id를 함께 담는다.
    let id: String

    @Property(title: LocalizedStringResource("Content", table: "AppIntentCore"))
    var content: String

    @Property(title: LocalizedStringResource("Checked", table: "AppIntentCore"))
    var isChecked: Bool

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    init(checklistId: String, item: DiaryAppActionChecklistItem) {
        id = ChecklistItemEntityID(checklistId: checklistId, itemId: item.id).rawValue
        content = item.content
        isChecked = item.isChecked
        createdAt = Date(epochMillis: item.createdAtEpochMillis)
        updatedAt = Date(epochMillis: item.updatedAtEpochMillis)
    }

    nonisolated var itemId: String {
        ChecklistItemEntityID(rawValue: id)?.itemId ?? id
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(content)",
            image: AppIntentItemImage.symbol(isChecked ? "checkmark.square" : "square", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}

nonisolated struct ChecklistItemEntityID {
    let checklistId: String
    let itemId: String

    init(checklistId: String, itemId: String) {
        self.checklistId = checklistId
        self.itemId = itemId
    }

    init?(rawValue: String) {
        let parts = rawValue.split(separator: "/", maxSplits: 1).map(String.init)
        guard parts.count == 2 else { return nil }
        checklistId = parts[0]
        itemId = parts[1]
    }

    var rawValue: String {
        "\(checklistId)/\(itemId)"
    }
}
