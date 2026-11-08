import AppIntents
import DiaryIos

struct MusicEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Song", table: "AppIntentMusic"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Songs", table: "AppIntentMusic")
    )
    static let defaultQuery = MusicEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Artist", table: "AppIntentCore"))
    var artist: String

    @Property(title: LocalizedStringResource("YouTube Link", table: "AppIntentCore"))
    var link: String

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    init(music: DiaryAppActionMusic) {
        id = music.id
        title = music.title
        artist = music.artist
        link = music.link
        createdAt = Date(epochMillis: music.createdAtEpochMillis)
        updatedAt = Date(epochMillis: music.updatedAtEpochMillis)
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: artist.appIntentNonEmpty.map { "\($0)" },
            image: AppIntentItemImage.symbol("music.note", hexColor: AppIntentItemImage.primaryHexColor)
        )
    }
}

struct MusicEntityQuery: EntityStringQuery {
    func entities(for identifiers: [MusicEntity.ID]) async throws -> [MusicEntity] {
        var entityList: [MusicEntity] = []
        for identifier in identifiers {
            if let music = try await appAction({ try await DiaryMusicAppAction.shared.get(musicId: identifier) }) {
                entityList.append(MusicEntity(music: music))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [MusicEntity] {
        try await appAction { try await DiaryMusicAppAction.shared.search(query: string) }.map(MusicEntity.init(music:))
    }

    func suggestedEntities() async throws -> [MusicEntity] {
        try await appAction { try await DiaryMusicAppAction.shared.list(sort: nil, pageNumber: AppIntentPage.number(nil)) }.items.map(MusicEntity.init(music:))
    }

    static func require(musicId: String) async throws -> MusicEntity {
        guard let music = try await MusicEntityQuery().entities(for: [musicId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the song.", table: "AppIntentMusic"))
        }
        return music
    }
}
