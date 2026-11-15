import AppIntents
import DiaryIos

struct DeleteMusicIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Song", table: "AppIntentMusic")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a song from the playlist. Run Restore Deleted Song with the same song to undo.", table: "AppIntentMusic")
    )

    @Parameter(title: LocalizedStringResource("Song", table: "AppIntentMusic"))
    var song: MusicEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$song)", table: "AppIntentMusic")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await MusicEntityQuery.require(musicId: song.id)
        let changedCount = try await appAction { try await DiaryMusicAppAction.shared.delete(musicId: song.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the song.", table: "AppIntentMusic"))
        )
    }
}

struct RestoreMusicIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Song", table: "AppIntentMusic")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted song.", table: "AppIntentMusic"))

    @Parameter(title: LocalizedStringResource("Song to Restore", table: "AppIntentMusic"))
    var song: MusicEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$song)", table: "AppIntentMusic")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryMusicAppAction.shared.restore(musicId: song.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the song.", table: "AppIntentMusic"))
        )
    }
}

struct LinkTagToMusicIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to Song", table: "AppIntentMusic")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a song.", table: "AppIntentMusic"))

    @Parameter(title: LocalizedStringResource("Song", table: "AppIntentMusic"))
    var song: MusicEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentMusic"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$song)", table: "AppIntentMusic")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await MusicEntityQuery.require(musicId: song.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.add(kind: .MUSIC, entityId: song.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromMusicIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from Song", table: "AppIntentMusic")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a song.", table: "AppIntentMusic"))

    @Parameter(title: LocalizedStringResource("Song", table: "AppIntentMusic"))
    var song: MusicEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentMusic"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$song)", table: "AppIntentMusic")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await MusicEntityQuery.require(musicId: song.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.remove(kind: .MUSIC, entityId: song.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
