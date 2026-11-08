import AppIntents
import DiaryIos

struct TagEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Tag", table: "AppIntentTag"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Tags", table: "AppIntentTag")
    )
    static let defaultQuery = TagEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Emoji", table: "AppIntentCore"))
    var emoji: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var tagDescription: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("Finished", table: "AppIntentCore"))
    var isFinished: Bool

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 하위 태그가 없으므로, 하위 태그는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Child Tags", table: "AppIntentCore"))
    var childTags: [TagEntity]

    init(tag: DiaryAppActionTag) {
        id = tag.id
        emoji = tag.emoji
        title = tag.title
        tagDescription = tag.tagDescription
        color = tag.color
        isFinished = tag.isFinished
        createdAt = Date(epochMillis: tag.createdAtEpochMillis)
        updatedAt = Date(epochMillis: tag.updatedAtEpochMillis)
        childTags = []
    }

    init(detail: DiaryAppActionTagDetail) {
        self.init(tag: detail.tag)
        childTags = detail.linkedTags.map(TagEntity.init(tag:))
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: emoji.isEmpty ? "\(title)" : "\(emoji) \(title)",
            subtitle: tagDescription.appIntentNonEmpty.map { "\($0)" },
            image: AppIntentItemImage.symbol("tag", hexColor: color)
        )
    }
}
