package io.github.taetae98coding.diary.compose.memo

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

public const val MEMO_DATE_HEADER_TEST_TAG: String = "MemoDateHeader"

@Composable
public fun MemoDateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
    state: MemoListState = rememberMemoListState(),
) {
    val today = state.today

    Surface(modifier = modifier) {
        Text(
            text = if (date == today) stringResource(Res.string.memo_list_today_header) else date.toDisplayText(),
            modifier =
                Modifier
                    .padding(vertical = 4.dp)
                    .testTag(MEMO_DATE_HEADER_TEST_TAG),
            color = DiaryTheme.colorScheme.primary,
            style = DiaryTheme.typography.titleSmallEmphasized,
        )
    }
}

private class MemoDateHeaderPreviewParameter : PreviewParameterProvider<LocalDate> {
    override val values: Sequence<LocalDate> =
        sequenceOf(
            LocalDate(year = 2026, month = 7, day = 19),
            LocalDate(year = 2026, month = 7, day = 28),
        )
}

@ComponentPreview
@Composable
private fun MemoDateHeaderPreview(
    @PreviewParameter(MemoDateHeaderPreviewParameter::class) date: LocalDate,
) {
    DiaryTheme {
        MemoDateHeader(
            date = date,
            state = rememberMemoListState(initialToday = LocalDate(year = 2026, month = 7, day = 28)),
        )
    }
}
