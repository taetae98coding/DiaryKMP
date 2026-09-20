package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountDailyMemoLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountDailyMemoLocalDataSourceImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountDailyMemoLocalDataSourceImpl(database = database)
            memoTransaction = AccountMemoTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun upsert(
            accountId: Uuid,
            vararg memoList: MemoLocalEntity,
        ) {
            memoTransaction.upsert(accountId = accountId, memoList = memoList.toList(), memoTagList = emptyList())
        }

        suspend fun dailyMemoIdList(accountId: Uuid): List<Uuid> =
            dataSource
                .get(accountId = accountId, date = TODAY)
                .first()
                .map { dailyMemo -> dailyMemo.id }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-008 기간이 알림이 발생하는 날과 하루라도 겹치는 메모만 오늘의 메모다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoList =
                listOf(
                    allDayMemo(start = TODAY, endInclusive = TODAY) to true,
                    allDayMemo(start = TODAY.minusDays(2), endInclusive = TODAY) to true,
                    allDayMemo(start = TODAY, endInclusive = TODAY.plusDays(2)) to true,
                    allDayMemo(start = TODAY.minusDays(2), endInclusive = TODAY.plusDays(2)) to true,
                    allDayMemo(start = TODAY.minusDays(2), endInclusive = TODAY.minusDays(1)) to false,
                    allDayMemo(start = TODAY.plusDays(1), endInclusive = TODAY.plusDays(2)) to false,
                    memo(detail = detail(isAllDay = null, start = null, endInclusive = null)) to false,
                )
            upsert(accountId, *memoList.map { (memo, _) -> memo }.toTypedArray())

            dailyMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder
                memoList.filter { (_, isIncluded) -> isIncluded }.map { (memo, _) -> memo.id }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-009 시각이 있는 기간의 겹침도 날짜만으로 판단한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoList =
                listOf(
                    dateTimeMemo(
                        start = LocalDateTime(date = TODAY.minusDays(1), time = LocalTime(hour = 23, minute = 0)),
                        endInclusive = LocalDateTime(date = TODAY, time = LocalTime(hour = 0, minute = 30)),
                    ) to true,
                    dateTimeMemo(
                        start = LocalDateTime(date = TODAY, time = LocalTime(hour = 23, minute = 30)),
                        endInclusive = LocalDateTime(date = TODAY.plusDays(1), time = LocalTime(hour = 1, minute = 0)),
                    ) to true,
                    dateTimeMemo(
                        start = LocalDateTime(date = TODAY.minusDays(1), time = LocalTime(hour = 9, minute = 0)),
                        endInclusive = LocalDateTime(date = TODAY.minusDays(1), time = LocalTime(hour = 23, minute = 59)),
                    ) to false,
                )
            upsert(accountId, *memoList.map { (memo, _) -> memo }.toTypedArray())

            dailyMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder
                memoList.filter { (_, isIncluded) -> isIncluded }.map { (memo, _) -> memo.id }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-010 완료된 메모는 오늘의 메모에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedMemo = todayMemo().copy(isFinished = true)
            val unfinishedMemo = todayMemo().copy(isFinished = false)

            upsert(accountId, finishedMemo, unfinishedMemo)

            dailyMemoIdList(accountId = accountId) shouldBe listOf(unfinishedMemo.id)
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-011 삭제된 메모는 오늘의 메모에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val deletedMemo = todayMemo().copy(isDeleted = true)
            val memo = todayMemo().copy(isDeleted = false)

            upsert(accountId, deletedMemo, memo)

            dailyMemoIdList(accountId = accountId) shouldBe listOf(memo.id)
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-012 현재 사용자 계정과 연결된 메모만 오늘의 메모다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = todayMemo()
            val otherAccountMemo = todayMemo()
            upsert(accountId, memo)
            upsert(otherAccountId, otherAccountMemo)

            dailyMemoIdList(accountId = accountId) shouldBe listOf(memo.id)
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-013 오늘의 메모 순서는 캘린더 메모의 조회 결과 정렬을 따른다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val earlierStartAllDayMemo = allDayMemo(start = TODAY.minusDays(1), endInclusive = TODAY).withTitle("B")
            val laterStartAllDayMemo = allDayMemo(start = TODAY, endInclusive = TODAY).withTitle("A")
            val earlierStartDateTimeMemo =
                dateTimeMemo(
                    start = LocalDateTime(date = TODAY, time = LocalTime(hour = 9, minute = 0)),
                    endInclusive = LocalDateTime(date = TODAY, time = LocalTime(hour = 10, minute = 0)),
                ).withTitle("D")
            val sameStartFirstTitleDateTimeMemo =
                dateTimeMemo(
                    start = LocalDateTime(date = TODAY, time = LocalTime(hour = 13, minute = 0)),
                    endInclusive = LocalDateTime(date = TODAY, time = LocalTime(hour = 14, minute = 0)),
                ).withTitle("C")
            val sameStartLaterTitleDateTimeMemo =
                dateTimeMemo(
                    start = LocalDateTime(date = TODAY, time = LocalTime(hour = 13, minute = 0)),
                    endInclusive = LocalDateTime(date = TODAY, time = LocalTime(hour = 14, minute = 0)),
                ).withTitle("E")

            upsert(
                accountId,
                sameStartLaterTitleDateTimeMemo,
                laterStartAllDayMemo,
                earlierStartDateTimeMemo,
                sameStartFirstTitleDateTimeMemo,
                earlierStartAllDayMemo,
            )

            dailyMemoIdList(accountId = accountId) shouldContainExactly
                listOf(
                    earlierStartAllDayMemo.id,
                    laterStartAllDayMemo.id,
                    earlierStartDateTimeMemo.id,
                    sameStartFirstTitleDateTimeMemo.id,
                    sameStartLaterTitleDateTimeMemo.id,
                )
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DATA-001 오늘의 메모 조회에는 캘린더의 태그 필터를 적용하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val memoWithoutTag = todayMemo()
            upsert(accountId, memoWithoutTag)
            database.calendarFilterTagDao().upsert(entity = CalendarFilterTagLocalEntity(accountId = accountId, tagId = tag.id))

            database
                .accountCalendarMemoDao()
                .get(accountId = accountId, start = TODAY, endInclusive = TODAY)
                .first()
                .shouldBeEmpty()
            dailyMemoIdList(accountId = accountId) shouldBe listOf(memoWithoutTag.id)
        }

        test("오늘의 메모에는 메모의 제목이 그대로 담긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = todayMemo().withTitle("title ${fixtureMonkey.giveMeOne<String>()}")

            upsert(accountId, memo)

            dataSource
                .get(accountId = accountId, date = TODAY)
                .first()
                .single()
                .title shouldBe memo.detail.title
        }

        test("오늘과 겹치는 메모가 없으면 빈 목록을 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            upsert(accountId, allDayMemo(start = TODAY.plusDays(1), endInclusive = TODAY.plusDays(1)))

            dailyMemoIdList(accountId = accountId).shouldBeEmpty()
        }
    }) {
    private companion object {
        private val TODAY: LocalDate = LocalDate(2026, 9, 8)
        private val Midnight: LocalTime = LocalTime(hour = 0, minute = 0)

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun LocalDate.minusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() - days)

        private fun LocalDate.plusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() + days)

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun detail(
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
        ): MemoDetailLocalEntity =
            fixtureMonkey
                .giveMeOne<MemoDetailLocalEntity>()
                .copy(
                    isAllDay = isAllDay,
                    start = start,
                    endInclusive = endInclusive,
                )

        private fun memo(detail: MemoDetailLocalEntity): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()
                .copy(
                    detail = detail,
                    primaryTagId = null,
                    isFinished = false,
                    isDeleted = false,
                )

        private fun MemoLocalEntity.withTitle(title: String): MemoLocalEntity = copy(detail = detail.copy(title = title))

        private fun allDayMemo(
            start: LocalDate,
            endInclusive: LocalDate,
        ): MemoLocalEntity =
            memo(
                detail =
                    detail(
                        isAllDay = true,
                        start = LocalDateTime(date = start, time = Midnight),
                        endInclusive = LocalDateTime(date = endInclusive, time = Midnight),
                    ),
            )

        private fun dateTimeMemo(
            start: LocalDateTime,
            endInclusive: LocalDateTime,
        ): MemoLocalEntity =
            memo(
                detail =
                    detail(
                        isAllDay = false,
                        start = start,
                        endInclusive = endInclusive,
                    ),
            )

        private fun todayMemo(): MemoLocalEntity = allDayMemo(start = TODAY, endInclusive = TODAY)

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
    }
}
