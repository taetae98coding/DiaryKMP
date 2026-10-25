@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.clear_text_field_button_content_description
import io.github.taetae98coding.diary.compose.core.graphics.rememberTemplateUIImage
import io.github.taetae98coding.diary.compose.core.graphics.toUIColor
import io.github.taetae98coding.diary.compose.core.icon.ArrowBack
import io.github.taetae98coding.diary.compose.core.icon.Close
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.Search
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource
import platform.UIKit.UIColor

@Composable
public actual fun DiaryTopBar(
    title: @Composable DiaryTopBarTitleScope.() -> Unit,
    modifier: Modifier,
    isTitleCentered: Boolean,
    navigationIcon: @Composable DiaryTopBarNavigationIconScope.() -> Unit,
    actions: @Composable DiaryTopBarActionScope.() -> Unit,
) {
    val renderer = UiKitTopBarRendererRegistry.renderer

    if (renderer == null) {
        MaterialDiaryTopBar(
            title = title,
            modifier = modifier,
            isTitleCentered = isTitleCentered,
            navigationIcon = navigationIcon,
            actions = actions,
        )
        return
    }

    val items = remember { UiKitTopBarItems() }
    val highlightColor by rememberUpdatedState(DiaryTheme.colorScheme.primary.toUIColor())
    val alphaProvider = LocalDiaryTopBarAlpha.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .height(DiaryTopBarDefaults.IosBarHeight),
    ) {
        UIKitViewController(
            factory = renderer::createViewController,
            modifier = Modifier.fillMaxSize(),
            // 상태를 이 블록 안에서 읽어야 상태가 바뀔 때 Compose가 이 블록을 다시 실행한다.
            update = { viewController ->
                val state = items.value.toUiKitTopBarState(highlightColor = highlightColor)
                // 검색어와 초점 요청은 글자마다 바뀌므로 항목을 다시 모으지 않고, 여기서 읽어 바뀔 때 이 블록만 다시 실행한다.
                state.search?.let { search -> search.text to search.focusRequestCount }
                viewController.view.alpha = alphaProvider().toDouble()
                renderer.update(viewController = viewController, state = state)
            },
            properties =
                UIKitInteropProperties(
                    interactionMode = UIKitInteropInteractionMode.NonCooperative,
                    isNativeAccessibilityEnabled = true,
                    // 유리 질감이 아래의 Compose 화면을 비추려면 SwiftUI 뷰가 Compose 화면 위에 놓여야 한다.
                    placedAsOverlay = true,
                ),
        )
        // 네이티브 뷰는 화면에 그려지는 순서와 상관없이 Compose 위에 놓이지만, Compose 안에서 누름을 받는 순서는 나중에 둔 요소가 먼저다.
        // 제목을 네이티브 뷰보다 뒤에 두어야 유리 면 밖을 누를 때 네이티브 뷰 자리가 아니라 제목이 누름을 받는다.
        UiKitTopBarTitleLayout(
            items = items,
            modifier = Modifier.fillMaxSize(),
            isTitleCentered = isTitleCentered,
        ) {
            uiKitNavigationIconScope.navigationIcon()
            uiKitTitleScope.title()
            uiKitActionScope.actions()
            UiKitTopBarComposeTitle(
                title = title,
                modifier = Modifier.layoutId(TITLE_LAYOUT_ID),
                isTitleCentered = isTitleCentered,
            )
        }
    }
}

@Composable
private fun UiKitTopBarComposeTitle(
    title: @Composable DiaryTopBarTitleScope.() -> Unit,
    modifier: Modifier = Modifier,
    isTitleCentered: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isTitleCentered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        ProvideContentColorTextStyle(contentColor = DiaryTheme.colorScheme.onSurface, textStyle = DiaryTheme.typography.titleLarge) {
            materialNonSearchTitleScope.title()
        }
        ProvideContentColorTextStyle(contentColor = DiaryTheme.colorScheme.onSurfaceVariant, textStyle = DiaryTheme.typography.labelMedium) {
            materialSubtitleScope.title()
        }
    }
}

@Composable
private fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides LocalTextStyle.current.merge(textStyle),
        content = content,
    )
}

private class UiKitTopBarItems {
    var value: List<UiKitTopBarItem> by mutableStateOf(emptyList())
}

private sealed interface UiKitTopBarItem {
    class Search(
        val search: UiKitTopBarSearch,
    ) : UiKitTopBarItem

    class NavigateUp(
        val button: UiKitTopBarButton,
    ) : UiKitTopBarItem

    class Action(
        val button: UiKitTopBarButton,
    ) : UiKitTopBarItem
}

private fun List<UiKitTopBarItem>.toUiKitTopBarState(highlightColor: UIColor): UiKitTopBarState =
    UiKitTopBarState(
        search = filterIsInstance<UiKitTopBarItem.Search>().firstOrNull()?.search,
        navigateUp = filterIsInstance<UiKitTopBarItem.NavigateUp>().firstOrNull()?.button,
        actions = filterIsInstance<UiKitTopBarItem.Action>().map { action -> action.button },
        highlightColor = highlightColor,
    )

// 슬롯에 무엇을 두었는지는 composition을 거쳐야 알 수 있다. 스코프가 부른 요소마다 크기 없는 노드를 하나씩 두고
// 그 parentData에 항목을 담으면, 측정할 때 받는 노드 순서가 슬롯에서 부른 순서와 같다.
// 모은 항목은 composition이 아니라 이 측정과 UIKitViewController의 update 블록에서만 읽으므로 측정과 composition이 서로를 다시 부르지 않는다.
// 제목이 피할 유리 면의 폭도 모은 항목으로 정해지므로 같은 측정에서 제목 자리를 정한다.
@Composable
private fun UiKitTopBarTitleLayout(
    items: UiKitTopBarItems,
    modifier: Modifier = Modifier,
    isTitleCentered: Boolean = false,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val itemMeasurables = measurables.filter { measurable -> measurable.parentData is UiKitTopBarItem }
        val itemPlaceables = itemMeasurables.map { measurable -> measurable.measure(Constraints.fixed(width = 0, height = 0)) }
        val collectedItems = itemMeasurables.map { measurable -> measurable.parentData as UiKitTopBarItem }
        items.value = collectedItems

        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val leadingInset = uiKitTopBarInset(buttonCount = collectedItems.count { item -> item is UiKitTopBarItem.NavigateUp })
        val trailingInset =
            uiKitTopBarInset(
                buttonCount = collectedItems.count { item -> item is UiKitTopBarItem.Action && item.button.isVisible },
            )
        val betweenWidth = (width - leadingInset - trailingInset).coerceAtLeast(0)
        val centeredWidth = (width - maxOf(leadingInset, trailingInset) * 2).coerceAtLeast(0)
        val titlePlaceable =
            measurables
                .firstOrNull { measurable -> measurable.layoutId == TITLE_LAYOUT_ID }
                ?.measure(Constraints(maxWidth = betweenWidth, maxHeight = height))

        layout(width = width, height = height) {
            itemPlaceables.forEach { placeable -> placeable.place(x = 0, y = 0) }
            if (titlePlaceable != null) {
                val x =
                    when {
                        !isTitleCentered -> leadingInset
                        titlePlaceable.width <= centeredWidth -> (width - titlePlaceable.width) / 2
                        else -> leadingInset + (betweenWidth - titlePlaceable.width) / 2
                    }
                titlePlaceable.placeRelative(x = x, y = (height - titlePlaceable.height) / 2)
            }
        }
    }
}

private fun Density.uiKitTopBarInset(buttonCount: Int): Int {
    val horizontalPadding = DiaryTopBarDefaults.IosHorizontalPadding.roundToPx()

    return if (buttonCount > 0) {
        horizontalPadding + (DiaryTopBarDefaults.IosButtonSize * buttonCount + DiaryTopBarDefaults.IosTitleSpacing).roundToPx()
    } else {
        horizontalPadding
    }
}

@Composable
private fun UiKitTopBarItemNode(
    item: UiKitTopBarItem,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier.then(UiKitTopBarItemElement(item = item)),
        measurePolicy = { _, constraints -> layout(width = constraints.minWidth, height = constraints.minHeight) {} },
    )
}

private class UiKitTopBarItemElement(
    private val item: UiKitTopBarItem,
) : ModifierNodeElement<UiKitTopBarItemModifierNode>() {
    override fun create(): UiKitTopBarItemModifierNode = UiKitTopBarItemModifierNode(item = item)

    override fun update(node: UiKitTopBarItemModifierNode) {
        node.item = item
    }

    override fun equals(other: Any?): Boolean = other is UiKitTopBarItemElement && other.item === item

    override fun hashCode(): Int = item.hashCode()
}

private class UiKitTopBarItemModifierNode(
    var item: UiKitTopBarItem,
) : Modifier.Node(),
    ParentDataModifierNode {
    override fun Density.modifyParentData(parentData: Any?): Any = item
}

private val uiKitTitleScope =
    DiaryTopBarTitleScope { title ->
        if (title is DiaryTopBarTitle.Search) {
            UiKitTopBarSearchItem(title = title)
        }
    }

private val uiKitNavigationIconScope =
    DiaryTopBarNavigationIconScope { navigateUp ->
        UiKitTopBarItemNode(item = UiKitTopBarItem.NavigateUp(button = rememberUiKitTopBarNavigateUpButton(navigateUp = navigateUp)))
    }

private val uiKitActionScope =
    DiaryTopBarActionScope { action ->
        UiKitTopBarItemNode(item = UiKitTopBarItem.Action(button = rememberUiKitTopBarButton(action = action)))
    }

private class SearchFocusRequestCount {
    var value by mutableIntStateOf(0)
}

// 공유 화면은 FocusRequester로 검색 입력에 초점을 요청하지만 실제 입력은 SwiftUI에 있다.
// 검색 항목의 노드가 초점 자리가 되어 요청을 받아 SwiftUI에 넘기고, 다음 요청도 받을 수 있게 바로 초점을 놓는다.
@Composable
private fun UiKitTopBarSearchItem(title: DiaryTopBarTitle.Search) {
    val focusRequestCount = remember(title.focusRequester) { SearchFocusRequestCount() }
    val focusManager = LocalFocusManager.current

    UiKitTopBarItemNode(
        item = UiKitTopBarItem.Search(search = rememberUiKitTopBarSearch(title = title, focusRequestCount = focusRequestCount)),
        modifier =
            Modifier
                .focusRequester(title.focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        focusRequestCount.value++
                        focusManager.clearFocus()
                    }
                }.focusable(),
    )
}

@Composable
private fun rememberUiKitTopBarNavigateUpButton(navigateUp: DiaryTopBarNavigateUp): UiKitTopBarButton =
    UiKitTopBarButton(
        id = "navigateUp",
        image = rememberTemplateUIImage(imageVector = DiaryIcons.ArrowBack, size = DiaryTopBarDefaults.IosIconSize),
        text = null,
        contentDescription = navigateUp.contentDescription.orEmpty(),
        stateDescription = null,
        isEnabled = true,
        isInProgress = false,
        isHighlighted = false,
        isToggle = false,
        isVisible = true,
        onClick = navigateUp.onClick,
    )

@Composable
private fun rememberUiKitTopBarButton(action: DiaryTopBarAction): UiKitTopBarButton =
    when (action) {
        is DiaryTopBarAction.Icon ->
            UiKitTopBarButton(
                id = action.icon.buttonKey,
                image = rememberTemplateUIImage(imageVector = action.icon.imageVector, size = DiaryTopBarDefaults.IosIconSize),
                text = null,
                contentDescription = action.contentDescription,
                stateDescription = null,
                isEnabled = action.isEnabled,
                isInProgress = action.isInProgress,
                isHighlighted = false,
                isToggle = false,
                isVisible = action.isVisible,
                onClick = action.onClick,
            )

        is DiaryTopBarAction.Toggle ->
            UiKitTopBarButton(
                id = action.icon.buttonKey,
                image = rememberTemplateUIImage(imageVector = action.icon.imageVector, size = DiaryTopBarDefaults.IosIconSize),
                text = null,
                contentDescription = action.contentDescription,
                stateDescription = null,
                isEnabled = true,
                isInProgress = action.isInProgress,
                isHighlighted = action.isChecked,
                isToggle = true,
                isVisible = true,
                onClick = action.onClick,
            )

        is DiaryTopBarAction.Applied ->
            UiKitTopBarButton(
                id = action.icon.buttonKey,
                image = rememberTemplateUIImage(imageVector = action.icon.imageVector, size = DiaryTopBarDefaults.IosIconSize),
                text = null,
                contentDescription = action.contentDescription,
                stateDescription = action.appliedStateDescription.takeIf { action.isApplied },
                isEnabled = true,
                isInProgress = false,
                isHighlighted = action.isApplied,
                isToggle = false,
                isVisible = true,
                onClick = action.onClick,
            )

        is DiaryTopBarAction.Text ->
            UiKitTopBarButton(
                id = TEXT_BUTTON_ID,
                image = null,
                text = action.text,
                contentDescription = action.contentDescription,
                stateDescription = null,
                isEnabled = true,
                isInProgress = false,
                isHighlighted = false,
                isToggle = false,
                isVisible = true,
                onClick = action.onClick,
            )
    }

@Composable
private fun rememberUiKitTopBarSearch(
    title: DiaryTopBarTitle.Search,
    focusRequestCount: SearchFocusRequestCount,
): UiKitTopBarSearch =
    UiKitTopBarSearch(
        placeholder = title.placeholder,
        searchImage = rememberTemplateUIImage(imageVector = DiaryIcons.Search, size = DiaryTopBarDefaults.IosIconSize),
        clearImage = rememberTemplateUIImage(imageVector = DiaryIcons.Close, size = DiaryTopBarDefaults.IosIconSize),
        clearAccessibilityLabel = stringResource(Res.string.clear_text_field_button_content_description),
        textProvider = { title.state.text.toString() },
        focusRequestCountProvider = { focusRequestCount.value },
        onTextChange = { text ->
            if (text != title.state.text.toString()) {
                title.state.setTextAndPlaceCursorAtEnd(text)
            }
        },
    )

private const val TEXT_BUTTON_ID = "text"
private const val TITLE_LAYOUT_ID = "title"
