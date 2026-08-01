package io.github.taetae98coding.diary.feature.memo.ui.add

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebPickerEvent

internal fun handleMemoAddEvent(
    event: MemoAddScaffoldEvent,
    addViewModel: MemoAddViewModel,
    tagViewModel: MemoAddTagViewModel,
    webViewModel: MemoAddWebViewModel,
    placeViewModel: MemoAddPlaceViewModel,
    scaffoldState: MemoFormState,
    navigateUp: () -> Unit,
) {
    when (event) {
        is MemoAddScaffoldEvent.ClickNavigateUp -> navigateUp()

        is MemoAddScaffoldEvent.ClickAdd ->
            addViewModel.add(
                detail = scaffoldState.detail,
                tagSelection = tagViewModel.selection.value,
                webIdSet = webViewModel.webIdSet.value,
                placeIdSet = placeViewModel.placeIdSet.value,
            )
    }
}

internal fun handleMemoAddTagPickerEvent(
    event: MemoTagPickerEvent,
    tagViewModel: MemoAddTagViewModel,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is MemoTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is MemoTagPickerEvent.Select -> tagViewModel.selectTag(id = event.id)
        is MemoTagPickerEvent.Unselect -> tagViewModel.unselectTag(id = event.id)
        is MemoTagPickerEvent.SelectPrimary -> tagViewModel.selectPrimaryTag(id = event.id)
        is MemoTagPickerEvent.UnselectPrimary -> tagViewModel.unselectPrimaryTag()
        is MemoTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}

internal fun handleMemoAddWebPickerEvent(
    event: MemoWebPickerEvent,
    webViewModel: MemoAddWebViewModel,
    navigateToWebAdd: () -> Unit,
) {
    when (event) {
        is MemoWebPickerEvent.ClickAdd -> navigateToWebAdd()
        is MemoWebPickerEvent.Select -> webViewModel.selectWeb(id = event.id)
        is MemoWebPickerEvent.Unselect -> webViewModel.unselectWeb(id = event.id)
        is MemoWebPickerEvent.ChangeQuery -> webViewModel.updateQuery(query = event.query)
    }
}

internal fun handleMemoAddPlacePickerEvent(
    event: MemoPlacePickerEvent,
    placeViewModel: MemoAddPlaceViewModel,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
) {
    when (event) {
        is MemoPlacePickerEvent.ClickAdd -> navigateToPlaceAdd(event.coordinate)
        is MemoPlacePickerEvent.Select -> placeViewModel.selectPlace(id = event.id)
        is MemoPlacePickerEvent.Unselect -> placeViewModel.unselectPlace(id = event.id)
        is MemoPlacePickerEvent.ChangeQuery -> placeViewModel.updateQuery(query = event.query)
    }
}
