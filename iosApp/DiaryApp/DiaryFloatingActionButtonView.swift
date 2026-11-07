import DiaryIos
import SwiftUI
import UIKit

// 공유 Compose 화면의 떠 있는 동작 버튼을 iOS 26 Liquid Glass로 그린다. 무엇을 그릴지는 Kotlin이 넘겨 주는 상태가 정한다.
// 부모인 Kotlin 클래스가 격리되지 않았으므로 이 클래스도 격리하지 않고, Compose가 메인 스레드에서 부르는 것에 기대어 화면을 다룬다.
nonisolated final class DiaryFloatingActionButtonRendererImpl: DiaryFloatingActionButtonRendererBase, DiaryFloatingActionButtonRenderer {
    func createViewController() -> UIViewController {
        MainActor.assumeIsolated {
            DiaryFloatingActionButtonViewController()
        }
    }

    func update(viewController: UIViewController, state: DiaryFloatingActionButtonState) {
        MainActor.assumeIsolated {
            guard let buttonViewController = viewController as? DiaryFloatingActionButtonViewController else { return }
            buttonViewController.update(state: state)
        }
    }
}

@Observable
final class DiaryFloatingActionButtonModel {
    var state: DiaryFloatingActionButtonState?
}

final class DiaryFloatingActionButtonViewController: UIViewController {
    private let model = DiaryFloatingActionButtonModel()
    private lazy var hostingController = UIHostingController(rootView: DiaryFloatingActionButtonView(model: model))

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        // 버튼 자리는 Compose가 시스템 영역을 피해 잡아 주므로 SwiftUI가 시스템 영역만큼 다시 비우지 않게 한다.
        hostingController.safeAreaRegions = []
        hostingController.view.backgroundColor = .clear
        addChild(hostingController)
        hostingController.view.frame = view.bounds
        hostingController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(hostingController.view)
        hostingController.didMove(toParent: self)
    }

    func update(state: DiaryFloatingActionButtonState) {
        model.state = state
        // SwiftUI는 상태 변화를 다음 화면 갱신 때 반영하므로, Compose가 그린 같은 프레임에 버튼도 맞도록 바로 다시 배치한다.
        hostingController.view.setNeedsLayout()
        hostingController.view.layoutIfNeeded()
    }
}

struct DiaryFloatingActionButtonView: View {
    let model: DiaryFloatingActionButtonModel

    var body: some View {
        if let state = model.state {
            DiaryFloatingActionButtonContent(state: state)
        }
    }
}

private struct DiaryFloatingActionButtonContent: View {
    let state: DiaryFloatingActionButtonState

    var body: some View {
        let size = CGFloat(state.size)

        // 나타나고 사라질 때 Compose가 버튼 자리를 키우고 줄이므로, 제 크기로 그린 버튼을 자리 크기에 맞춰 함께 키우고 줄인다.
        GeometryReader { proxy in
            button
                // glassProminent 버튼 스타일은 라벨 둘레에 여백을 더해 정한 지름보다 크게 그려지므로, 같은 강조 유리를 지름을 정해 직접 입힌다.
                .glassEffect(.regular.tint(Color(uiColor: state.containerColor)).interactive(), in: .circle)
                .scaleEffect(min(proxy.size.width, proxy.size.height) / size)
                .frame(width: proxy.size.width, height: proxy.size.height)
                .accessibilityLabel(state.contentDescription)
                .help(state.contentDescription)
        }
    }

    @ViewBuilder
    private var button: some View {
        let menuItems = state.menuItems

        if menuItems.isEmpty {
            Button {
                state.click()
            } label: {
                label
            }
            .buttonStyle(.plain)
        } else {
            // 메뉴 버튼은 누르면 시스템 메뉴를 펼치고, 시스템 메뉴가 펼친 상태를 보여 주므로 아이콘을 바꾸지 않는다.
            Menu {
                ForEach(menuItems, id: \.id) { item in
                    Button {
                        item.click()
                    } label: {
                        if let image = item.image {
                            Label {
                                Text(item.text)
                            } icon: {
                                Image(uiImage: image).renderingMode(.template)
                            }
                        } else {
                            Text(item.text)
                        }
                    }
                }
            } label: {
                label
            }
            .menuStyle(.button)
            .menuOrder(.fixed)
            .buttonStyle(.plain)
        }
    }

    private var label: some View {
        let size = CGFloat(state.size)

        return ZStack {
            if state.isInProgress {
                ProgressView()
                    .transition(.opacity)
            } else if let image = state.image {
                Image(uiImage: image)
                    .renderingMode(.template)
                    .id(ObjectIdentifier(image))
                    .transition(.opacity)
            }
        }
        .frame(width: size, height: size)
        .contentShape(.circle)
        .animation(.default, value: state.isInProgress)
    }
}
