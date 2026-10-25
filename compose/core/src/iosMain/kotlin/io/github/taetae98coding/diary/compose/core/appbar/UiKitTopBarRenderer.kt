package io.github.taetae98coding.diary.compose.core.appbar

import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

public interface UiKitTopBarRenderer {
    public fun createViewController(): UIViewController

    public fun update(
        viewController: UIViewController,
        state: UiKitTopBarState,
    )
}

public object UiKitTopBarRendererRegistry {
    public var renderer: UiKitTopBarRenderer? = null
}

public class UiKitTopBarState internal constructor(
    public val search: UiKitTopBarSearch?,
    public val navigateUp: UiKitTopBarButton?,
    public val actions: List<UiKitTopBarButton>,
    public val highlightColor: UIColor,
)

public class UiKitTopBarButton internal constructor(
    public val id: String,
    public val image: UIImage?,
    public val text: String?,
    public val contentDescription: String,
    public val stateDescription: String?,
    public val isEnabled: Boolean,
    public val isInProgress: Boolean,
    public val isHighlighted: Boolean,
    public val isToggle: Boolean,
    public val isVisible: Boolean,
    private val onClick: () -> Unit,
) {
    public fun click() {
        onClick()
    }
}

public class UiKitTopBarSearch internal constructor(
    public val placeholder: String,
    public val searchImage: UIImage?,
    public val clearImage: UIImage?,
    public val clearAccessibilityLabel: String,
    private val textProvider: () -> String,
    private val focusRequestCountProvider: () -> Int,
    private val onTextChange: (String) -> Unit,
) {
    public val text: String get() = textProvider()

    // 공유 화면이 검색 입력에 초점을 요청할 때마다 1씩 커진다. SwiftUI는 값이 바뀌면 입력에 초점을 준다.
    public val focusRequestCount: Int get() = focusRequestCountProvider()

    public fun changeText(text: String) {
        onTextChange(text)
    }
}
