package io.github.taetae98coding.diary.feature.search.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort

internal data class SearchHomeResultUiState(
    val sort: ListSort = ListSort.TITLE,
    val appliedQuery: String = "",
)
