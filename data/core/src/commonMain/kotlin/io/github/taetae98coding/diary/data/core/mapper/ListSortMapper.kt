package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.model.list.ListSort

public fun ListSort.toLocal(): ListSortLocalEntity =
    when (this) {
        ListSort.DEFAULT -> ListSortLocalEntity.DEFAULT
        ListSort.TITLE -> ListSortLocalEntity.TITLE
        ListSort.NAME -> ListSortLocalEntity.NAME
        ListSort.RECENTLY_UPDATED -> ListSortLocalEntity.RECENTLY_UPDATED
    }
