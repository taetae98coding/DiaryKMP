import DiaryIos
import SwiftUI
import UIKit

struct ContentView: View {
    var body: some View {
        DiaryComposeView().ignoresSafeArea()
    }
}

// 공유 UI는 Compose가 만든 UIViewController로만 받을 수 있어 SwiftUI에 끼워 넣는 경계만 UIKit에 둔다.
private struct DiaryComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        DiaryComposeViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
