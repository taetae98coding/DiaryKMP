@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.app.shared.scaffold

import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.topLevelNavigationList
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUUID
import platform.UIKit.UIImage
import platform.UIKit.UITab
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.UITabBarControllerModeTabSidebar
import platform.UIKit.UITabPlacementMovable
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.removeFromParentViewController
import platform.UIKit.willMoveToParentViewController

@Suppress("FunctionName")
public fun AppTabBarController(content: UIViewController): UIViewController = TopLevelTabBarController(content = content)

// UITabBarController는 탭마다 자식 화면을 요구하지만 내비게이션은 Compose 화면 하나가 맡으므로,
// 탭마다 빈 호스트를 두고 선택된 탭의 호스트로 Compose 화면을 옮겨 단다.
// Compose 화면은 부모가 window 계층에 남아 있으면 폐기되지 않으므로 옮기는 동안 상태가 유지된다.
internal class TopLevelTabBarController(
    private val content: UIViewController,
) : UITabBarController(nibName = null, bundle = null),
    UITabBarControllerDelegateProtocol {
    var onSelect: ((TopLevelNavigation) -> Unit)? = null

    private val hostList = topLevelNavigationList.map { UIViewController(nibName = null, bundle = null) }
    private val tabList =
        topLevelNavigationList.mapIndexed { index, topLevelNavigation ->
            UITab(
                title = "",
                image = null,
                identifier = topLevelNavigation.tabIdentifier,
                viewControllerProvider = { hostList[index] },
            ).apply {
                preferredPlacement = UITabPlacementMovable
            }
        }

    // UITabBarController는 초기화 도중에 viewDidLoad를 부르므로 프로퍼티가 준비된 뒤인 init에서 설정한다.
    init {
        delegate = this
        // 시스템은 편집한 순서와 구성을 이 식별자로 저장해 다음 실행에 다시 적용하므로, 실행마다 새 식별자를 주어 이번 실행에서만 유지한다.
        customizationIdentifier = NSUUID().UUIDString
        mode = UITabBarControllerModeTabSidebar
        setTabs(tabList)
        select(TopLevelNavigation.DEFAULT)
    }

    fun updateTabs(
        titleList: List<String>,
        imageList: List<UIImage?>,
    ) {
        tabList.forEachIndexed { index, tab ->
            tab.title = titleList[index]
            tab.image = imageList[index]
        }
    }

    fun select(topLevelNavigation: TopLevelNavigation) {
        val index = topLevelNavigationList.indexOf(topLevelNavigation)
        if (index < 0) return

        setSelectedTab(tabList[index])
        moveContent(host = hostList[index])
    }

    override fun tabBarController(
        tabBarController: UITabBarController,
        shouldSelectTab: UITab,
    ): Boolean {
        val onSelect = onSelect
        val topLevelNavigation = topLevelNavigationList.getOrNull(tabList.indexOf(shouldSelectTab))

        // 선택은 AppState가 정하고 그 결과를 select로 반영하므로, Compose가 연결된 뒤에는 UIKit이 직접 바꾸지 않게 한다.
        if (onSelect != null && topLevelNavigation != null) {
            onSelect(topLevelNavigation)
        }

        return onSelect == null
    }

    private fun moveContent(host: UIViewController) {
        if (content.parentViewController == host) return

        content.willMoveToParentViewController(null)
        content.view.removeFromSuperview()
        content.removeFromParentViewController()

        host.addChildViewController(content)
        content.view.setFrame(host.view.bounds)
        content.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        host.view.addSubview(content.view)
        content.didMoveToParentViewController(host)
    }
}

private val TopLevelNavigation.tabIdentifier: String
    get() =
        when (this) {
            TopLevelNavigation.Memo -> "memo"
            TopLevelNavigation.Tag -> "tag"
            TopLevelNavigation.Calendar -> "calendar"
            TopLevelNavigation.Routine -> "routine"
            TopLevelNavigation.More -> "more"
        }
