package io.github.taetae98coding.diary.feature.search.ui.home

import io.github.taetae98coding.diary.feature.search.api.SearchHomeType

internal val searchHomeTypeList: List<SearchHomeType> =
    listOf(
        SearchHomeType.MEMO,
        SearchHomeType.TAG,
        SearchHomeType.PLACE,
        SearchHomeType.WEB,
    )
