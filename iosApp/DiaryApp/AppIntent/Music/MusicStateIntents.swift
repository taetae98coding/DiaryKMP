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
