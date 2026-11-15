import AppIntents
import DiaryIos

struct DeletePlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Place", table: "AppIntentPlace")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a place. Links to memos stay. Run Restore Deleted Place with the same place to undo.", table: "AppIntentPlace")
    )

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$place)", table: "AppIntentPlace")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        let changedCount = try await appAction { try await DiaryPlaceAppAction.shared.delete(placeId: place.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the place.", table: "AppIntentPlace"))
        )
    }
}

struct RestorePlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Place", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted place.", table: "AppIntentPlace"))

    @Parameter(title: LocalizedStringResource("Place to Restore", table: "AppIntentPlace"))
    var place: PlaceEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$place)", table: "AppIntentPlace")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryPlaceAppAction.shared.restore(placeId: place.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the place.", table: "AppIntentPlace"))
        )
    }
}

struct LinkTagToPlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to Place", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a place.", table: "AppIntentPlace"))

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentPlace"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$place)", table: "AppIntentPlace")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.add(kind: .PLACE, entityId: place.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromPlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from Place", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a place.", table: "AppIntentPlace"))

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentPlace"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$place)", table: "AppIntentPlace")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.remove(kind: .PLACE, entityId: place.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
