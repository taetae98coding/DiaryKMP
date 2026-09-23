package io.github.taetae98coding.diary.feature.memo.ui.detail

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel

internal fun handleMemoDetailEvent(
    event: MemoDetailScaffoldEvent,
    detailViewModel: MemoDetailViewModel,
    geminiViewModel: MemoGeminiViewModel,
    scaffoldState: MemoFormState,
    navigateUp: () -> Unit,
) {
    when (event) {
        is MemoDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is MemoDetailScaffoldEvent.ClickUpdate -> detailViewModel.update(detail = scaffoldState.detail)
        is MemoDetailScaffoldEvent.ClickFinish -> detailViewModel.finish()
        is MemoDetailScaffoldEvent.ClickRestart -> detailViewModel.restart()
        is MemoDetailScaffoldEvent.ClickCopy -> detailViewModel.copy()
        is MemoDetailScaffoldEvent.ClickDelete -> detailViewModel.delete()
        is MemoDetailScaffoldEvent.ClickGemini -> geminiViewModel.open()
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
    navigateToPlaceAdd: (Coordinate?) -> Unit,
) {
    when (event) {
        is MemoPlacePickerEvent.ClickAdd -> navigateToPlaceAdd(event.coordinate)
        is MemoPlacePickerEvent.Select -> placeViewModel.selectPlace(placeId = event.id)
        is MemoPlacePickerEvent.Unselect -> placeViewModel.unselectPlace(placeId = event.id)
        is MemoPlacePickerEvent.ChangeQuery -> placeViewModel.updateQuery(query = event.query)
    }
}
