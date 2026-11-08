import AppIntents
import DiaryIos

struct WebEntityQuery: EntityStringQuery {
    func entities(for identifiers: [WebEntity.ID]) async throws -> [WebEntity] {
        var entityList: [WebEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryWebAppAction.shared.get(webId: identifier) }) {
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

    static func require(webId: String) async throws -> WebEntity {
        guard let web = try await WebEntityQuery().entities(for: [webId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the web.", table: "AppIntentWeb"))
        }
        return web
    }
}
