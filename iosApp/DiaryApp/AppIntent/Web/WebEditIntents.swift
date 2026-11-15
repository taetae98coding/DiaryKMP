import AppIntents
import DiaryIos

struct AddWebIntent: AppIntent {
    static let title = LocalizedStringResource("Add Web", table: "AppIntentWeb")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new web. Page information isn't fetched, so enter the title yourself.", table: "AppIntentWeb")
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the web title?", table: "AppIntentWeb"))
    )
    var title: String

    @Parameter(
        title: LocalizedStringResource("URL", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current URL.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the URL?", table: "AppIntentWeb"))
    )
    var url: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var webDescription: String?

    @Parameter(
        title: LocalizedStringResource("Request Headers", table: "AppIntentCore"),
        description: LocalizedStringResource(
            "Headers sent when the page opens in the app, one `Name: value` per entry. When editing, they replace all current headers.",
            table: "AppIntentCore"
        )
    )
    var headers: [String]?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add web \(\.$title) \(\.$url)", table: "AppIntentWeb") {
            \.$webDescription
            \.$headers
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<WebEntity> & ProvidesDialog {
        let webId = try await appAction {
            try await DiaryWebAppAction.shared.add(
                title: title,
                url: url,
                webDescription: webDescription,
                headers: headers ?? [],
                tagIds: (tags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await WebEntityQuery.require(webId: webId),
            dialog: IntentDialog(LocalizedStringResource("Added the web.", table: "AppIntentWeb"))
        )
    }
}

struct EditWebIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Web", table: "AppIntentWeb")
    static let description = IntentDescription(
        LocalizedStringResource("Changes the title, description, URL or request headers of a web. Values left empty stay the same.", table: "AppIntentWeb")
    )

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var webDescription: String?

    @Parameter(
        title: LocalizedStringResource("URL", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current URL.", table: "AppIntentCore")
    )
    var url: String?

    @Parameter(
        title: LocalizedStringResource("Request Headers", table: "AppIntentCore"),
        description: LocalizedStringResource(
            "Headers sent when the page opens in the app, one `Name: value` per entry. When editing, they replace all current headers.",
            table: "AppIntentCore"
        )
    )
    var headers: [String]?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$web)", table: "AppIntentWeb") {
            \.$title
            \.$webDescription
            \.$url
            \.$headers
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await WebEntityQuery.require(webId: web.id)
        let changedCount = try await appAction {
            try await DiaryWebAppAction.shared.update(webId: web.id, title: title, webDescription: webDescription, url: url, headers: headers)
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the web.", table: "AppIntentWeb"))
        )
    }
}
