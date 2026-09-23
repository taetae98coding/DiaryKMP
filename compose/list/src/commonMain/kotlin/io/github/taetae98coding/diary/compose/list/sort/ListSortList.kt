package io.github.taetae98coding.diary.compose.list.sort

import io.github.taetae98coding.diary.core.model.list.ListSort

public val listSortList: List<ListSort> =
    listOf(
        ListSort.TITLE,
        ListSort.RECENTLY_UPDATED,
    )

public val nameListSortList: List<ListSort> =
    listOf(
        ListSort.NAME,
        ListSort.RECENTLY_UPDATED,
    )

public val memoListSortList: List<ListSort> =
    listOf(
        ListSort.DEFAULT,
        ListSort.TITLE,
        ListSort.RECENTLY_UPDATED,
    )
