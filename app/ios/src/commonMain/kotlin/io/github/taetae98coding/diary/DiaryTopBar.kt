package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarButton
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarRenderer
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarRendererRegistry
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarSearch
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarState
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarTitleButton
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

public interface DiaryTopBarRenderer {
    public fun createViewController(): UIViewController

    public fun update(
        viewController: UIViewController,
        state: DiaryTopBarState,
    )
}

// Swift 클래스가 Kotlin 인터페이스를 구현하려면 Swift export가 내보낸 Kotlin open 클래스를 함께 상속해야 한다.
public open class DiaryTopBarRendererBase

public data object DiaryTopBar {
    public fun register(renderer: DiaryTopBarRenderer) {
        UiKitTopBarRendererRegistry.renderer =
            object : UiKitTopBarRenderer {
                override fun createViewController(): UIViewController = renderer.createViewController()

                override fun update(
                    viewController: UIViewController,
                    state: UiKitTopBarState,
                ) {
                    renderer.update(viewController = viewController, state = DiaryTopBarState(state = state))
                }
            }
    }
}

public class DiaryTopBarState internal constructor(
    private val state: UiKitTopBarState,
) {
    public val title: String get() = state.title
    public val subtitle: String? get() = state.subtitle
    public val titleButton: DiaryTopBarTitleButton? get() = state.titleButton?.let(::DiaryTopBarTitleButton)
    public val search: DiaryTopBarSearch? get() = state.search?.let(::DiaryTopBarSearch)
    public val navigateUp: DiaryTopBarButton? get() = state.navigateUp?.let(::DiaryTopBarButton)
    public val actions: List<DiaryTopBarButton> get() = state.actions.map(::DiaryTopBarButton)
    public val highlightColor: UIColor get() = state.highlightColor
}

public class DiaryTopBarButton internal constructor(
    private val button: UiKitTopBarButton,
) {
    public val id: String get() = button.id
    public val image: UIImage? get() = button.image
    public val text: String? get() = button.text
    public val contentDescription: String get() = button.contentDescription
    public val stateDescription: String? get() = button.stateDescription
    public val isEnabled: Boolean get() = button.isEnabled
    public val isInProgress: Boolean get() = button.isInProgress
    public val isHighlighted: Boolean get() = button.isHighlighted
    public val isToggle: Boolean get() = button.isToggle
    public val isVisible: Boolean get() = button.isVisible

    public fun click() {
        button.click()
    }
}

public class DiaryTopBarTitleButton internal constructor(
    private val button: UiKitTopBarTitleButton,
) {
    public val image: UIImage? get() = button.image
    public val hintDescription: String get() = button.hintDescription

    public fun click() {
        button.click()
    }
}

public class DiaryTopBarSearch internal constructor(
    private val search: UiKitTopBarSearch,
) {
    public val text: String get() = search.text
    public val placeholder: String get() = search.placeholder
    public val searchImage: UIImage? get() = search.searchImage
    public val clearImage: UIImage? get() = search.clearImage
    public val clearAccessibilityLabel: String get() = search.clearAccessibilityLabel
    public val focusRequestCount: Int get() = search.focusRequestCount

    public fun changeText(text: String) {
        search.changeText(text = text)
    }
}
