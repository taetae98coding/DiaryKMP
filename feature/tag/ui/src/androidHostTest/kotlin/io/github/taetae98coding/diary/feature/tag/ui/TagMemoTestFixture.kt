package io.github.taetae98coding.diary.feature.tag.ui

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
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun tagMemo(
    title: String,
    dateTime: MemoDateTime? = null,
): Memo =
    fixtureMonkey
        .giveMeKotlinBuilder<Memo>()
        .setExp(
            Memo::detail,
            fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = dateTime),
        ).setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .sample()

internal fun tagMemoPagingData(
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

internal fun fixtureText(prefix: String): String = prefix + fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)

internal fun fixtureId(): Uuid = fixtureMonkey.giveMeOne<Uuid>()
