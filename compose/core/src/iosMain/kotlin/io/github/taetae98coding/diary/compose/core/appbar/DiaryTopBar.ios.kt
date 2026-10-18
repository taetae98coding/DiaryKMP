@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.clear_text_field_button_content_description
import io.github.taetae98coding.diary.compose.core.graphics.rememberTemplateUIImage
import io.github.taetae98coding.diary.compose.core.graphics.toUIColor
import io.github.taetae98coding.diary.compose.core.icon.ArrowBack
import io.github.taetae98coding.diary.compose.core.icon.ArrowDropDown
import io.github.taetae98coding.diary.compose.core.icon.ArrowDropUp
import io.github.taetae98coding.diary.compose.core.icon.Close
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.Search
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
public actual fun DiaryTopBar(
    modifier: Modifier,
    title: DiaryTopBarTitle?,
    navigateUp: DiaryTopBarNavigateUp?,
    actions: List<DiaryTopBarAction>,
    isTitleCentered: Boolean,
) {
    val renderer = UiKitTopBarRendererRegistry.renderer

    if (renderer == null) {
        MaterialDiaryTopBar(
            modifier = modifier,
            title = title,
            navigateUp = navigateUp,
            actions = actions,
            isTitleCentered = isTitleCentered,
        )
        return
    }

    val focusRequestCount = rememberSearchFocusRequestCount(title = title)
    val state =
        rememberUiKitTopBarState(
            title = title,
            navigateUp = navigateUp,
            actions = actions,
            focusRequestCount = focusRequestCount,
        )
    val currentState by rememberUpdatedState(state)
    val alphaProvider = LocalDiaryTopBarAlpha.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .height(DiaryTopBarDefaults.IosBarHeight),
    ) {
        if (title is DiaryTopBarTitle.Search) {
            SearchFocusForwarder(title = title, onFocusRequest = { focusRequestCount.value++ })
        }
        UIKitViewController(
            factory = renderer::createViewController,
            modifier = Modifier.fillMaxSize(),
            // 상태를 이 블록 안에서 읽어야 상태가 바뀔 때 Compose가 이 블록을 다시 실행한다.
            update = { viewController ->
                val state = currentState
                // 검색어와 초점 요청은 글자마다 바뀌므로 상단 바를 다시 구성하지 않고, 여기서 읽어 바뀔 때 이 블록만 다시 실행한다.
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
    }
}

private class SearchFocusRequestCount {
    var value by mutableIntStateOf(0)
}

@Composable
private fun rememberSearchFocusRequestCount(title: DiaryTopBarTitle?): SearchFocusRequestCount = remember((title as? DiaryTopBarTitle.Search)?.focusRequester) { SearchFocusRequestCount() }

// 공유 화면은 FocusRequester로 검색 입력에 초점을 요청하지만 실제 입력은 SwiftUI에 있다.
// 보이지 않는 초점 자리가 요청을 받아 SwiftUI에 넘기고, 다음 요청도 받을 수 있게 바로 초점을 놓는다.
@Composable
private fun SearchFocusForwarder(
    title: DiaryTopBarTitle.Search,
    onFocusRequest: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier =
            Modifier
                .focusRequester(title.focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onFocusRequest()
                        focusManager.clearFocus()
                    }
                }.focusable(),
    )
}

@Composable
private fun rememberUiKitTopBarState(
    title: DiaryTopBarTitle?,
    navigateUp: DiaryTopBarNavigateUp?,
    actions: List<DiaryTopBarAction>,
    focusRequestCount: SearchFocusRequestCount,
): UiKitTopBarState {
    val highlightColor = DiaryTheme.colorScheme.primary
    val navigateUpImage = rememberTemplateUIImage(imageVector = DiaryIcons.ArrowBack, size = DiaryTopBarDefaults.IosIconSize)
    val actionButtons =
        actions.map { action ->
            // 앞쪽 버튼이 생기거나 빠져도 뒤 버튼이 그린 아이콘 이미지를 그대로 쓰도록 위치가 아니라 아이콘으로 구분한다.
            key(action.compositionKey) { rememberUiKitTopBarButton(action = action) }
        }

    return UiKitTopBarState(
        title = (title as? DiaryTopBarTitle.Text)?.text ?: (title as? DiaryTopBarTitle.Button)?.text.orEmpty(),
        subtitle = (title as? DiaryTopBarTitle.Text)?.subtitle,
        titleButton = (title as? DiaryTopBarTitle.Button)?.let { button -> rememberUiKitTopBarTitleButton(title = button) },
        search = (title as? DiaryTopBarTitle.Search)?.let { search -> rememberUiKitTopBarSearch(title = search, focusRequestCount = focusRequestCount) },
        navigateUp =
            navigateUp?.let {
                UiKitTopBarButton(
                    id = "navigateUp",
                    image = navigateUpImage,
                    text = null,
                    contentDescription = it.contentDescription.orEmpty(),
                    stateDescription = null,
                    isEnabled = true,
                    isInProgress = false,
                    isHighlighted = false,
                    isToggle = false,
                    isVisible = true,
                    onClick = it.onClick,
                )
            },
        actions = actionButtons,
        highlightColor = highlightColor.toUIColor(),
    )
}

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
private fun rememberUiKitTopBarTitleButton(title: DiaryTopBarTitle.Button): UiKitTopBarTitleButton {
    val expandImage = rememberTemplateUIImage(imageVector = DiaryIcons.ArrowDropDown, size = DiaryTopBarDefaults.IosIconSize)
    val collapseImage = rememberTemplateUIImage(imageVector = DiaryIcons.ArrowDropUp, size = DiaryTopBarDefaults.IosIconSize)

    return UiKitTopBarTitleButton(
        image = if (title.isExpanded) collapseImage else expandImage,
        hintDescription = if (title.isExpanded) title.collapseContentDescription else title.expandContentDescription,
        onClick = title.onClick,
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

private val DiaryTopBarAction.compositionKey: String
    get() =
        when (this) {
            is DiaryTopBarAction.Icon -> icon.buttonKey
            is DiaryTopBarAction.Toggle -> icon.buttonKey
            is DiaryTopBarAction.Applied -> icon.buttonKey
            is DiaryTopBarAction.Text -> TEXT_BUTTON_ID
        }
