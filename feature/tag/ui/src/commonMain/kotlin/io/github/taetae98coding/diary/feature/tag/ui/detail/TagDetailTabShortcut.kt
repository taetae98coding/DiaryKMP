package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.isSubmitShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut

@Composable
internal fun Modifier.tagDetailTabShortcut(
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    onWebAdd: () -> Unit,
    onPlaceAdd: () -> Unit,
    state: TagDetailTabState = rememberTagDetailTabState(),
    isUpdateEnabledProvider: () -> Boolean = { false },
): Modifier =
    keyShortcut { keyEvent ->
        when (state.tab) {
            TagDetailTab.DETAIL -> {
                if (isUpdateEnabledProvider() && keyEvent.isSubmitShortcut()) {
                    onUpdate()
                    true
                } else {
                    false
                }
            }

            // 세 목록 탭은 같은 추가 단축키를 쓰고 선택한 탭이 무엇을 추가할지 정한다.
            TagDetailTab.MEMO -> keyEvent.handleAddShortcut(onAdd = onMemoAdd)

            TagDetailTab.WEB -> keyEvent.handleAddShortcut(onAdd = onWebAdd)

            TagDetailTab.PLACE -> keyEvent.handleAddShortcut(onAdd = onPlaceAdd)
        }
    }

private fun KeyEvent.handleAddShortcut(onAdd: () -> Unit): Boolean =
    if (isAddShortcut()) {
        onAdd()
        true
    } else {
        false
    }
