import AppIntents
import DiaryIos

struct TagEntityQuery: EntityStringQuery {
    func entities(for identifiers: [TagEntity.ID]) async throws -> [TagEntity] {
        var entityList: [TagEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryTagAppAction.shared.get(tagId: identifier) }) {
                entityList.append(TagEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [TagEntity] {
        try await appAction {
            try await DiarySearchAppAction.shared.tag(query: string, sort: nil, pageNumber: AppIntentPage.number(nil))
        }.items.map(TagEntity.init(tag:))
    }

    func suggestedEntities() async throws -> [TagEntity] {
        let firstPage = AppIntentPage.number(nil)
        let inProgress = try await appAction { try await DiaryTagAppAction.shared.list(sort: nil, pageNumber: firstPage) }
        let finished = try await appAction { try await DiaryTagAppAction.shared.finishedList(sort: nil, pageNumber: firstPage) }
        return (inProgress.items + finished.items).map(TagEntity.init(tag:))
    }

    static func require(tagId: String) async throws -> TagEntity {
        guard let tag = try await TagEntityQuery().entities(for: [tagId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the tag.", table: "AppIntentTag"))
        }
        return tag
    }
}
