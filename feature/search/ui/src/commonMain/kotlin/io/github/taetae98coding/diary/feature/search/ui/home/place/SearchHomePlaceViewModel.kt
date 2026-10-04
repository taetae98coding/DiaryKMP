package io.github.taetae98coding.diary.feature.search.ui.home.place

import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomePlaceViewModel(
    searchPlaceUseCase: SearchPlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
) : SearchHomeResultViewModel<Place>(
        search = { query, sort ->
            searchPlaceUseCase(parameter = SearchPlaceUseCase.Parameter(query = query, sort = sort))
        },
    ) {
    private val _effect = Channel<PlaceListEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceListEffect> = _effect.receiveAsFlow()

    fun delete(id: Uuid) {
        launchOnce(id = id) {
            deletePlaceUseCase(parameter = id)
                .onSuccess { _effect.send(PlaceListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        launchOnce(id = id) {
            restorePlaceUseCase(parameter = id)
        }
    }
}
