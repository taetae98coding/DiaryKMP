import AppIntents
import DiaryIos

struct PlaceEntityQuery: EntityStringQuery {
    func entities(for identifiers: [PlaceEntity.ID]) async throws -> [PlaceEntity] {
        var entityList: [PlaceEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryPlaceAppAction.shared.get(placeId: identifier) }) {
                entityList.append(PlaceEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [PlaceEntity] {
        try await appAction {
            try await DiarySearchAppAction.shared.place(query: string, sort: nil, pageNumber: AppIntentPage.number(nil))
        }.items.map { place in PlaceEntity(place: place) }
    }

    func suggestedEntities() async throws -> [PlaceEntity] {
        try await appAction { try await DiaryPlaceAppAction.shared.list(sort: nil, pageNumber: AppIntentPage.number(nil)) }
            .items
            .map { place in PlaceEntity(place: place) }
    }

    static func require(placeId: String) async throws -> PlaceEntity {
        guard let place = try await PlaceEntityQuery().entities(for: [placeId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the place.", table: "AppIntentPlace"))
        }
        return place
    }
}
