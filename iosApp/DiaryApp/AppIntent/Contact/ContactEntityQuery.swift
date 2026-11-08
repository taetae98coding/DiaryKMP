import AppIntents
import DiaryIos

struct ContactEntityQuery: EntityStringQuery {
    func entities(for identifiers: [ContactEntity.ID]) async throws -> [ContactEntity] {
        var entityList: [ContactEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryContactAppAction.shared.get(contactId: identifier) }) {
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

    static func require(contactId: String) async throws -> ContactEntity {
        guard let contact = try await ContactEntityQuery().entities(for: [contactId]).first else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the contact.", table: "AppIntentContact"))
        }
        return contact
    }
}
