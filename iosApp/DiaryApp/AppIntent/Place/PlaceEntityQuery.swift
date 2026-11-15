import AppIntents
import DiaryIos

struct PlaceEntityQuery: EntityStringQuery {
    // ‘삭제한 장소 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [PlaceEntity.ID]) async throws -> [PlaceEntity] {
        var entityList: [PlaceEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryPlaceAppAction.shared.getIncludingDeleted(placeId: identifier) }) {
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

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(placeId: String) async throws -> PlaceEntity {
        guard let detail = try await appAction({ try await DiaryPlaceAppAction.shared.get(placeId: placeId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the place.", table: "AppIntentPlace"))
        }
        return PlaceEntity(detail: detail)
    }
}
