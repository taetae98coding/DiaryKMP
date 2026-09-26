package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun placeMemo(
    title: String,
    dateTime: MemoDateTime? = null,
): Memo =
    fixtureMonkey
        .giveMeKotlinBuilder<Memo>()
        .setExp(
            Memo::detail,
            fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = dateTime),
        ).setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()

internal fun placeMemoPagingData(
    itemList: List<MemoListItem>,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
): PagingData<MemoListItem> =
    PagingData.from(
        data = itemList,
        sourceLoadStates =
            LoadStates(
                refresh = refresh,
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = append,
            ),
    )

internal fun allDayMemoDateTime(date: LocalDate): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = date..date)
