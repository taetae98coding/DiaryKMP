import AppIntents
import DiaryIos

struct AddPlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Add Place", table: "AppIntentPlace")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new place. The address and coordinates aren't looked up, so enter values you know.", table: "AppIntentPlace")
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String

    @Parameter(
        title: LocalizedStringResource("Latitude", table: "AppIntentCore"),
        description: LocalizedStringResource("A decimal from -90 to 90, like 37.566500.", table: "AppIntentCore")
    )
    var latitude: Double

    @Parameter(
        title: LocalizedStringResource("Longitude", table: "AppIntentCore"),
        description: LocalizedStringResource("A decimal from -180 to 180, like 126.978000.", table: "AppIntentCore")
    )
    var longitude: Double

    @Parameter(title: LocalizedStringResource("Address", table: "AppIntentCore"))
    var address: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var placeDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add place \(\.$title) \(\.$latitude) \(\.$longitude)", table: "AppIntentPlace") {
            \.$address
            \.$placeDescription
            \.$color
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<PlaceEntity> & ProvidesDialog {
        let placeId = try await appAction {
            try await DiaryPlaceAppAction.shared.add(
                title: title,
                latitude: latitude,
                longitude: longitude,
                address: address,
                placeDescription: placeDescription,
                color: color,
                tagIds: (tags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await PlaceEntityQuery.require(placeId: placeId),
            dialog: IntentDialog(LocalizedStringResource("Added the place.", table: "AppIntentPlace"))
        )
    }
}

struct EditPlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Place", table: "AppIntentPlace")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Changes the title, description, color, address or coordinates of a place. Values left empty stay the same.",
            table: "AppIntentPlace"
        )
    )

    @Parameter(title: LocalizedStringResource("Place", table: "AppIntentPlace"))
    var place: PlaceEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var placeDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(title: LocalizedStringResource("Address", table: "AppIntentCore"))
    var address: String?

    @Parameter(
        title: LocalizedStringResource("Latitude", table: "AppIntentCore"),
        description: LocalizedStringResource("A decimal from -90 to 90, like 37.566500.", table: "AppIntentCore")
    )
    var latitude: Double?

    @Parameter(
        title: LocalizedStringResource("Longitude", table: "AppIntentCore"),
        description: LocalizedStringResource("A decimal from -180 to 180, like 126.978000.", table: "AppIntentCore")
    )
    var longitude: Double?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$place)", table: "AppIntentPlace") {
            \.$title
            \.$placeDescription
            \.$color
            \.$address
            \.$latitude
            \.$longitude
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        let changedCount = try await appAction {
            try await DiaryPlaceAppAction.shared.update(
                placeId: place.id,
                title: title,
                placeDescription: placeDescription,
                color: color,
                address: address,
                latitude: latitude,
                longitude: longitude
            )
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the place.", table: "AppIntentPlace"))
        )
    }
}
