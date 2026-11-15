import AppIntents
import DiaryIos

struct PlaceListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Places", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Gets saved places in the order of the place list.", table: "AppIntentPlace"))

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: PlaceSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get Places", table: "AppIntentPlace") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[PlaceEntity]> & ProvidesDialog {
        let placePage = try await appAction {
            try await DiaryPlaceAppAction.shared.list(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: placePage.items.map { place in PlaceEntity(place: place) },
            dialog: PlaceListDialog.make(count: placePage.items.count, nextPageNumber: placePage.nextPageNumber)
        )
    }
}

struct PlaceListInAreaIntent: AppIntent {
    static let title = LocalizedStringResource("Get Places in Area", table: "AppIntentPlace")
    static let description = IntentDescription(
        LocalizedStringResource("Gets all places inside an area set by latitude and longitude at once.", table: "AppIntentPlace")
    )

    @Parameter(
        title: LocalizedStringResource("South Latitude", table: "AppIntentCore"),
        description: LocalizedStringResource("Latitude of the southern or northern edge of the area.", table: "AppIntentCore")
    )
    var south: Double

    @Parameter(
        title: LocalizedStringResource("North Latitude", table: "AppIntentCore"),
        description: LocalizedStringResource("Latitude of the southern or northern edge of the area.", table: "AppIntentCore")
    )
    var north: Double

    @Parameter(
        title: LocalizedStringResource("West Longitude", table: "AppIntentCore"),
        description: LocalizedStringResource(
            "Longitude of the western or eastern edge. West may be greater than east when the area crosses the 180th meridian.",
            table: "AppIntentCore"
        )
    )
    var west: Double

    @Parameter(
        title: LocalizedStringResource("East Longitude", table: "AppIntentCore"),
        description: LocalizedStringResource(
            "Longitude of the western or eastern edge. West may be greater than east when the area crosses the 180th meridian.",
            table: "AppIntentCore"
        )
    )
    var east: Double

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: PlaceSortAppEnum?

    static var parameterSummary: some ParameterSummary {
        Summary("Get places in area \(\.$south) \(\.$north) \(\.$west) \(\.$east)", table: "AppIntentPlace") {
            \.$sort
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[PlaceEntity]> & ProvidesDialog {
        let placeList = try await appAction {
            try await DiaryPlaceAppAction.shared.listInArea(south: south, north: north, west: west, east: east, sort: sort?.kotlin)
        }
        return .result(
            value: placeList.map { place in PlaceEntity(place: place) },
            dialog: PlaceListDialog.make(count: placeList.count, nextPageNumber: nil)
        )
    }
}

struct PlaceDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Place Details", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Gets a place with its linked tags.", table: "AppIntentPlace"))

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$place) Details", table: "AppIntentPlace")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<PlaceEntity> {
        .result(value: try await PlaceEntityQuery.require(placeId: place.id))
    }
}

struct PlaceMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Place's In-Progress Memos", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Gets memos linked to a place that aren't finished.", table: "AppIntentPlace"))

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: MemoSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$place)'s In-Progress Memos", table: "AppIntentPlace") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        let memoPage = try await appAction {
            try await DiaryMemoListAppAction.shared.placeList(placeId: place.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: LinkedMemoListDialog.make(page: memoPage))
    }
}

struct PlaceFinishedMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Place's Finished Memos", table: "AppIntentPlace")
    static let description = IntentDescription(LocalizedStringResource("Gets finished memos linked to a place.", table: "AppIntentPlace"))

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: MemoSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$place)'s Finished Memos", table: "AppIntentPlace") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        let memoPage = try await appAction {
            try await DiaryMemoListAppAction.shared.placeFinishedList(placeId: place.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: LinkedMemoListDialog.make(page: memoPage))
    }
}

nonisolated enum PlaceListDialog {
    static func make(count: Int, nextPageNumber: Int32?) -> IntentDialog {
        AppIntentResultDialog.list(
            count: count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) places.", table: "AppIntentPlace") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) places. There's more on page \(nextPage).", table: "AppIntentPlace")
            }
        )
    }
}

// 장소·웹에 연결된 메모 목록은 메모 목록과 같은 문구로 알린다.
nonisolated enum LinkedMemoListDialog {
    static func make(page: DiaryAppActionMemoPage) -> IntentDialog {
        AppIntentResultDialog.list(
            count: page.items.count,
            nextPageNumber: page.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) memos.", table: "AppIntentMemo") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) memos. There's more on page \(nextPage).", table: "AppIntentMemo")
            }
        )
    }
}
