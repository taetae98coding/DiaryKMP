package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.isSubmitShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut

@Composable
internal fun Modifier.contactDetailTabShortcut(
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    state: ContactDetailTabState = rememberContactDetailTabState(),
    isUpdateEnabledProvider: () -> Boolean = { false },
): Modifier =
    keyShortcut { keyEvent ->
        when (state.tab) {
            ContactDetailTab.DETAIL -> {
                if (isUpdateEnabledProvider() && keyEvent.isSubmitShortcut()) {
                    onUpdate()
                    true
                } else {
                    false
                }
            }

            ContactDetailTab.MEMO -> {
                if (keyEvent.isAddShortcut()) {
                    onMemoAdd()
                    true
                } else {
                    false
                }
            }
        }
    }
