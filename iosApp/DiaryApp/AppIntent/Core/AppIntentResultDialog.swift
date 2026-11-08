import AppIntents

nonisolated enum AppIntentResultDialog {
    // 다음 페이지가 있으면 한 페이지가 가득 찬 것이므로, 다음 페이지 문구에는 단수형이 필요 없다.
    static func list(
        count: Int,
        nextPageNumber: Int32?,
        found: (Int) -> LocalizedStringResource,
        foundWithNextPage: (Int, Int) -> LocalizedStringResource
    ) -> IntentDialog {
        if count == 0 {
            return IntentDialog(LocalizedStringResource("No results.", table: "AppIntentCore"))
        }
        if let nextPageNumber {
            return IntentDialog(foundWithNextPage(count, Int(nextPageNumber)))
        }
        return IntentDialog(found(count))
    }

    static func changed(count: Int32, changed: LocalizedStringResource) -> IntentDialog {
        if count > 0 {
            return IntentDialog(changed)
        }
        return IntentDialog(LocalizedStringResource("Nothing changed.", table: "AppIntentCore"))
    }
}

nonisolated enum AppIntentPage {
    // 페이지를 고르지 않으면 첫 페이지다. 1보다 작은 번호는 Kotlin이 데스크톱 연결과 같은 방식으로 실패를 알린다.
    static func number(_ page: Int?) -> Int32 {
        Int32(clamping: page ?? 1)
    }
}
