package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.focus.FocusRequester

@DslMarker
public annotation class DiaryTopBarDsl

// iOS 상단 바는 SwiftUI가 그리므로 슬롯에 임의의 Composable을 받지 않고, 스코프가 주는 요소만 둘 수 있게 한다.
// 같은 호출을 Material은 그 자리에 그리고, iOS는 SwiftUI에 넘길 항목으로 모은다.
@DiaryTopBarDsl
@Stable
public class DiaryTopBarTitleScope internal constructor(
    private val content: @Composable (DiaryTopBarTitle) -> Unit,
) {
    @Composable
    public fun Title(
        text: String,
        subtitle: String? = null,
        isMarquee: Boolean = false,
    ) {
        content(DiaryTopBarTitle.Text(text = text, subtitle = subtitle, isMarquee = isMarquee))
    }

    @Composable
    public fun TitleButton(
        text: String,
        isExpanded: Boolean,
        expandContentDescription: String,
        collapseContentDescription: String,
        onClick: () -> Unit,
    ) {
        content(
            DiaryTopBarTitle.Button(
                text = text,
                isExpanded = isExpanded,
                expandContentDescription = expandContentDescription,
                collapseContentDescription = collapseContentDescription,
                onClick = onClick,
            ),
        )
    }

    @Composable
    public fun SearchTitle(
        state: TextFieldState,
        placeholder: String,
        focusRequester: FocusRequester,
    ) {
        content(DiaryTopBarTitle.Search(state = state, placeholder = placeholder, focusRequester = focusRequester))
    }
}

@DiaryTopBarDsl
@Stable
public class DiaryTopBarNavigationIconScope internal constructor(
    private val content: @Composable (DiaryTopBarNavigateUp) -> Unit,
) {
    @Composable
    public fun NavigateUp(
        onClick: () -> Unit,
        contentDescription: String? = null,
    ) {
        content(DiaryTopBarNavigateUp(contentDescription = contentDescription, onClick = onClick))
    }
}

@DiaryTopBarDsl
@Stable
public class DiaryTopBarActionScope internal constructor(
    private val content: @Composable (DiaryTopBarAction) -> Unit,
) {
    // isVisible을 false로 바꾸면 크기·투명도 전환을 거쳐 사라지고, 호출하지 않으면 전환 없이 곧바로 사라진다.
    @Composable
    public fun IconAction(
        icon: DiaryTopBarIcon,
        contentDescription: String,
        onClick: () -> Unit,
        isEnabled: Boolean = true,
        isInProgress: Boolean = false,
        isVisible: Boolean = true,
    ) {
        content(
            DiaryTopBarAction.Icon(
                icon = icon,
                contentDescription = contentDescription,
                onClick = onClick,
                isEnabled = isEnabled,
                isInProgress = isInProgress,
                isVisible = isVisible,
            ),
        )
    }

    @Composable
    public fun ToggleAction(
        icon: DiaryTopBarIcon,
        contentDescription: String,
        isChecked: Boolean,
        onClick: () -> Unit,
        isInProgress: Boolean = false,
    ) {
        content(
            DiaryTopBarAction.Toggle(
                icon = icon,
                contentDescription = contentDescription,
                isChecked = isChecked,
                onClick = onClick,
                isInProgress = isInProgress,
            ),
        )
    }

    @Composable
    public fun AppliedAction(
        icon: DiaryTopBarIcon,
        contentDescription: String,
        isApplied: Boolean,
        appliedStateDescription: String,
        onClick: () -> Unit,
    ) {
        content(
            DiaryTopBarAction.Applied(
                icon = icon,
                contentDescription = contentDescription,
                isApplied = isApplied,
                appliedStateDescription = appliedStateDescription,
                onClick = onClick,
            ),
        )
    }

    @Composable
    public fun TextAction(
        text: String,
        contentDescription: String,
        onClick: () -> Unit,
    ) {
        content(DiaryTopBarAction.Text(text = text, contentDescription = contentDescription, onClick = onClick))
    }
}
