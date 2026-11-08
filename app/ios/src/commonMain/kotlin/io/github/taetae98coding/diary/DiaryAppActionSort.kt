package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue

public enum class DiaryAppActionSort {
    DEFAULT,
    TITLE,
    NAME,
    RECENTLY_UPDATED,
}

internal fun DiaryAppActionSort?.toSortValue(): String? =
    when (this) {
        null -> null
        DiaryAppActionSort.DEFAULT -> AppActionSortValue.DEFAULT
        DiaryAppActionSort.TITLE -> AppActionSortValue.TITLE
        DiaryAppActionSort.NAME -> AppActionSortValue.NAME
        DiaryAppActionSort.RECENTLY_UPDATED -> AppActionSortValue.RECENTLY_UPDATED
    }
