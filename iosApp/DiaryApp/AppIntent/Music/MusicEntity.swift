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

    // 목록 결과에는 데스크톱 연결과 같이 연결된 태그가 없으므로, 태그는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    init(music: DiaryAppActionMusic) {
        self.init(music: music, tags: [])
    }

    init(detail: DiaryAppActionMusicDetail) {
        self.init(music: detail.music, tags: detail.tags)
    }

    private init(music: DiaryAppActionMusic, tags: [DiaryAppActionTag]) {
        id = music.id
        title = music.title
        artist = music.artist
        link = music.link
        createdAt = Date(epochMillis: music.createdAtEpochMillis)
        updatedAt = Date(epochMillis: music.updatedAtEpochMillis)
        self.tags = tags.map(TagEntity.init(tag:))
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
    // ‘삭제한 곡 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [MusicEntity.ID]) async throws -> [MusicEntity] {
        var entityList: [MusicEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryMusicAppAction.shared.getIncludingDeleted(musicId: identifier) }) {
                entityList.append(MusicEntity(detail: detail))
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

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(musicId: String) async throws -> MusicEntity {
        guard let detail = try await appAction({ try await DiaryMusicAppAction.shared.get(musicId: musicId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the song.", table: "AppIntentMusic"))
        }
        return MusicEntity(detail: detail)
    }
}
