package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryPickerDialogHost(
    dialogState: DialogState,
    onQueryChange: (String) -> Unit,
    content: @Composable (searchFieldState: DiaryPickerSearchFieldState, hide: () -> Unit) -> Unit,
) {
    if (!dialogState.isVisible) return

    val searchFieldState = rememberDiaryPickerSearchFieldState()

    val hide = {
        onQueryChange("")
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = searchFieldState.textFieldState,
        onQueryChange = onQueryChange,
    )

    content(searchFieldState, hide)
}

@ScreenPreview
@Composable
private fun DiaryPickerDialogHostPreview() {
    DiaryTheme {
        DiaryPickerDialogHost(
            dialogState = rememberDialogState(initialVisible = true),
            onQueryChange = {},
        ) { searchFieldState, hide ->
            DiaryPickerDialog(
                title = "태그 선택",
                onDismissRequest = hide,
            ) {
                DiaryPickerSearchField(
                    placeholder = "태그 검색",
                    state = searchFieldState,
                )
                Text(text = "선택할 항목이 들어간다.")
            }
        }
    }
}
