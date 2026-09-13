import DiaryIos
import SwiftUI
import UIKit

struct ContentView: View {
    var body: some View {
        DiaryComposeView().ignoresSafeArea()
    }
}

private struct DiaryComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        DiaryComposeViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
