package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.chip.DiaryAssistChip
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_detail_action
import io.github.taetae98coding.diary.feature.memo.ui.previewContact
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoContactChip(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailActionLabel = stringResource(Res.string.memo_contact_detail_action)

    DiaryAssistChip(
        onClick = onClick,
        label = contact.detail.name,
        modifier =
            modifier.semantics {
                // 칩의 클릭 동작은 유지하고 이름만 덧붙이도록 action을 비워 둔다.
                onClick(label = detailActionLabel, action = null)
            },
    )
}

@ComponentPreview
@Composable
private fun MemoContactChipPreview() {
    val contact = remember { previewContact(name = "김철수", phoneNumber = "010-1234-5678") }

    DiaryTheme {
        Surface {
            MemoContactChip(
                contact = contact,
                onClick = {},
            )
        }
    }
}
