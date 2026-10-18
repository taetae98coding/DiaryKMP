import DiaryIos
import SwiftUI
import UIKit

// 공유 Compose 화면의 상단 바를 iOS 26 Liquid Glass로 그린다. 무엇을 그릴지는 Kotlin이 넘겨 주는 상태가 정한다.
// 부모인 Kotlin 클래스가 격리되지 않았으므로 이 클래스도 격리하지 않고, Compose가 메인 스레드에서 부르는 것에 기대어 화면을 다룬다.
nonisolated final class DiaryTopBarRendererImpl: DiaryTopBarRendererBase, DiaryTopBarRenderer {
    func createViewController() -> UIViewController {
        MainActor.assumeIsolated {
            DiaryTopBarHostingController()
        }
    }

    func update(viewController: UIViewController, state: DiaryTopBarState) {
        MainActor.assumeIsolated {
            guard let hostingController = viewController as? DiaryTopBarHostingController else { return }
            hostingController.model.state = state
            // SwiftUI는 상태 변화를 다음 화면 갱신 때 반영하므로, Compose가 그린 같은 프레임에 상단 바도 맞도록 바로 다시 배치한다.
            hostingController.view.setNeedsLayout()
            hostingController.view.layoutIfNeeded()
        }
    }
}

@Observable
final class DiaryTopBarModel {
    var state: DiaryTopBarState?
}

final class DiaryTopBarHostingController: UIHostingController<DiaryTopBarView> {
    let model: DiaryTopBarModel

    init() {
        let model = DiaryTopBarModel()
        self.model = model
        super.init(rootView: DiaryTopBarView(model: model))
        // 상단 바 자리는 Compose가 시스템 영역 아래로 잡아 주므로 SwiftUI가 시스템 영역만큼 다시 비우지 않게 한다.
        safeAreaRegions = []
        view.backgroundColor = .clear
    }

    @MainActor @preconcurrency required dynamic init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }
}

struct DiaryTopBarView: View {
    let model: DiaryTopBarModel

    var body: some View {
        if let state = model.state {
            DiaryTopBarContent(state: state)
        }
    }
}

private nonisolated enum DiaryTopBarMetrics {
    static let horizontalPadding: CGFloat = 16
    static let buttonSize: CGFloat = 44
    static let spacing: CGFloat = 8
}

private struct DiaryTopBarContent: View {
    let state: DiaryTopBarState

    var body: some View {
        let highlightColor = Color(uiColor: state.highlightColor)

        DiaryTopBarLayout(fillsTitleWidth: state.search != nil) {
            leading(highlightColor: highlightColor)
            title
            trailing(highlightColor: highlightColor)
        }
        .padding(.horizontal, DiaryTopBarMetrics.horizontalPadding)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    @ViewBuilder
    private func leading(highlightColor: Color) -> some View {
        if let navigateUp = state.navigateUp {
            DiaryTopBarButtonView(button: navigateUp, highlightColor: highlightColor)
                .glassEffect(.regular.interactive(), in: .circle)
        } else {
            Color.clear.frame(width: 0, height: 0)
        }
    }

    @ViewBuilder
    private var title: some View {
        if let search = state.search {
            DiaryTopBarSearchField(search: search)
        } else if let titleButton = state.titleButton {
            Button {
                titleButton.click()
            } label: {
                HStack(spacing: 2) {
                    Text(state.title)
                        .font(.headline)
                        .lineLimit(1)
                    if let image = titleButton.image {
                        Image(uiImage: image)
                            .renderingMode(.template)
                    }
                }
            }
            .buttonStyle(.plain)
            .foregroundStyle(.primary)
            .accessibilityLabel(state.title)
            .accessibilityHint(titleButton.hintDescription)
        } else {
            VStack(spacing: 0) {
                Text(state.title)
                    .font(.headline)
                    .lineLimit(1)
                    .truncationMode(.tail)
                if let subtitle = state.subtitle {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
            .accessibilityElement(children: .combine)
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

// iOS 상단 바처럼 제목을 화면 가운데에 두되, 양쪽 버튼과 겹치면 버튼 사이 남은 자리를 쓴다.
private struct DiaryTopBarLayout: Layout {
    let fillsTitleWidth: Bool

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
        let centeredWidth = max(bounds.width - max(leadingInset, trailingInset) * 2, 0)
        let titleWidth = subviews[1].sizeThatFits(ProposedViewSize(width: betweenWidth, height: bounds.height)).width

        if !fillsTitleWidth && titleWidth <= centeredWidth {
            subviews[1].place(
                at: CGPoint(x: bounds.midX, y: bounds.midY),
                anchor: .center,
                proposal: ProposedViewSize(width: centeredWidth, height: bounds.height)
            )
        } else {
            subviews[1].place(
                at: CGPoint(x: bounds.minX + leadingInset + betweenWidth / 2, y: bounds.midY),
                anchor: .center,
                proposal: ProposedViewSize(width: betweenWidth, height: bounds.height)
            )
        }
    }
}
