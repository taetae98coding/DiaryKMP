package io.github.taetae98coding.diary.feature.memo.ui.detail

import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.MemoDetailSharer
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.memoShareCalendarFileOf
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.memoShareTextOf
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
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

internal fun handleMemoDetailTagPickerEvent(
    event: MemoTagPickerEvent,
    tagViewModel: MemoTagViewModel,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is MemoTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is MemoTagPickerEvent.Select -> tagViewModel.selectTag(tagId = event.id)
        is MemoTagPickerEvent.Unselect -> tagViewModel.unselectTag(tagId = event.id)
        is MemoTagPickerEvent.SelectPrimary -> tagViewModel.selectPrimaryTag(tagId = event.id)
        is MemoTagPickerEvent.UnselectPrimary -> tagViewModel.unselectPrimaryTag()
        is MemoTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}

internal fun handleMemoDetailContactPickerEvent(
    event: MemoContactPickerEvent,
    contactViewModel: MemoContactViewModel,
    navigateToContactAdd: () -> Unit,
) {
    when (event) {
        is MemoContactPickerEvent.ClickAdd -> navigateToContactAdd()
        is MemoContactPickerEvent.Select -> contactViewModel.selectContact(contactId = event.id)
        is MemoContactPickerEvent.Unselect -> contactViewModel.unselectContact(contactId = event.id)
        is MemoContactPickerEvent.ChangeQuery -> contactViewModel.updateQuery(query = event.query)
    }
}

internal fun handleMemoDetailWebPickerEvent(
    event: MemoWebPickerEvent,
    webViewModel: MemoWebViewModel,
    navigateToWebAdd: () -> Unit,
) {
    when (event) {
        is MemoWebPickerEvent.ClickAdd -> navigateToWebAdd()
        is MemoWebPickerEvent.Select -> webViewModel.selectWeb(webId = event.id)
        is MemoWebPickerEvent.Unselect -> webViewModel.unselectWeb(webId = event.id)
        is MemoWebPickerEvent.ChangeQuery -> webViewModel.updateQuery(query = event.query)
    }
}

internal fun handleMemoDetailPlacePickerEvent(
    event: MemoPlacePickerEvent,
    placeViewModel: MemoPlaceViewModel,
    placePickerMapViewModel: MemoPlacePickerMapViewModel,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
) {
    when (event) {
        is MemoPlacePickerEvent.ClickAdd -> navigateToPlaceAdd(event.coordinate)
        is MemoPlacePickerEvent.Select -> placeViewModel.selectPlace(placeId = event.id)
        is MemoPlacePickerEvent.Unselect -> placeViewModel.unselectPlace(placeId = event.id)
        is MemoPlacePickerEvent.ChangeQuery -> placeViewModel.updateQuery(query = event.query)
        is MemoPlacePickerEvent.MoveMap -> placePickerMapViewModel.updateVisibleBounds(bounds = event.bounds)
    }
}
