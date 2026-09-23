package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.animation.animateBounds
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.chip.DiaryAddChip
import io.github.taetae98coding.diary.compose.core.layout.DiaryChipFlexBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_select_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_select_label
import io.github.taetae98coding.diary.feature.memo.ui.previewContact
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoContactFlexBox(
    onContactClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
) {
    val uiState = uiStateProvider()

    DiaryChipFlexBox(modifier = modifier) {
        uiState.selectedContactList.forEach { contact ->
            key(contact.id) {
                MemoContactChip(
                    contact = contact,
                    onClick = { onContactClick(contact.id) },
                    modifier = Modifier.animateBounds(lookaheadScope = this),
                )
            }
        }
        DiaryAddChip(
            onClick = onAddClick,
            label = stringResource(Res.string.memo_contact_select_label),
            actionLabel = stringResource(Res.string.memo_contact_select_action),
            modifier = Modifier.animateBounds(lookaheadScope = this),
        )
    }
}

@ComponentPreview
@Composable
private fun MemoContactFlexBoxPreview() {
    val contactList = remember { listOf(previewContact(name = "김철수", phoneNumber = "010-1234-5678")) }

    DiaryTheme {
        Surface {
            MemoContactFlexBox(
                onContactClick = {},
                onAddClick = {},
                uiStateProvider = { MemoContactInputUiState(selectedContactList = contactList) },
            )
        }
    }
}
