import AppIntents
import DiaryIos

struct DDayEntityQuery: EntityStringQuery {
    // ‘삭제한 디데이 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [DDayEntity.ID]) async throws -> [DDayEntity] {
        var entityList: [DDayEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryDDayAppAction.shared.getIncludingDeleted(ddayId: identifier) }) {
                entityList.append(DDayEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [DDayEntity] {
        try await appAction {
            try await DiarySearchAppAction.shared.dday(query: string, sort: nil, pageNumber: AppIntentPage.number(nil))
        }.items.map(DDayEntity.init(dday:))
    }

    func suggestedEntities() async throws -> [DDayEntity] {
        try await appAction { try await DiaryDDayAppAction.shared.list(pageNumber: AppIntentPage.number(nil)) }
            .items
            .map(DDayEntity.init(dday:))
    }

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(ddayId: String) async throws -> DDayEntity {
        guard let detail = try await appAction({ try await DiaryDDayAppAction.shared.get(ddayId: ddayId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the D-Day.", table: "AppIntentDDay"))
        }
        return DDayEntity(detail: detail)
    }
}
