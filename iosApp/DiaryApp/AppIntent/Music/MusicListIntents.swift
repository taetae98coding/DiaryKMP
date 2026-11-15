import AppIntents
import DiaryIos

struct MusicListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Playlist", table: "AppIntentMusic")
    static let description = IntentDescription(LocalizedStringResource("Gets the songs in the playlist.", table: "AppIntentMusic"))

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: MusicSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get Playlist", table: "AppIntentMusic") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MusicEntity]> & ProvidesDialog {
        let musicPage = try await appAction {
            try await DiaryMusicAppAction.shared.list(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: musicPage.items.map(MusicEntity.init(music:)), dialog: musicPage.dialog)
    }
}

struct MusicDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Song Details", table: "AppIntentMusic")
    static let description = IntentDescription(LocalizedStringResource("Gets a song in the playlist with its linked tags.", table: "AppIntentMusic"))

    @Parameter(title: LocalizedStringResource("Song", table: "AppIntentMusic"))
    var song: MusicEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$song) Details", table: "AppIntentMusic")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<MusicEntity> {
        .result(value: try await MusicEntityQuery.require(musicId: song.id))
    }
}

private extension DiaryAppActionMusicPage {
    nonisolated var dialog: IntentDialog {
        AppIntentResultDialog.list(
            count: items.count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) songs.", table: "AppIntentMusic") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) songs. There's more on page \(nextPage).", table: "AppIntentMusic")
            }
        )
    }
}
