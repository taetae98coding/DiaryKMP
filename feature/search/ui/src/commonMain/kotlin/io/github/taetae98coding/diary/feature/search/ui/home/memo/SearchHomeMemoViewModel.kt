package io.github.taetae98coding.diary.feature.search.ui.home.memo

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeMemoViewModel(
    searchMemoUseCase: SearchMemoUseCase,
    private val finishMemoUseCase: FinishMemoUseCase,
    private val restartMemoUseCase: RestartMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val restoreMemoUseCase: RestoreMemoUseCase,
) : SearchHomeResultViewModel<Memo>(
        search = { query, sort ->
            searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = query, sort = sort))
        },
    ) {
    private val _effect = Channel<SearchHomeMemoEffect>(Channel.BUFFERED)
    val effect: Flow<SearchHomeMemoEffect> = _effect.receiveAsFlow()

    fun finish(id: Uuid) {
        launchOnce(id = id) {
            finishMemoUseCase(parameter = id)
                .onSuccess { _effect.send(SearchHomeMemoEffect.Finished(id = id)) }
        }
    }

    fun restart(id: Uuid) {
        launchOnce(id = id) {
            restartMemoUseCase(parameter = id)
                .onSuccess { _effect.send(SearchHomeMemoEffect.Restarted(id = id)) }
        }
    }

    fun delete(id: Uuid) {
        launchOnce(id = id) {
            deleteMemoUseCase(parameter = id)
                .onSuccess { _effect.send(SearchHomeMemoEffect.Deleted(id = id)) }
        }
    }

    fun undo(effect: SearchHomeMemoEffect) {
        launchOnce(id = effect.targetId()) {
            when (effect) {
                is SearchHomeMemoEffect.Finished -> restartMemoUseCase(parameter = effect.id)
                is SearchHomeMemoEffect.Restarted -> finishMemoUseCase(parameter = effect.id)
                is SearchHomeMemoEffect.Deleted -> restoreMemoUseCase(parameter = effect.id)
            }
        }
    }

    private fun SearchHomeMemoEffect.targetId(): Uuid =
        when (this) {
            is SearchHomeMemoEffect.Finished -> id
            is SearchHomeMemoEffect.Restarted -> id
            is SearchHomeMemoEffect.Deleted -> id
        }
}
