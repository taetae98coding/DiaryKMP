@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.core.systemscroll

import androidx.compose.ui.geometry.Rect
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.UIKit.UIScrollViewDelegateProtocol
import platform.UIKit.UIView
import platform.UIKit.setAccessibilityElementsHidden
import platform.darwin.NSObject

private const val SCROLLED_OFFSET = 2.0

// iOS는 상태 표시줄 누름을 UIScrollView에만 전달하는데 Compose는 UIScrollView로 스크롤하지 않으므로, 누름을 대신 받을 스크롤 뷰를 목록 자리에 붙인다.
// iOS는 누른 가로 위치에 놓인 스크롤 뷰 가운데 맨 위가 아닌 가장 위쪽 하나에게 묻고, 그 스크롤 뷰가 맨 위로 가면 다른 스크롤 뷰에는 묻지 않는다.
// 이 스크롤 뷰의 자리와 맨 위 여부를 목록과 같게 맞춰 두면, 목록이 여럿이어도 UIKit·SwiftUI의 목록과 같은 순서로 하나씩 골라진다.
internal class SystemScrollTarget(
    private val hostView: UIView,
    private val canScrollToTop: () -> Boolean,
    onScrollToTop: () -> Unit,
) {
    // UIScrollView는 delegate를 약하게 잡으므로 여기서 잡아 둔다.
    private val delegate = SystemScrollDelegate(canScrollToTop = canScrollToTop, onScrollToTop = onScrollToTop, onDidScrollToTop = ::sync)

    // 숨기거나(hidden, alpha 0) 사용자 입력을 끄면 누름을 받지 못한다. 높이를 0으로 두어 보이지도 터치를 가로채지도 않게 한다.
    private val scrollView =
        UIScrollView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)).apply {
            contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
            showsVerticalScrollIndicator = false
            showsHorizontalScrollIndicator = false
            setAccessibilityElementsHidden(true)
            setContentSize(CGSizeMake(SCROLLED_OFFSET, SCROLLED_OFFSET))
        }

    init {
        scrollView.delegate = delegate
    }

    fun attach() {
        hostView.insertSubview(scrollView, atIndex = 0)
    }

    fun detach() {
        scrollView.removeFromSuperview()
    }

    // bounds는 Compose 루트 기준의 px 값이다.
    fun place(
        bounds: Rect,
        density: Float,
    ) {
        scrollView.setFrame(CGRectMake((bounds.left / density).toDouble(), (bounds.top / density).toDouble(), (bounds.width / density).toDouble(), 0.0))
    }

    fun sync() {
        val offset = if (canScrollToTop()) SCROLLED_OFFSET else 0.0

        scrollView.setContentOffset(CGPointMake(0.0, offset), animated = false)
    }
}

private class SystemScrollDelegate(
    private val canScrollToTop: () -> Boolean,
    private val onScrollToTop: () -> Unit,
    private val onDidScrollToTop: () -> Unit,
) : NSObject(),
    UIScrollViewDelegateProtocol {
    override fun scrollViewShouldScrollToTop(scrollView: UIScrollView): Boolean {
        // false를 주면 iOS가 다음 스크롤 뷰에 묻는다. 창이 떠 있으면 모두 false를 주어 아무것도 돌아가지 않는다.
        if (scrollView.isUnderOverlay() || !canScrollToTop()) return false

        onScrollToTop()
        return true
    }

    // iOS가 이 스크롤 뷰를 맨 위로 옮겼다. 목록은 아직 올라가는 중이거나 올라가지 않았을 수 있으므로 목록의 지금 상태로 되돌린다.
    override fun scrollViewDidScrollToTop(scrollView: UIScrollView) {
        onDidScrollToTop()
    }
}

// Compose는 화면 위에 다이얼로그·Bottom Sheet·메뉴가 떠 있는지를 그 아래 화면에 알려 주지 않는다.
// 그런 창은 같은 window에 루트 화면의 형제로 붙고 키 입력을 받으려고 first responder를 가져가므로,
// first responder가 루트 화면 밖에 있으면 창이 떠 있는 것으로 본다. UIKit이 띄운 화면은 presentedViewController로 안다.
private fun UIView.isUnderOverlay(): Boolean {
    val rootViewController = window?.rootViewController ?: return false
    val firstResponder = window?.findFirstResponder()

    return rootViewController.presentedViewController != null || firstResponder?.isDescendantOfView(rootViewController.view) == false
}

private fun UIView.findFirstResponder(): UIView? {
    if (isFirstResponder) return this

    return subviews.firstNotNullOfOrNull { subview -> (subview as UIView).findFirstResponder() }
}
