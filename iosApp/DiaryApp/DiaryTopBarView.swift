import DiaryIos
import SwiftUI
import UIKit

// 공유 Compose 화면의 상단 바를 iOS 26 Liquid Glass로 그린다. 무엇을 그릴지는 Kotlin이 넘겨 주는 상태가 정한다.
// 부모인 Kotlin 클래스가 격리되지 않았으므로 이 클래스도 격리하지 않고, Compose가 메인 스레드에서 부르는 것에 기대어 화면을 다룬다.
nonisolated final class DiaryTopBarRendererImpl: DiaryTopBarRendererBase, DiaryTopBarRenderer {
    func createViewController() -> UIViewController {
        MainActor.assumeIsolated {
            DiaryTopBarViewController()
        }
    }

    func update(viewController: UIViewController, state: DiaryTopBarState) {
        MainActor.assumeIsolated {
            guard let topBarViewController = viewController as? DiaryTopBarViewController else { return }
            topBarViewController.update(state: state)
        }
    }
}

@Observable
final class DiaryTopBarModel {
    var state: DiaryTopBarState?

    // 누름을 받을지 판단할 때만 읽으므로 바뀌어도 상단 바를 다시 그리지 않는다.
    @ObservationIgnored var glassFrames: [DiaryTopBarGlass: CGRect] = [:]
}

enum DiaryTopBarGlass {
    case navigateUp
    case actions
    case search
}

final class DiaryTopBarViewController: UIViewController {
    private let model = DiaryTopBarModel()
    private lazy var hostingController = UIHostingController(rootView: DiaryTopBarView(model: model))

    override func loadView() {
        view = DiaryTopBarPassthroughView(model: model)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        // 상단 바 자리는 Compose가 시스템 영역 아래로 잡아 주므로 SwiftUI가 시스템 영역만큼 다시 비우지 않게 한다.
        hostingController.safeAreaRegions = []
        hostingController.view.backgroundColor = .clear
        addChild(hostingController)
        hostingController.view.frame = view.bounds
        hostingController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(hostingController.view)
        hostingController.didMove(toParent: self)
    }

    func update(state: DiaryTopBarState) {
        model.state = state
        // SwiftUI는 상태 변화를 다음 화면 갱신 때 반영하므로, Compose가 그린 같은 프레임에 상단 바도 맞도록 바로 다시 배치한다.
        hostingController.view.setNeedsLayout()
        hostingController.view.layoutIfNeeded()
    }
}

// 제목은 이 뷰 아래의 Compose가 그린다. SwiftUI 호스팅 뷰는 아무것도 없는 자리의 누름도 가져가므로,
// 유리 면 자리의 누름만 받고 나머지는 아래 Compose로 넘긴다.
private final class DiaryTopBarPassthroughView: UIView {
    private let model: DiaryTopBarModel

    init(model: DiaryTopBarModel) {
        self.model = model
        super.init(frame: .zero)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard model.glassFrames.values.contains(where: { frame in frame.contains(point) }) else { return nil }
        return super.hitTest(point, with: event)
    }
}

struct DiaryTopBarView: View {
    let model: DiaryTopBarModel

    var body: some View {
        if let state = model.state {
            DiaryTopBarContent(state: state, model: model)
        }
    }
}

private nonisolated enum DiaryTopBarMetrics {
    static let horizontalPadding: CGFloat = 16
    static let buttonSize: CGFloat = 44
    static let spacing: CGFloat = 8
    static let coordinateSpace = "DiaryTopBar"
}

private struct DiaryTopBarContent: View {
    let state: DiaryTopBarState
    let model: DiaryTopBarModel

    var body: some View {
        let highlightColor = Color(uiColor: state.highlightColor)

        DiaryTopBarLayout {
            leading(highlightColor: highlightColor)
            search
            trailing(highlightColor: highlightColor)
        }
        .padding(.horizontal, DiaryTopBarMetrics.horizontalPadding)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .coordinateSpace(.named(DiaryTopBarMetrics.coordinateSpace))
    }

    @ViewBuilder
    private func leading(highlightColor: Color) -> some View {
        if let navigateUp = state.navigateUp {
            DiaryTopBarButtonView(button: navigateUp, highlightColor: highlightColor)
                .glassEffect(.regular.interactive(), in: .circle)
                .diaryTopBarGlassFrame(.navigateUp, model: model)
        } else {
            Color.clear.frame(width: 0, height: 0)
        }
    }

    @ViewBuilder
    private var search: some View {
        if let search = state.search {
            DiaryTopBarSearchField(search: search)
                .diaryTopBarGlassFrame(.search, model: model)
        } else {
            Color.clear.frame(width: 0, height: 0)
        }
    }

    @ViewBuilder
    private func trailing(highlightColor: Color) -> some View {
        let visibleActions = state.actions.filter(\.isVisible)

        if visibleActions.isEmpty {
            Color.clear.frame(width: 0, height: 0)
        } else {
            HStack(spacing: 0) {
                ForEach(visibleActions, id: \.id) { action in
                    DiaryTopBarButtonView(button: action, highlightColor: highlightColor)
                        .transition(.scale.combined(with: .opacity))
                }
            }
            .glassEffect(.regular.interactive(), in: .capsule)
            .diaryTopBarGlassFrame(.actions, model: model)
            // 목록에 넣고 빼는 버튼은 곧바로 나타나고 사라지고, 숨김 여부를 가진 버튼만 바뀔 때 전환을 거친다.
            .animation(.default, value: state.actions.filter { !$0.isVisible }.map(\.id))
        }
    }
}

private struct DiaryTopBarButtonView: View {
    let button: DiaryTopBarButton
    let highlightColor: Color

    var body: some View {
        Button {
            button.click()
        } label: {
            ZStack {
                if button.isInProgress {
                    ProgressView()
                        .transition(.opacity)
                } else if let image = button.image {
                    Image(uiImage: image)
                        .renderingMode(.template)
                        .foregroundStyle(button.isHighlighted ? highlightColor : Color.primary)
                        .id(ObjectIdentifier(image))
                        .transition(.opacity)
                } else if let text = button.text {
                    Text(text)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(Color.primary)
                }
            }
            .frame(width: DiaryTopBarMetrics.buttonSize, height: DiaryTopBarMetrics.buttonSize)
            .contentShape(.rect)
            .animation(.default, value: button.isInProgress)
            .animation(.default, value: button.image.map(ObjectIdentifier.init))
        }
        .buttonStyle(.plain)
        .disabled(!button.isEnabled)
        .opacity(button.isEnabled ? 1 : 0.4)
        .accessibilityLabel(button.contentDescription)
        .accessibilityValue(button.stateDescription ?? "")
        .accessibilityAddTraits(button.isToggle ? .isToggle : [])
        .accessibilityAddTraits(button.isToggle && button.isHighlighted ? .isSelected : [])
        .help(button.contentDescription)
    }
}

private struct DiaryTopBarSearchField: View {
    let search: DiaryTopBarSearch

    @State private var text = ""
    @State private var handledFocusRequestCount: Int32 = 0
    @FocusState private var isFocused: Bool

    var body: some View {
        HStack(spacing: DiaryTopBarMetrics.spacing) {
            if let searchImage = search.searchImage {
                Image(uiImage: searchImage)
                    .renderingMode(.template)
                    .foregroundStyle(.secondary)
                    .accessibilityHidden(true)
            }
            TextField(search.placeholder, text: $text)
                .focused($isFocused)
                .submitLabel(.search)
                .onSubmit { isFocused = false }
            if !text.isEmpty, let clearImage = search.clearImage {
                Button {
                    text = ""
                    isFocused = true
                } label: {
                    Image(uiImage: clearImage)
                        .renderingMode(.template)
                        .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(search.clearAccessibilityLabel)
                .transition(.scale.combined(with: .opacity))
            }
        }
        .padding(.horizontal, 12)
        .frame(height: DiaryTopBarMetrics.buttonSize)
        .glassEffect(.regular.interactive(), in: .capsule)
        .animation(.default, value: text.isEmpty)
        .onAppear {
            text = search.text
            handleFocusRequest(search.focusRequestCount)
        }
        .onChange(of: text) { _, newValue in
            if newValue != search.text {
                search.changeText(text: newValue)
            }
        }
        .onChange(of: search.text) { _, newValue in
            if newValue != text {
                text = newValue
            }
        }
        .onChange(of: search.focusRequestCount) { _, newValue in
            handleFocusRequest(newValue)
        }
    }

    private func handleFocusRequest(_ count: Int32) {
        guard count != handledFocusRequestCount else { return }
        handledFocusRequestCount = count
        isFocused = true
    }
}

private extension View {
    func diaryTopBarGlassFrame(_ glass: DiaryTopBarGlass, model: DiaryTopBarModel) -> some View {
        onGeometryChange(for: CGRect.self) { proxy in
            proxy.frame(in: .named(DiaryTopBarMetrics.coordinateSpace))
        } action: { frame in
            model.glassFrames[glass] = frame
        }
        .onDisappear {
            model.glassFrames[glass] = nil
        }
    }
}

// 양쪽 버튼을 끝에 두고, 검색 입력은 두 버튼 사이에 남은 자리를 모두 쓴다.
private struct DiaryTopBarLayout: Layout {
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        proposal.replacingUnspecifiedDimensions()
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        guard subviews.count == 3 else { return }

        let leadingSize = subviews[0].sizeThatFits(.unspecified)
        let trailingSize = subviews[2].sizeThatFits(.unspecified)

        subviews[0].place(at: CGPoint(x: bounds.minX, y: bounds.midY), anchor: .leading, proposal: ProposedViewSize(leadingSize))
        subviews[2].place(at: CGPoint(x: bounds.maxX, y: bounds.midY), anchor: .trailing, proposal: ProposedViewSize(trailingSize))

        let leadingInset = leadingSize.width > 0 ? leadingSize.width + DiaryTopBarMetrics.spacing : 0
        let trailingInset = trailingSize.width > 0 ? trailingSize.width + DiaryTopBarMetrics.spacing : 0
        let betweenWidth = max(bounds.width - leadingInset - trailingInset, 0)

        subviews[1].place(
            at: CGPoint(x: bounds.minX + leadingInset + betweenWidth / 2, y: bounds.midY),
            anchor: .center,
            proposal: ProposedViewSize(width: betweenWidth, height: bounds.height)
        )
    }
}
