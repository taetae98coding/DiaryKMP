import AppIntents
import DiaryIos

struct AddTagIntent: AppIntent {
    static let title = LocalizedStringResource("Add Tag", table: "AppIntentTag")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new tag. A random color is used if you don't choose one.", table: "AppIntentTag")
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the tag title?", table: "AppIntentTag"))
    )
    var title: String

    @Parameter(
        title: LocalizedStringResource("Emoji", table: "AppIntentCore"),
        description: LocalizedStringResource("One emoji shown before the title. To remove it when editing, enter empty text.", table: "AppIntentCore")
    )
    var emoji: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var tagDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var linkedTags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add tag \(\.$title)", table: "AppIntentTag") {
            \.$emoji
            \.$tagDescription
            \.$color
            \.$linkedTags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<TagEntity> & ProvidesDialog {
        let tagId = try await appAction {
            try await DiaryTagAppAction.shared.add(
                title: title,
                emoji: emoji,
                tagDescription: tagDescription,
                color: color,
                linkedTagIds: (linkedTags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await TagEntityQuery.require(tagId: tagId),
            dialog: IntentDialog(LocalizedStringResource("Added the tag.", table: "AppIntentTag"))
        )
    }
}

struct EditTagIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Tag", table: "AppIntentTag")
    static let description = IntentDescription(
        LocalizedStringResource("Changes the emoji, title, description or color of a tag. Values left empty stay the same.", table: "AppIntentTag")
    )

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Emoji", table: "AppIntentCore"),
        description: LocalizedStringResource("One emoji shown before the title. To remove it when editing, enter empty text.", table: "AppIntentCore")
    )
    var emoji: String?

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var tagDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$tag)", table: "AppIntentTag") {
            \.$emoji
            \.$title
            \.$tagDescription
            \.$color
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let changedCount = try await appAction {
            try await DiaryTagAppAction.shared.update(tagId: tag.id, emoji: emoji, title: title, tagDescription: tagDescription, color: color)
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the tag.", table: "AppIntentTag"))
        )
    }
}
