import AppIntents
import DiaryIos

struct PlaceEntity: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(
        name: LocalizedStringResource("Place", table: "AppIntentPlace"),
        numericFormat: LocalizedStringResource("\(placeholder: .int) Places", table: "AppIntentPlace")
    )
    static let defaultQuery = PlaceEntityQuery()

    let id: String

    @Property(title: LocalizedStringResource("Title", table: "AppIntentCore"))
    var title: String

    @Property(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var placeDescription: String

    @Property(title: LocalizedStringResource("Color", table: "AppIntentCore"))
    var color: String

    @Property(title: LocalizedStringResource("Address", table: "AppIntentCore"))
    var address: String

    @Property(title: LocalizedStringResource("Latitude", table: "AppIntentCore"))
    var latitude: Double?

    @Property(title: LocalizedStringResource("Longitude", table: "AppIntentCore"))
    var longitude: Double?

    @Property(title: LocalizedStringResource("Created", table: "AppIntentCore"))
    var createdAt: Date

    @Property(title: LocalizedStringResource("Updated", table: "AppIntentCore"))
    var updatedAt: Date

    // 목록 결과에는 데스크톱 연결과 같이 연결된 태그가 없으므로, 태그는 상세 결과에서만 채워진다.
    @Property(title: LocalizedStringResource("Tags", table: "AppIntentCore"))
    var tags: [TagEntity]

    init(place: DiaryAppActionPlace) {
        self.init(place: place, tags: [])
    }

    init(detail: DiaryAppActionPlaceDetail) {
        self.init(place: detail.place, tags: detail.tags)
    }

    private init(place: DiaryAppActionPlace, tags: [DiaryAppActionTag]) {
        id = place.id
        title = place.title
        placeDescription = place.placeDescription
        color = place.color
        address = place.address
        latitude = place.latitude
        longitude = place.longitude
        createdAt = Date(epochMillis: place.createdAtEpochMillis)
        updatedAt = Date(epochMillis: place.updatedAtEpochMillis)
        self.tags = tags.map(TagEntity.init(tag:))
    }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: "\(title)",
            subtitle: (address.appIntentNonEmpty ?? placeDescription.appIntentNonEmpty).map { "\($0)" },
            image: AppIntentItemImage.symbol("mappin.and.ellipse", hexColor: color)
        )
    }
}
