package io.github.taetae98coding.diary.compose.core.sort

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.core.model.list.ListSort

public class ListSortPreviewParameter : PreviewParameterProvider<ListSort> {
    override val values: Sequence<ListSort> = memoListSortList.asSequence()
}
