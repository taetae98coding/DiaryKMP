package io.github.taetae98coding.diary.compose.core.button

import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

public interface UiKitFloatingActionButtonRenderer {
    public fun createViewController(): UIViewController

    public fun update(
        viewController: UIViewController,
        state: UiKitFloatingActionButtonState,
    )
}

public class UiKitFloatingActionButtonState internal constructor(
    public val image: UIImage?,
    public val contentDescription: String,
    public val isInProgress: Boolean,
    public val containerColor: UIColor,
    public val contentColor: UIColor,
    public val size: Double,
    public val menuItems: List<UiKitFloatingActionButtonMenuItem>,
    private val onClick: () -> Unit,
) {
    public fun click() {
        onClick()
    }
}

public class UiKitFloatingActionButtonMenuItem internal constructor(
    public val id: String,
    public val text: String,
    public val image: UIImage?,
    private val onClick: () -> Unit,
) {
    public fun click() {
        onClick()
    }
}
