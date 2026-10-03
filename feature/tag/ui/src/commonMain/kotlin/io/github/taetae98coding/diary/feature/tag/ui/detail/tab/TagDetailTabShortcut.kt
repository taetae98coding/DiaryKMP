package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.isSubmitShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import kotlinx.coroutines.flow.filter

@Composable
internal fun Modifier.tagDetailTabShortcut(
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    onWebAdd: () -> Unit,
    onPlaceAdd: () -> Unit,
    state: TagDetailTabState = rememberTagDetailTabState(),
    isUpdateEnabledProvider: () -> Boolean = { false },
): Modifier {
    val addTabFocusRequester = remember { FocusRequester() }

    // 목록과 함께 표시하면 키 입력은 초점이 있는 영역에만 전달되므로, 추가 탭을 고르면 목록 영역이 가져간 초점을 되찾는다.
    LaunchedEffect(state, addTabFocusRequester) {
        snapshotFlow { state.tab }
            .filter { tab -> tab != TagDetailTab.DETAIL }
            .collect { addTabFocusRequester.requestFocus() }
    }

    return focusRequester(addTabFocusRequester)
        .keyShortcut { keyEvent ->
            when (state.tab) {
                TagDetailTab.DETAIL -> {
                    if (isUpdateEnabledProvider() && keyEvent.isSubmitShortcut()) {
                        onUpdate()
                        true
                    } else {
                        false
                    }
                }

                TagDetailTab.MEMO -> keyEvent.handleAddShortcut(onAdd = onMemoAdd)

                TagDetailTab.WEB -> keyEvent.handleAddShortcut(onAdd = onWebAdd)

                TagDetailTab.PLACE -> keyEvent.handleAddShortcut(onAdd = onPlaceAdd)
            }
        }
}

private fun KeyEvent.handleAddShortcut(onAdd: () -> Unit): Boolean =
    if (isAddShortcut()) {
        onAdd()
        true
    } else {
        false
    }
