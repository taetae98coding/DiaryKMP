package io.github.taetae98coding.diary.compose.list.sort

import io.github.taetae98coding.diary.compose.list.Res
import io.github.taetae98coding.diary.compose.list.list_sort_default_label
import io.github.taetae98coding.diary.compose.list.list_sort_name_label
import io.github.taetae98coding.diary.compose.list.list_sort_recently_updated_label
import io.github.taetae98coding.diary.compose.list.list_sort_title_label
import io.github.taetae98coding.diary.core.model.list.ListSort
import org.jetbrains.compose.resources.StringResource

public fun listSortLabel(sort: ListSort): StringResource =
    when (sort) {
        ListSort.DEFAULT -> Res.string.list_sort_default_label
        ListSort.TITLE -> Res.string.list_sort_title_label
        ListSort.NAME -> Res.string.list_sort_name_label
        ListSort.RECENTLY_UPDATED -> Res.string.list_sort_recently_updated_label
    }
