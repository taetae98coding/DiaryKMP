@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.detail.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScaffoldEvent
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_share_calendar_file
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_share_sheet_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_share_text
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoDetailShareBottomSheetContent(
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.memo_detail_share_sheet_title),
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetContent)) {
            ShareFormatRow(
                text = stringResource(Res.string.memo_detail_share_text),
                onClick = { onEvent(MemoDetailScaffoldEvent.ClickShareText) },
                modifier = Modifier.fillMaxWidth(),
            )
            ShareFormatRow(
                text = stringResource(Res.string.memo_detail_share_calendar_file),
                onClick = { onEvent(MemoDetailScaffoldEvent.ClickShareCalendarFile) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ShareFormatRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable(role = Role.Button, onClick = onClick)
                .styleable(style = DiaryTheme.styles.bottomSheetRow),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = DiaryTheme.typography.bodyLarge,
        )
    }
}

@ScreenPreview
@Composable
private fun MemoDetailShareBottomSheetContentPreview() {
    DiaryTheme {
        MemoDetailShareBottomSheetContent(onEvent = {})
    }
}
