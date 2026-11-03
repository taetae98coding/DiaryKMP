package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.compose.core.button.UiKitFloatingActionButtonMenuItem
import io.github.taetae98coding.diary.compose.core.button.UiKitFloatingActionButtonRenderer
import io.github.taetae98coding.diary.compose.core.button.UiKitFloatingActionButtonState
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

public interface DiaryFloatingActionButtonRenderer {
    public fun createViewController(): UIViewController

    public fun update(
        viewController: UIViewController,
        state: DiaryFloatingActionButtonState,
    )
}

// Swift 클래스가 Kotlin 인터페이스를 구현하려면 Swift export가 내보낸 Kotlin open 클래스를 함께 상속해야 한다.
public open class DiaryFloatingActionButtonRendererBase

internal class DiaryFloatingActionButtonRendererAdapter(
    private val renderer: DiaryFloatingActionButtonRenderer,
) : UiKitFloatingActionButtonRenderer {
    override fun createViewController(): UIViewController = renderer.createViewController()

    override fun update(
        viewController: UIViewController,
        state: UiKitFloatingActionButtonState,
    ) {
        renderer.update(viewController = viewController, state = DiaryFloatingActionButtonState(state = state))
    }
}

public class DiaryFloatingActionButtonState internal constructor(
    private val state: UiKitFloatingActionButtonState,
) {
    public val image: UIImage? get() = state.image
    public val contentDescription: String get() = state.contentDescription
    public val isInProgress: Boolean get() = state.isInProgress
    public val containerColor: UIColor get() = state.containerColor
    public val contentColor: UIColor get() = state.contentColor
    public val size: Double get() = state.size
    public val menuItems: List<DiaryFloatingActionButtonMenuItem> get() = state.menuItems.map(::DiaryFloatingActionButtonMenuItem)

    public fun click() {
        state.click()
    }
}

public class DiaryFloatingActionButtonMenuItem internal constructor(
    private val item: UiKitFloatingActionButtonMenuItem,
) {
    public val id: String get() = item.id
    public val text: String get() = item.text
    public val image: UIImage? get() = item.image

    public fun click() {
        item.click()
    }
}
