import AppIntents
import DiaryIos

struct DDayEntityQuery: EntityStringQuery {
    func entities(for identifiers: [DDayEntity.ID]) async throws -> [DDayEntity] {
        var entityList: [DDayEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryDDayAppAction.shared.get(ddayId: identifier) }) {
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

    static func require(ddayId: String) async throws -> DDayEntity {
        guard let dday = try await DDayEntityQuery().entities(for: [ddayId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the D-Day.", table: "AppIntentDDay"))
        }
        return dday
    }
}
