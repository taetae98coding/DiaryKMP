import AppIntents
import DiaryIos

struct TagEntityQuery: EntityStringQuery {
    // ‘삭제한 태그 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [TagEntity.ID]) async throws -> [TagEntity] {
        var entityList: [TagEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryTagAppAction.shared.getIncludingDeleted(tagId: identifier) }) {
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

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(tagId: String) async throws -> TagEntity {
        guard let detail = try await appAction({ try await DiaryTagAppAction.shared.get(tagId: tagId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the tag.", table: "AppIntentTag"))
        }
        return TagEntity(detail: detail)
    }
}
