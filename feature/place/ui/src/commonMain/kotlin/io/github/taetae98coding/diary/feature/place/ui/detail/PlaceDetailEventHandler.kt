package io.github.taetae98coding.diary.feature.place.ui.detail

import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchEvent
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel

internal fun handlePlaceDetailScaffoldEvent(
    event: PlaceDetailScaffoldEvent,
    detailViewModel: PlaceDetailViewModel,
    scaffoldState: PlaceFormState,
    openExternalMap: () -> Unit,
    navigateUp: () -> Unit,
) {
    when (event) {
        is PlaceDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is PlaceDetailScaffoldEvent.ClickUpdate -> detailViewModel.update(detail = scaffoldState.detail)
        is PlaceDetailScaffoldEvent.ClickDelete -> detailViewModel.delete()
        is PlaceDetailScaffoldEvent.ClickSearch -> scaffoldState.searchDialogState.show()
        is PlaceDetailScaffoldEvent.ClickOpenExternalMap -> openExternalMap()
    }
}

internal fun PlaceSearchViewModel.handleSearchEvent(
    event: PlaceSearchEvent,
    scaffoldState: PlaceFormState,
) {
    when (event) {
        is PlaceSearchEvent.Search -> search(request = event.request)
        is PlaceSearchEvent.ClearSearch -> clear()
        is PlaceSearchEvent.Select -> scaffoldState.applySearchedPlace(event.place)
    }
}

internal fun PlaceDetailTagViewModel.handleTagPickerEvent(
    event: EntityTagPickerEvent,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is EntityTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is EntityTagPickerEvent.Add -> add(tagId = event.id)
        is EntityTagPickerEvent.Remove -> remove(tagId = event.id)
        is EntityTagPickerEvent.ChangeQuery -> updateQuery(query = event.query)
    }
}
