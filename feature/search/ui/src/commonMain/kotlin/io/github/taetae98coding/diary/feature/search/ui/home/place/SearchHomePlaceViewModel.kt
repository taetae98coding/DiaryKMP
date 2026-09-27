package io.github.taetae98coding.diary.feature.search.ui.home.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeQueryInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomePlaceViewModel(
    searchPlaceUseCase: SearchPlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
) : ViewModel() {
    private val query = SearchHomeQueryInput(scope = viewModelScope)

    val appliedQuery: StateFlow<String> = query.appliedQuery

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val pagingData: Flow<PagingData<Place>> =
        query.pagingData(sort = sort) { value, sortValue ->
            searchPlaceUseCase(parameter = SearchPlaceUseCase.Parameter(query = value, sort = sortValue))
                .map { result -> result.getOrElse { PagingData.empty() } }
        }

    private val _effect = Channel<PlaceListEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceListEffect> = _effect.receiveAsFlow()

    fun showQuery(query: String) {
        this.query.show(query = query)
    }

    fun updateQuery(query: String) {
        this.query.update(query = query)
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deletePlaceUseCase(parameter = id)
                .onSuccess { _effect.send(PlaceListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restorePlaceUseCase(parameter = id)
        }
    }
}
