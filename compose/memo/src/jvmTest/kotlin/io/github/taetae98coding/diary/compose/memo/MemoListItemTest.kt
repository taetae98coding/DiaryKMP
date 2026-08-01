package io.github.taetae98coding.diary.compose.memo

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoListItemTest :
    FunSpec({
        test("TC-MEMO-HOME-FEATURE-013 TC-MEMO-HOME-FEATURE-014 시작 날짜가 바뀌는 자리마다 날짜 헤더를 하나 만든다") {
            val allDayMemo = memo(dateTime = allDay(day = 19))
            val sameDateMemo = memo(dateTime = dateTime(day = 19, hour = 13))
            val nextDateMemo = memo(dateTime = allDay(day = 21))

            val itemList = memoListItemList(listOf(allDayMemo, sameDateMemo, nextDateMemo))

            itemList shouldBe
                listOf(
                    MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)),
                    MemoListItem.Content(memo = allDayMemo),
                    MemoListItem.Content(memo = sameDateMemo),
                    MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 21)),
                    MemoListItem.Content(memo = nextDateMemo),
                )
        }

        test("TC-MEMO-HOME-FEATURE-016 기간이 없는 메모에는 날짜 헤더를 만들지 않는다") {
            val noDateTimeMemo = memo(dateTime = null)
            val otherNoDateTimeMemo = memo(dateTime = null)
            val allDayMemo = memo(dateTime = allDay(day = 19))

            val itemList = memoListItemList(listOf(noDateTimeMemo, otherNoDateTimeMemo, allDayMemo))

            itemList shouldBe
                listOf(
                    MemoListItem.Content(memo = noDateTimeMemo),
                    MemoListItem.Content(memo = otherNoDateTimeMemo),
                    MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)),
                    MemoListItem.Content(memo = allDayMemo),
                )
        }

        test("기간이 없는 메모만 있으면 날짜 헤더가 없다") {
            val itemList = memoListItemList(listOf(memo(dateTime = null)))

            itemList.filterIsInstance<MemoListItem.DateHeader>() shouldBe emptyList()
        }

        test("메모가 없으면 항목도 없다") {
            memoListItemList(emptyList()) shouldBe emptyList()
        }

        listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED).forEach { sort ->
            test("TC-MEMO-HOME-DOMAIN-016 ${'$'}sort 정렬에서는 날짜 헤더를 만들지 않는다") {
                val allDayMemo = memo(dateTime = allDay(day = 19))
                val nextDateMemo = memo(dateTime = allDay(day = 21))

                val itemList = memoListItemList(listOf(allDayMemo, nextDateMemo), sort = sort)

                itemList shouldBe
                    listOf(
                        MemoListItem.Content(memo = allDayMemo),
                        MemoListItem.Content(memo = nextDateMemo),
                    )
            }
        }

        test("TC-MEMO-HOME-DOMAIN-016 기본순에서는 날짜 헤더를 만든다") {
            val allDayMemo = memo(dateTime = allDay(day = 19))

            val itemList = memoListItemList(listOf(allDayMemo), sort = ListSort.DEFAULT)

            itemList.filterIsInstance<MemoListItem.DateHeader>() shouldBe
                listOf(MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)))
        }
    })

private suspend fun memoListItemList(
    memoList: List<Memo>,
    sort: ListSort = ListSort.DEFAULT,
): List<MemoListItem> = flowOf(PagingData.from(memoList).toMemoListItem(sort = sort)).asSnapshot()

private fun memo(dateTime: MemoDateTime?): Memo =
    fixtureMonkey
        .giveMeKotlinBuilder<Memo>()
        .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = dateTime))
        .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .sample()

private fun allDay(day: Int): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = day)..LocalDate(year = 2026, month = 7, day = day))

private fun dateTime(
    day: Int,
    hour: Int,
): MemoDateTime.DateTime =
    MemoDateTime.DateTime(
        start = LocalDateTime(year = 2026, month = 7, day = day, hour = hour, minute = 30),
        endInclusive = LocalDateTime(year = 2026, month = 7, day = day + 1, hour = hour, minute = 30),
    )
