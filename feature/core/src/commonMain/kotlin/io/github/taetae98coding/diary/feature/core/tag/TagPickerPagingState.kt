@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.tag

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

public class TagPickerPagingState(
    pageTag: (query: String) -> Flow<Result<PagingData<Tag>>>,
) {
    private val query = MutableStateFlow("")

    public val tagPagingData: Flow<PagingData<Tag>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value -> pageTag(value) }
            .map { result -> result.getOrElse { PagingData.empty() } }

    // 태그 추가 칩이 선택 목록을 열지 태그 추가로 바로 갈지 정하려면 검색어와 관계없이 선택할 태그가 있는지 알아야 한다.
    public val selectableTagPagingData: Flow<PagingData<Tag>> =
        flowOf("")
            .flatMapLatest { value -> pageTag(value) }
            .map { result -> result.getOrElse { PagingData.empty() } }

    public fun updateQuery(query: String) {
        this.query.value = query
    }
}
