package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

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
