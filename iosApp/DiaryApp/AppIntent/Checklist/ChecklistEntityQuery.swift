import AppIntents
import DiaryIos

struct ChecklistEntityQuery: EntityStringQuery {
    // ‘삭제한 체크리스트 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [ChecklistEntity.ID]) async throws -> [ChecklistEntity] {
        var entityList: [ChecklistEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryChecklistAppAction.shared.getIncludingDeleted(checklistId: identifier, itemSort: nil) }) {
                entityList.append(ChecklistEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [ChecklistEntity] {
        try await appAction { try await DiaryChecklistSearchAppAction.shared.search(query: string) }.map(ChecklistEntity.init(summary:))
    }

    func suggestedEntities() async throws -> [ChecklistEntity] {
        try await ChecklistEntityQuery.firstPage().map(ChecklistEntity.init(summary:))
    }

    nonisolated static func firstPage() async throws -> [DiaryAppActionChecklistSummary] {
        try await appAction { try await DiaryChecklistAppAction.shared.list(pageNumber: AppIntentPage.number(nil)) }.items
    }

    // 상세와 추가 결과, 항목 찾기는 데스크톱 연결과 같이 삭제된 체크리스트를 없는 체크리스트로 다룬다.
    nonisolated static func detail(checklistId: String, itemSort: DiaryAppActionChecklistItemSort?) async throws -> DiaryAppActionChecklistDetail? {
        try await appAction { try await DiaryChecklistAppAction.shared.get(checklistId: checklistId, itemSort: itemSort) }
    }

    static func require(checklistId: String, itemSort: DiaryAppActionChecklistItemSort? = nil) async throws -> ChecklistEntity {
        guard let detail = try await detail(checklistId: checklistId, itemSort: itemSort) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the checklist.", table: "AppIntentChecklist"))
        }
        return ChecklistEntity(detail: detail)
    }

    static func requireChecklist(of item: ChecklistItemEntity) async throws {
        guard let checklistId = item.checklistId else { return }
        guard let detail = try await detail(checklistId: checklistId, itemSort: nil) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the checklist.", table: "AppIntentChecklist"))
        }
        guard detail.items.contains(where: { saved in saved.id == item.itemId }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the checklist item.", table: "AppIntentChecklist"))
        }
    }
}

struct ChecklistItemEntityQuery: EntityStringQuery {
    func entities(for identifiers: [ChecklistItemEntity.ID]) async throws -> [ChecklistItemEntity] {
        var entityList: [ChecklistItemEntity] = []
        for identifier in identifiers {
            guard let entityID = ChecklistItemEntityID(rawValue: identifier),
                  let detail = try await ChecklistEntityQuery.detail(checklistId: entityID.checklistId, itemSort: nil),
                  let item = detail.items.first(where: { item in item.id == entityID.itemId })
            else { continue }
            entityList.append(ChecklistItemEntity(checklistId: entityID.checklistId, item: item))
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [ChecklistItemEntity] {
        try await appAction { try await DiaryChecklistSearchAppAction.shared.searchItem(query: string) }
            .map { match in ChecklistItemEntity(checklistId: match.checklistId, item: match.item) }
    }

    // 항목 선택 목록은 첫 페이지 체크리스트들의 항목을 체크리스트 순서대로 보여 준다.
    func suggestedEntities() async throws -> [ChecklistItemEntity] {
        var entityList: [ChecklistItemEntity] = []
        for summary in try await ChecklistEntityQuery.firstPage() {
            if let detail = try await ChecklistEntityQuery.detail(checklistId: summary.id, itemSort: nil) {
                entityList += detail.items.map { item in ChecklistItemEntity(checklistId: summary.id, item: item) }
            }
        }
        return entityList
    }
}
