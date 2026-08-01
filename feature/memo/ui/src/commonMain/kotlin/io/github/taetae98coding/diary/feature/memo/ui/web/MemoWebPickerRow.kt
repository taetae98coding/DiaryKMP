package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerRow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.previewWeb

@Composable
internal fun MemoWebPickerRow(
    onEvent: (MemoWebPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    web: Web? = null,
    isSelected: Boolean = false,
) {
    DiaryPickerRow(
        isSelected = isSelected,
        onSelectedChange = {
            if (isSelected) {
                web?.let { value -> onEvent(MemoWebPickerEvent.Unselect(id = value.id)) }
            } else {
                web?.let { value -> onEvent(MemoWebPickerEvent.Select(id = value.id)) }
            }
        },
        label = web?.detail?.title.orEmpty(),
        modifier = modifier,
        description = web?.detail?.url.orEmpty(),
        enabled = web != null,
    )
}

@ComponentPreview
@Composable
private fun MemoWebPickerRowPreview() {
    val web = remember { previewWeb(title = "사내 위키", url = "https://wiki.example.com") }

    DiaryTheme {
        Surface {
            MemoWebPickerRow(
                onEvent = {},
                web = web,
                isSelected = true,
            )
        }
    }
}
