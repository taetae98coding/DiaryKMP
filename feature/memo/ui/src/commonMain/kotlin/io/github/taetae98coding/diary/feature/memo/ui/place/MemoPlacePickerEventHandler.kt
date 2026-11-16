package io.github.taetae98coding.diary.feature.memo.ui.place

import io.github.taetae98coding.diary.core.model.location.Coordinate

internal fun handleMemoPlacePickerEvent(
    event: MemoPlacePickerEvent,
    placeViewModel: MemoPlaceInputController,
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
