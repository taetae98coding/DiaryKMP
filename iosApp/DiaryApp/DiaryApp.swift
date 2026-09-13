import DiaryIos
import FirebaseCore
import GoogleMaps
import SwiftUI

@main
struct DiaryApp: App {
    init() {
        FirebaseApp.configure()
        DiaryStartupInitializer.shared.start()

        if let googleMapApiKey = Bundle.main.object(forInfoDictionaryKey: "GMSApiKey") as? String {
            GMSServices.provideAPIKey(googleMapApiKey)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
