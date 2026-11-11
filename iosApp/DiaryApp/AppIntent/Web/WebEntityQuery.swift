import AppIntents
import DiaryIos

struct WebEntityQuery: EntityStringQuery {
    // ‘삭제한 웹 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [WebEntity.ID]) async throws -> [WebEntity] {
        var entityList: [WebEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryWebAppAction.shared.getIncludingDeleted(webId: identifier) }) {
                entityList.append(WebEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [WebEntity] {
        try await appAction {
            try await DiarySearchAppAction.shared.web(query: string, sort: nil, pageNumber: AppIntentPage.number(nil))
        }.items.map { web in WebEntity(web: web) }
    }

    func suggestedEntities() async throws -> [WebEntity] {
        try await appAction { try await DiaryWebAppAction.shared.list(sort: nil, pageNumber: AppIntentPage.number(nil)) }
            .items
            .map { web in WebEntity(web: web) }
    }

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(webId: String) async throws -> WebEntity {
        guard let detail = try await appAction({ try await DiaryWebAppAction.shared.get(webId: webId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the web.", table: "AppIntentWeb"))
        }
        return WebEntity(detail: detail)
    }
}
