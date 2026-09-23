package io.github.taetae98coding.diary.compose.memo.list

import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlinx.datetime.LocalDate

public sealed interface MemoListItem {
    public data class DateHeader(
        val date: LocalDate,
    ) : MemoListItem

    public data class Content(
        val memo: Memo,
    ) : MemoListItem
}

public fun PagingData<Memo>.toMemoListItem(sort: ListSort): PagingData<MemoListItem> {
    val itemPagingData = map<Memo, MemoListItem> { memo -> MemoListItem.Content(memo = memo) }

    return if (sort == ListSort.DEFAULT) {
        itemPagingData.insertSeparators { before, after -> dateHeaderOrNull(before = before, after = after) }
    } else {
        itemPagingData
    }
}

internal fun dateHeaderOrNull(
    before: MemoListItem?,
    after: MemoListItem?,
): MemoListItem.DateHeader? {
    val afterDate = after?.startDate ?: return null

    return if (afterDate == before?.startDate) {
        null
    } else {
        MemoListItem.DateHeader(date = afterDate)
    }
}

internal val MemoListItem.startDate: LocalDate?
    get() =
        when (this) {
            is MemoListItem.DateHeader -> date
            is MemoListItem.Content -> memo.startDate
        }

private val Memo.startDate: LocalDate?
    get() =
        when (val dateTime = detail.dateTime) {
            is MemoDateTime.AllDay -> dateTime.dateRange.start
            is MemoDateTime.DateTime -> dateTime.start.date
            null -> null
        }
