package io.github.taetae98coding.diary.feature.memo.ui.detail

import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.MemoDetailSharer
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.memoShareCalendarFileOf
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.memoShareTextOf
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

internal fun handleMemoDetailEvent(
    event: MemoDetailScaffoldEvent,
    detailViewModel: MemoDetailViewModel,
    geminiViewModel: MemoGeminiViewModel,
    scaffoldState: MemoFormState,
    navigateUp: () -> Unit,
    onShareEvent: (MemoDetailScaffoldEvent.Share) -> Unit,
) {
    when (event) {
        is MemoDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is MemoDetailScaffoldEvent.ClickUpdate -> detailViewModel.update(detail = scaffoldState.detail)
        is MemoDetailScaffoldEvent.ClickFinish -> detailViewModel.finish()
        is MemoDetailScaffoldEvent.ClickRestart -> detailViewModel.restart()
        is MemoDetailScaffoldEvent.ClickCopy -> detailViewModel.copy()
        is MemoDetailScaffoldEvent.ClickDelete -> detailViewModel.delete()
        is MemoDetailScaffoldEvent.ClickGemini -> geminiViewModel.open()
        is MemoDetailScaffoldEvent.Share -> onShareEvent(event)
    }
}

internal fun handleMemoDetailShareEvent(
    event: MemoDetailScaffoldEvent.Share,
    content: MemoDetailUiState.Content?,
    scaffoldState: MemoFormState,
    periodText: String,
    sharer: MemoDetailSharer,
    shareSheetState: DialogState,
    coroutineScope: CoroutineScope,
) {
    val savedTitle = content?.detail?.title
    val detail = scaffoldState.detail

    fun share(shareContent: ShareContent) {
        // 웹은 사용자 조작 직후에만 공유 시트를 열 수 있으므로 다음 프레임을 기다리지 않고 바로 부른다.
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) { sharer.share(content = shareContent) }
    }

    when (event) {
        is MemoDetailScaffoldEvent.ClickShare -> {
            when {
                savedTitle == null -> Unit
                detail.dateTime != null && sharer.support.isCalendarFileSupported -> shareSheetState.show()
                else -> share(memoShareTextOf(detail = detail, savedTitle = savedTitle, periodText = periodText))
            }
        }

        is MemoDetailScaffoldEvent.ClickShareText -> {
            shareSheetState.hide()
            if (savedTitle != null) share(memoShareTextOf(detail = detail, savedTitle = savedTitle, periodText = periodText))
        }

        is MemoDetailScaffoldEvent.ClickShareCalendarFile -> {
            shareSheetState.hide()
            val dateTime = detail.dateTime
            if (content != null && dateTime != null) {
                share(
                    memoShareCalendarFileOf(
                        id = content.id,
                        detail = detail,
                        dateTime = dateTime,
                        savedTitle = content.detail.title,
                        timeZone = TimeZone.currentSystemDefault(),
                        sharedAt = Clock.System.now(),
                    ),
                )
            }
        }

        is MemoDetailScaffoldEvent.DismissShareSheet -> shareSheetState.hide()
    }
}
