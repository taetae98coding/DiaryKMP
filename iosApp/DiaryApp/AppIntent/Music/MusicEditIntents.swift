import AppIntents
import DiaryIos

struct AddMusicIntent: AppIntent {
    static let title = LocalizedStringResource("Add Song", table: "AppIntentMusic")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Adds a song to the playlist. Song information isn't loaded from the link, so enter the title and artist yourself.",
            table: "AppIntentMusic"
        )
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the song title?", table: "AppIntentMusic"))
    )
    var title: String

    @Parameter(title: LocalizedStringResource("Artist", table: "AppIntentCore"))
    var artist: String?

    @Parameter(
        title: LocalizedStringResource("YouTube Link", table: "AppIntentCore"),
        description: LocalizedStringResource("A YouTube video link. Empty text means no link.", table: "AppIntentCore")
    )
    var link: String?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add song \(\.$title)", table: "AppIntentMusic") {
            \.$artist
            \.$link
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<MusicEntity> & ProvidesDialog {
        let musicId = try await appAction {
            try await DiaryMusicAppAction.shared.add(title: title, artist: artist, link: link, tagIds: (tags ?? []).map(\.id))
        }
        return .result(
            value: try await MusicEntityQuery.require(musicId: musicId),
            dialog: IntentDialog(LocalizedStringResource("Added the song.", table: "AppIntentMusic"))
        )
    }
}

struct EditMusicIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Song", table: "AppIntentMusic")
    static let description = IntentDescription(
        LocalizedStringResource("Changes the title, artist or link of a song. Values left empty stay the same.", table: "AppIntentMusic")
    )

    @Parameter(title: LocalizedStringResource("Song", table: "AppIntentMusic"))
    var song: MusicEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Artist", table: "AppIntentCore"))
    var artist: String?

    @Parameter(
        title: LocalizedStringResource("YouTube Link", table: "AppIntentCore"),
        description: LocalizedStringResource("A YouTube video link. Empty text means no link.", table: "AppIntentCore")
    )
    var link: String?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$song)", table: "AppIntentMusic") {
            \.$title
            \.$artist
            \.$link
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await MusicEntityQuery.require(musicId: song.id)
        let changedCount = try await appAction {
            try await DiaryMusicAppAction.shared.update(musicId: song.id, title: title, artist: artist, link: link)
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the song.", table: "AppIntentMusic"))
        )
    }
}
