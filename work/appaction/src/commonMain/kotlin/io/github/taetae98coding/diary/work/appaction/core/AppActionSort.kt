package io.github.taetae98coding.diary.work.appaction.core

import io.github.taetae98coding.diary.core.model.list.ListSort

private const val SORT = "sort"

public object AppActionSortValue {
    public const val DEFAULT: String = "default"
    public const val TITLE: String = "title"
    public const val NAME: String = "name"
    public const val RECENTLY_UPDATED: String = "recently_updated"
}

internal fun ListSort.toSortValue(): String =
    when (this) {
        ListSort.DEFAULT -> AppActionSortValue.DEFAULT
        ListSort.TITLE -> AppActionSortValue.TITLE
        ListSort.NAME -> AppActionSortValue.NAME
        ListSort.RECENTLY_UPDATED -> AppActionSortValue.RECENTLY_UPDATED
    }

// 목록마다 고를 수 있는 정렬 기준이 다르므로, 그 목록이 제공하는 기준만 받는다.
internal fun String?.toSortArgument(sortList: List<ListSort>): ListSort {
    val value = this ?: return sortList.first()

    return sortList.firstOrNull { sort -> sort.toSortValue() == value }
        ?: invalidArgument(message = "'$SORT' must be one of ${sortList.joinToString { sort -> sort.toSortValue() }}.")
}
