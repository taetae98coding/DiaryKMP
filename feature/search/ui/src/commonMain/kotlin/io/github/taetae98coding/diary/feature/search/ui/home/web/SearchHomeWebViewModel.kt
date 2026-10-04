package io.github.taetae98coding.diary.feature.search.ui.home.web

import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeWebViewModel(
    searchWebUseCase: SearchWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    private val restoreWebUseCase: RestoreWebUseCase,
) : SearchHomeResultViewModel<Web>(
        search = { query, sort ->
            searchWebUseCase(parameter = SearchWebUseCase.Parameter(query = query, sort = sort))
        },
    ) {
    private val _effect = Channel<WebListEffect>(Channel.BUFFERED)
    val effect: Flow<WebListEffect> = _effect.receiveAsFlow()

    fun delete(id: Uuid) {
        launchOnce(id = id) {
            deleteWebUseCase(parameter = id)
                .onSuccess { _effect.send(WebListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        launchOnce(id = id) {
            restoreWebUseCase(parameter = id)
        }
    }
}
