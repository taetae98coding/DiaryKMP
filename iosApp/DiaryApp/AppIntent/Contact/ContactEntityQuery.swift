import AppIntents
import DiaryIos

struct ContactEntityQuery: EntityStringQuery {
    // ‘삭제한 연락처 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [ContactEntity.ID]) async throws -> [ContactEntity] {
        var entityList: [ContactEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryContactAppAction.shared.getIncludingDeleted(contactId: identifier) }) {
                entityList.append(ContactEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [ContactEntity] {
        try await appAction { try await DiaryContactAppAction.shared.search(query: string) }.map(ContactEntity.init(contact:))
    }

    func suggestedEntities() async throws -> [ContactEntity] {
        try await appAction { try await DiaryContactAppAction.shared.list(sort: nil, pageNumber: AppIntentPage.number(nil)) }
            .items
            .map(ContactEntity.init(contact:))
    }

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(contactId: String) async throws -> ContactEntity {
        guard let detail = try await appAction({ try await DiaryContactAppAction.shared.get(contactId: contactId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the contact.", table: "AppIntentContact"))
        }
        return ContactEntity(detail: detail)
    }
}
