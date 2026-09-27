@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration

internal class SearchHomeQueryInput(
    private val scope: CoroutineScope,
) {
    // 화면이 질의를 알리기 전(null)에는 조회하지 않는다. 빈 질의로 먼저 조회하면 복원된 질의의 결과보다 빈 결과가 먼저 보인다.
    private val input = MutableStateFlow<Input?>(null)
    private var shownQuery: QueryMark = QueryMark.None
    private var resultQuery: QueryMark = QueryMark.None

    val appliedQuery: StateFlow<String>
        field = MutableStateFlow("")

    private val reportedQuery: Flow<String> =
        input
            .filterNotNull()
            .debounce { value -> if (value.isImmediate || value.query.isBlank()) Duration.ZERO else INPUT_IDLE_DELAY }
            .map { value -> value.query }
            .onEach { query -> appliedQuery.value = query }

    fun show(query: String) {
        shownQuery = QueryMark.Of(query = query)
        input.value = Input(query = query, isImmediate = true)
    }

    fun update(query: String) {
        input.value = Input(query = query, isImmediate = false)
    }

    fun <T : Any> pagingData(
        sort: Flow<ListSort>,
        search: (query: String, sort: ListSort) -> Flow<PagingData<T>>,
    ): Flow<PagingData<T>> {
        val cachedPagingData =
            combine(reportedQuery, sort) { query, sortValue -> query to sortValue }
                .flatMapLatest { (query, sortValue) -> search(query, sortValue).onEach { resultQuery = QueryMark.Of(query = query) } }
                .cachedIn(scope)

        // 유형을 떠나 있는 동안에도 결과는 유지되므로, 다시 나타난 유형이 떠나기 전의 다른 질의 결과를 먼저 받지 않도록
        // 다시 구독할 때 지금 알린 질의의 결과가 올 때까지 앞선 결과를 건너뛴다.
        return cachedPagingData.dropWhile { resultQuery != shownQuery }
    }

    private data class Input(
        val query: String,
        val isImmediate: Boolean,
    )

    private sealed interface QueryMark {
        data object None : QueryMark

        data class Of(
            val query: String,
        ) : QueryMark
    }
}
