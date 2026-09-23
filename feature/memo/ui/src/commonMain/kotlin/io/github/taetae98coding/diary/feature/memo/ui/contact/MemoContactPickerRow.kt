package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerRow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.memo.ui.previewContact

@Composable
internal fun MemoContactPickerRow(
    onEvent: (MemoContactPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    contact: Contact? = null,
    isSelected: Boolean = false,
) {
    DiaryPickerRow(
        isSelected = isSelected,
        onSelectedChange = {
            if (isSelected) {
                contact?.let { value -> onEvent(MemoContactPickerEvent.Unselect(id = value.id)) }
            } else {
                contact?.let { value -> onEvent(MemoContactPickerEvent.Select(id = value.id)) }
            }
        },
        label = contact?.detail?.name.orEmpty(),
        modifier = modifier,
        // 같은 이름의 연락처를 가릴 수 있도록 첫 번째 전화번호를 함께 둔다. 전화번호가 없어도 줄은 비워 둔 채 자리를 지킨다.
        description =
            contact
                ?.detail
                ?.phoneNumberList
                ?.firstOrNull()
                ?.number
                .orEmpty(),
        enabled = contact != null,
    )
}

@ComponentPreview
@Composable
private fun MemoContactPickerRowPreview() {
    val contact = remember { previewContact(name = "김철수", phoneNumber = "010-1234-5678") }

    DiaryTheme {
        Surface {
            MemoContactPickerRow(
                onEvent = {},
                contact = contact,
                isSelected = true,
            )
        }
    }
}
