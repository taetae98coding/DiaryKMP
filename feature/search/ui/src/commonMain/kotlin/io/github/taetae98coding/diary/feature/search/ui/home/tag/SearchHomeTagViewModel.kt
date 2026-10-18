package io.github.taetae98coding.diary.feature.search.ui.home.tag

import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeTagViewModel(
    searchTagUseCase: SearchTagUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val restoreTagUseCase: RestoreTagUseCase,
) : SearchHomeResultViewModel<Tag>(
        search = { query, sort ->
            searchTagUseCase(parameter = SearchTagUseCase.Parameter(query = query, sort = sort))
        },
    ) {
    private val _effect = Channel<TagListEffect>(Channel.BUFFERED)
    val effect: Flow<TagListEffect> = _effect.receiveAsFlow()

    fun finish(id: Uuid) {
        launchOnce(id = id) {
            finishTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Finished(id = id)) }
        }
    }

    fun restart(id: Uuid) {
        launchOnce(id = id) {
            restartTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Restarted(id = id)) }
        }
    }

    fun delete(id: Uuid) {
        launchOnce(id = id) {
            deleteTagUseCase(parameter = id)
                .onSuccess { _effect.send(TagListEffect.Deleted(id = id)) }
        }
    }

    fun undo(effect: TagListEffect) {
        launchOnce(id = effect.targetId()) {
            when (effect) {
                is TagListEffect.Finished -> restartTagUseCase(parameter = effect.id)
                is TagListEffect.Restarted -> finishTagUseCase(parameter = effect.id)
                is TagListEffect.Deleted -> restoreTagUseCase(parameter = effect.id)
            }
        }
    }

    private fun TagListEffect.targetId(): Uuid =
        when (this) {
            is TagListEffect.Finished -> id
            is TagListEffect.Restarted -> id
            is TagListEffect.Deleted -> id
        }
}
