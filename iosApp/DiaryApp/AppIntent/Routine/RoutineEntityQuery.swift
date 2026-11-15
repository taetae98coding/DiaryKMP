import AppIntents
import DiaryIos

struct RoutineEntityQuery: EntityStringQuery {
    // ‘삭제한 루틴 되돌리기’에 넘긴 항목을 다시 찾을 수 있게 삭제된 항목도 찾는다.
    func entities(for identifiers: [RoutineEntity.ID]) async throws -> [RoutineEntity] {
        var entityList: [RoutineEntity] = []
        for identifier in identifiers {
            if let detail = try await appAction({ try await DiaryRoutineAppAction.shared.getIncludingDeleted(routineId: identifier) }) {
                entityList.append(RoutineEntity(detail: detail))
            }
        }
        return entityList
    }

    func entities(matching string: String) async throws -> [RoutineEntity] {
        try await appAction {
            try await DiarySearchAppAction.shared.routine(query: string, pageNumber: AppIntentPage.number(nil))
        }.items.map(RoutineEntity.init(routine:))
    }

    func suggestedEntities() async throws -> [RoutineEntity] {
        let finished = try await appAction { try await DiaryRoutineAppAction.shared.finishedList(pageNumber: AppIntentPage.number(nil)) }
        return (try await RoutineEntityQuery.firstPage() + finished.items).map(RoutineEntity.init(routine:))
    }

    nonisolated static func firstPage() async throws -> [DiaryAppActionRoutine] {
        try await appAction { try await DiaryRoutineAppAction.shared.list(pageNumber: AppIntentPage.number(nil)) }.items
    }

    // 상세와 추가 결과는 데스크톱 연결과 같이 삭제된 항목을 없는 항목으로 다룬다.
    static func require(routineId: String) async throws -> RoutineEntity {
        guard let detail = try await appAction({ try await DiaryRoutineAppAction.shared.get(routineId: routineId) }) else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the routine.", table: "AppIntentRoutine"))
        }
        return RoutineEntity(detail: detail)
    }
}

struct RoutineRecordEntityQuery: EntityQuery {
    func entities(for identifiers: [RoutineRecordEntity.ID]) async throws -> [RoutineRecordEntity] {
        var entityList: [RoutineRecordEntity] = []
        for identifier in identifiers {
            guard let entityID = RoutineRecordEntityID(rawValue: identifier),
                  let record = try await RoutineRecordEntityQuery.find(routineId: entityID.routineId, recordId: entityID.recordId)
            else { continue }
            entityList.append(RoutineRecordEntity(record: record))
        }
        return entityList
    }

    // 기록 선택 목록은 첫 페이지 루틴들의 첫 페이지 기록을 루틴 순서대로 보여 준다.
    func suggestedEntities() async throws -> [RoutineRecordEntity] {
        var entityList: [RoutineRecordEntity] = []
        for routine in try await RoutineEntityQuery.firstPage() {
            let recordPage = try await appAction {
                try await DiaryRoutineRecordAppAction.shared.list(routineId: routine.id, pageNumber: AppIntentPage.number(nil))
            }
            entityList += recordPage.items.map(RoutineRecordEntity.init(record:))
        }
        return entityList
    }

    nonisolated static func find(routineId: String, recordId: String) async throws -> DiaryAppActionRoutineRecord? {
        try await appAction { try await DiaryRoutineRecordAppAction.shared.find(routineId: routineId, recordId: recordId) }
    }

    // 삭제된 루틴의 기록과 이미 뺀 기록은 데스크톱 연결과 같이 없는 기록으로 다룬다.
    static func require(record: RoutineRecordEntity) async throws {
        guard let routineId = record.routineId else { return }
        guard try await find(routineId: routineId, recordId: record.recordId) != nil else {
            throw AppIntentError.notFound(LocalizedStringResource("Couldn't find the routine record.", table: "AppIntentRoutine"))
        }
    }
}
