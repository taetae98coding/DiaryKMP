import AppIntents
import DiaryIos

struct LinkChildTagToTagIntent: AppIntent {
    static let title = LocalizedStringResource("Link Child Tag to Tag", table: "AppIntentTag")
    static let description = IntentDescription(
        LocalizedStringResource("Links another tag to a tag as its child. Tags of memos, webs and places don't change.", table: "AppIntentTag")
    )

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(title: LocalizedStringResource("Child Tag", table: "AppIntentCore"))
    var childTag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$childTag) to \(\.$tag)", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await TagEntityQuery.require(tagId: childTag.id)
        _ = try await appAction { try await DiaryTagLinkAppAction.shared.add(tagId: tag.id, linkedTagId: childTag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkChildTagFromTagIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Child Tag from Tag", table: "AppIntentTag")
    static let description = IntentDescription(
        LocalizedStringResource("Unlinks a child tag from a tag. A link in the opposite direction stays.", table: "AppIntentTag")
    )

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(title: LocalizedStringResource("Child Tag", table: "AppIntentCore"))
    var childTag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$childTag) from \(\.$tag)", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await TagEntityQuery.require(tagId: childTag.id)
        _ = try await appAction { try await DiaryTagLinkAppAction.shared.remove(tagId: tag.id, linkedTagId: childTag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
