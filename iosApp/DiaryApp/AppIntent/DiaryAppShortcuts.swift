import AppIntents

struct DiaryAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: AddMemoIntent(),
            phrases: [
                "Add a memo in \(.applicationName)",
                "Create a memo in \(.applicationName)",
                "New \(.applicationName) memo"
            ],
            shortTitle: LocalizedStringResource("Add Memo", table: "AppIntentShortcut"),
            systemImageName: "square.and.pencil"
        )
        AppShortcut(
            intent: SearchMemoIntent(),
            phrases: [
                "Search memos in \(.applicationName)",
                "Find a memo in \(.applicationName)",
                "Search \(.applicationName) memos"
            ],
            shortTitle: LocalizedStringResource("Search Memos", table: "AppIntentShortcut"),
            systemImageName: "magnifyingglass"
        )
        AppShortcut(
            intent: CalendarMemoByDateIntent(),
            phrases: [
                "Show memos for a date in \(.applicationName)",
                "Show \(.applicationName) calendar memos",
                "What's on my \(.applicationName) calendar"
            ],
            shortTitle: LocalizedStringResource("Memos by Date", table: "AppIntentShortcut"),
            systemImageName: "calendar"
        )
        AppShortcut(
            intent: MemoListIntent(),
            phrases: [
                "Show in-progress memos in \(.applicationName)",
                "Show my \(.applicationName) memos",
                "List memos in \(.applicationName)"
            ],
            shortTitle: LocalizedStringResource("In-Progress Memos", table: "AppIntentShortcut"),
            systemImageName: "list.bullet"
        )
        AppShortcut(
            intent: AddTagIntent(),
            phrases: [
                "Add a tag in \(.applicationName)",
                "Create a tag in \(.applicationName)"
            ],
            shortTitle: LocalizedStringResource("Add Tag", table: "AppIntentShortcut"),
            systemImageName: "tag"
        )
        AppShortcut(
            intent: AddChecklistIntent(),
            phrases: [
                "Add a checklist in \(.applicationName)",
                "Create a checklist in \(.applicationName)"
            ],
            shortTitle: LocalizedStringResource("Add Checklist", table: "AppIntentShortcut"),
            systemImageName: "checklist"
        )
        AppShortcut(
            intent: AddWebIntent(),
            phrases: [
                "Add a web in \(.applicationName)",
                "Save a web page in \(.applicationName)"
            ],
            shortTitle: LocalizedStringResource("Add Web", table: "AppIntentShortcut"),
            systemImageName: "globe"
        )
        AppShortcut(
            intent: SearchPlaceIntent(),
            phrases: [
                "Search saved places in \(.applicationName)",
                "Find a place in \(.applicationName)"
            ],
            shortTitle: LocalizedStringResource("Search Places", table: "AppIntentShortcut"),
            systemImageName: "mappin.and.ellipse"
        )
        AppShortcut(
            intent: AddMusicIntent(),
            phrases: [
                "Add a song in \(.applicationName)",
                "Add a song to my \(.applicationName) playlist"
            ],
            shortTitle: LocalizedStringResource("Add Song", table: "AppIntentShortcut"),
            systemImageName: "music.note"
        )
        AppShortcut(
            intent: AddQrIntent(),
            phrases: [
                "Add a QR code in \(.applicationName)",
                "Save a QR code in \(.applicationName)"
            ],
            shortTitle: LocalizedStringResource("Add QR Code", table: "AppIntentShortcut"),
            systemImageName: "qrcode"
        )
    }
}
