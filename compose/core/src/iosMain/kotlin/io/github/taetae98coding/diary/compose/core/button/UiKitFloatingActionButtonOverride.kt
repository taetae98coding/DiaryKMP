@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import io.github.taetae98coding.diary.compose.core.appbar.LocalDiaryTopBarAlpha
import io.github.taetae98coding.diary.compose.core.graphics.rememberTemplateUIImage
import io.github.taetae98coding.diary.compose.core.graphics.toUIColor
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import platform.UIKit.UIImage

public class UiKitFloatingActionButtonOverride(
    private val renderer: UiKitFloatingActionButtonRenderer,
) : DiaryFloatingActionButtonOverride {
    @Composable
    override fun DiaryFloatingActionButtonOverrideScope.FloatingActionButton() {
        UiKitFloatingActionButton(
            renderer = renderer,
            image = rememberTemplateUIImage(imageVector = imageVector, size = DiaryFloatingActionButtonDefaults.IosIconSize),
            contentDescription = contentDescription.orEmpty(),
            onClick = onClick,
            modifier = modifier,
            isInProgressProvider = isInProgressProvider,
        )
    }
}

public class UiKitFloatingActionButtonMenuOverride(
    private val renderer: UiKitFloatingActionButtonRenderer,
) : DiaryFloatingActionButtonMenuOverride {
    @Composable
    override fun DiaryFloatingActionButtonMenuOverrideScope.FloatingActionButtonMenu() {
        val menuItems =
            items.map { item ->
                UiKitFloatingActionButtonMenuItem(
                    id = item.icon.name,
                    text = item.text,
                    image = rememberTemplateUIImage(imageVector = item.icon.imageVector, size = DiaryFloatingActionButtonDefaults.IosIconSize),
                    onClick = item.onClick,
                )
            }

        UiKitFloatingActionButton(
            renderer = renderer,
            image = rememberTemplateUIImage(imageVector = icon.imageVector, size = DiaryFloatingActionButtonDefaults.IosIconSize),
            contentDescription = contentDescription,
            onClick = {},
            modifier = modifier,
            menuItems = menuItems,
        )
    }
}

@Composable
private fun UiKitFloatingActionButton(
    renderer: UiKitFloatingActionButtonRenderer,
    image: UIImage?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInProgressProvider: () -> Boolean = { false },
    menuItems: List<UiKitFloatingActionButtonMenuItem> = emptyList(),
) {
    // update 블록은 그 안에서 읽은 상태가 바뀔 때만 다시 실행되므로, composition에서 바뀌는 값도 상태로 감싸 그 안에서 읽는다.
    val currentImage by rememberUpdatedState(image)
    val currentContentDescription by rememberUpdatedState(contentDescription)
    val currentIsInProgressProvider by rememberUpdatedState(isInProgressProvider)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentMenuItems by rememberUpdatedState(menuItems)
    val containerColor by rememberUpdatedState(DiaryTheme.colorScheme.primary.toUIColor())
    val contentColor by rememberUpdatedState(DiaryTheme.colorScheme.onPrimary.toUIColor())
    val alphaProvider = LocalDiaryTopBarAlpha.current

    UIKitViewController(
        factory = renderer::createViewController,
        modifier = modifier.size(DiaryFloatingActionButtonDefaults.IosSize),
        update = { viewController ->
            viewController.view.alpha = alphaProvider().toDouble()
            renderer.update(
                viewController = viewController,
                state =
                    UiKitFloatingActionButtonState(
                        image = currentImage,
                        contentDescription = currentContentDescription,
                        isInProgress = currentIsInProgressProvider(),
                        containerColor = containerColor,
                        contentColor = contentColor,
                        size = DiaryFloatingActionButtonDefaults.IosSize.value.toDouble(),
                        menuItems = currentMenuItems,
                        onClick = { currentOnClick() },
                    ),
            )
        },
        properties =
            UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
                // 유리 질감이 아래의 Compose 화면을 비추려면 SwiftUI 뷰가 Compose 화면 위에 놓여야 한다.
                placedAsOverlay = true,
            ),
    )
}
