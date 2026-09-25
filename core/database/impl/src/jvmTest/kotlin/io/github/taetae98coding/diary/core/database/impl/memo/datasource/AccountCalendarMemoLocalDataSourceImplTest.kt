package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountCalendarMemoLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountCalendarMemoLocalDataSourceImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountCalendarMemoLocalDataSourceImpl(database = database)
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

        fun calendarMemoFlow(accountId: Uuid): Flow<List<CalendarMemoLocalEntity>> =
            dataSource.get(
                accountId = accountId,
                dateRange = RANGE_START..RANGE_END_INCLUSIVE,
            )

        suspend fun calendarMemoList(accountId: Uuid): List<CalendarMemoLocalEntity> = calendarMemoFlow(accountId = accountId).first()

        suspend fun calendarMemoIdList(accountId: Uuid): List<Uuid> = calendarMemoList(accountId = accountId).map { calendarMemo -> calendarMemo.id }

        test("TC-CALENDAR-MEMO-DOMAIN-001 표시 대상 기간과 겹치는 메모만 표시 대상이 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoList =
                listOf(
                    allDayMemo(start = LocalDate(2026, 7, 1), endInclusive = LocalDate(2026, 7, 4)) to false,
                    allDayMemo(start = LocalDate(2026, 7, 1), endInclusive = LocalDate(2026, 7, 5)) to true,
                    allDayMemo(start = LocalDate(2026, 7, 6), endInclusive = LocalDate(2026, 7, 8)) to true,
                    allDayMemo(start = LocalDate(2026, 7, 5), endInclusive = LocalDate(2026, 7, 11)) to true,
                    allDayMemo(start = LocalDate(2026, 7, 1), endInclusive = LocalDate(2026, 7, 20)) to true,
                    allDayMemo(start = LocalDate(2026, 7, 11), endInclusive = LocalDate(2026, 7, 15)) to true,
                    allDayMemo(start = LocalDate(2026, 7, 12), endInclusive = LocalDate(2026, 7, 15)) to false,
                )
            upsert(accountId, *memoList.map { (memo, _) -> memo }.toTypedArray())

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder
                memoList.filter { (_, isIncluded) -> isIncluded }.map { (memo, _) -> memo.id }
        }

        test("TC-CALENDAR-MEMO-DOMAIN-002 시각이 있는 기간의 겹침도 날짜만으로 판단한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoList =
                listOf(
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 4, 23, 59),
                        endInclusive = LocalDateTime(2026, 7, 4, 23, 59),
                    ) to false,
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 4, 23, 0),
                        endInclusive = LocalDateTime(2026, 7, 5, 0, 0),
                    ) to true,
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 11, 23, 59),
                        endInclusive = LocalDateTime(2026, 7, 12, 1, 0),
                    ) to true,
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 12, 0, 0),
                        endInclusive = LocalDateTime(2026, 7, 12, 1, 0),
                    ) to false,
                )
            upsert(accountId, *memoList.map { (memo, _) -> memo }.toTypedArray())

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder
                memoList.filter { (_, isIncluded) -> isIncluded }.map { (memo, _) -> memo.id }
        }

        test("TC-CALENDAR-MEMO-DOMAIN-003 기간이 없는 메모는 표시하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val noDateTimeMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = null,
                            start = null,
                            endInclusive = null,
                        ),
                )
            val overlappingMemo = overlappingMemo()

            upsert(accountId, noDateTimeMemo, overlappingMemo)

            calendarMemoIdList(accountId = accountId) shouldBe listOf(overlappingMemo.id)
        }

        test("TC-CALENDAR-MEMO-DOMAIN-004 삭제된 메모는 표시하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val deletedMemo = overlappingMemo().copy(isDeleted = true)
            val memo = overlappingMemo().copy(isDeleted = false)

            upsert(accountId, deletedMemo, memo)

            calendarMemoIdList(accountId = accountId) shouldBe listOf(memo.id)
        }

        test("TC-CALENDAR-MEMO-DOMAIN-005 완료된 메모도 표시한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedMemo = overlappingMemo().copy(isFinished = true)
            val unfinishedMemo = overlappingMemo().copy(isFinished = false)

            upsert(accountId, finishedMemo, unfinishedMemo)

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(finishedMemo.id, unfinishedMemo.id)
        }

        test("TC-CALENDAR-MEMO-DOMAIN-006 현재 사용자 계정과 연결된 메모만 표시한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = overlappingMemo()
            val otherAccountMemo = overlappingMemo()
            upsert(accountId, memo)
            upsert(otherAccountId, otherAccountMemo)

            calendarMemoIdList(accountId = accountId) shouldBe listOf(memo.id)
            calendarMemoIdList(accountId = otherAccountId) shouldBe listOf(otherAccountMemo.id)
        }

        test("TC-CALENDAR-MEMO-DOMAIN-007 메모에 기록된 기간 형태를 그대로 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val allDayMemo = allDayMemo(start = LocalDate(2026, 7, 6), endInclusive = LocalDate(2026, 7, 7))
            val dateTimeMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 8, 13, 30),
                    endInclusive = LocalDateTime(2026, 7, 9, 9, 0),
                )
            upsert(accountId, allDayMemo, dateTimeMemo)

            val calendarMemoMap = calendarMemoList(accountId = accountId).associateBy { calendarMemo -> calendarMemo.id }

            calendarMemoMap.getValue(allDayMemo.id).isAllDay shouldBe true
            calendarMemoMap.getValue(allDayMemo.id).start shouldBe allDayMemo.detail.start
            calendarMemoMap.getValue(allDayMemo.id).endInclusive shouldBe allDayMemo.detail.endInclusive
            calendarMemoMap.getValue(dateTimeMemo.id).isAllDay shouldBe false
            calendarMemoMap.getValue(dateTimeMemo.id).start shouldBe LocalDateTime(2026, 7, 8, 13, 30)
            calendarMemoMap.getValue(dateTimeMemo.id).endInclusive shouldBe LocalDateTime(2026, 7, 9, 9, 0)
        }

        test("TC-CALENDAR-MEMO-DOMAIN-008 TC-MEMO-PRIMARY-TAG-DOMAIN-005 대표 태그가 있으면 대표 태그의 컬러를 표시 색상으로 사용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val aliveTag = tag().copy(isFinished = false, isDeleted = false)
            val deletedTag = tag().copy(isFinished = false, isDeleted = true)
            val finishedTag = tag().copy(isFinished = true, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(aliveTag, deletedTag, finishedTag), tagLinkList = emptyList())
            val noPrimaryTagMemo = overlappingMemo().copy(primaryTagId = null)
            val aliveTagMemo = overlappingMemo().copy(primaryTagId = aliveTag.id)
            val deletedTagMemo = overlappingMemo().copy(primaryTagId = deletedTag.id)
            val unknownTagMemo = overlappingMemo().copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
            val finishedTagMemo = overlappingMemo().copy(primaryTagId = finishedTag.id)

            upsert(accountId, noPrimaryTagMemo, aliveTagMemo, deletedTagMemo, unknownTagMemo, finishedTagMemo)

            val colorMap = calendarMemoList(accountId = accountId).associate { calendarMemo -> calendarMemo.id to calendarMemo.color }

            colorMap[noPrimaryTagMemo.id] shouldBe noPrimaryTagMemo.detail.color
            colorMap[aliveTagMemo.id] shouldBe aliveTag.detail.color
            colorMap[deletedTagMemo.id] shouldBe deletedTagMemo.detail.color
            colorMap[unknownTagMemo.id] shouldBe unknownTagMemo.detail.color
            colorMap[finishedTagMemo.id] shouldBe finishedTag.detail.color
        }

        test("TC-CALENDAR-MEMO-DOMAIN-018 종일 메모를 우선하고 시작이 이른 순으로 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val laterEndAllDayMemo =
                allDayMemo(start = LocalDate(2026, 7, 6), endInclusive = LocalDate(2026, 7, 9))
            val firstTitleAllDayMemo =
                allDayMemo(start = LocalDate(2026, 7, 6), endInclusive = LocalDate(2026, 7, 7))
                    .withTitle("A")
            val laterTitleAllDayMemo =
                allDayMemo(start = LocalDate(2026, 7, 6), endInclusive = LocalDate(2026, 7, 7))
                    .withTitle("B")
            val laterStartAllDayMemo = allDayMemo(start = LocalDate(2026, 7, 7), endInclusive = LocalDate(2026, 7, 8))
            val earlierDateTimeMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 5, 9, 0),
                    endInclusive = LocalDateTime(2026, 7, 5, 10, 0),
                )

            upsert(
                accountId,
                earlierDateTimeMemo,
                laterStartAllDayMemo,
                laterTitleAllDayMemo,
                firstTitleAllDayMemo,
                laterEndAllDayMemo,
            )

            calendarMemoIdList(accountId = accountId) shouldContainExactly
                listOf(
                    laterEndAllDayMemo.id,
                    firstTitleAllDayMemo.id,
                    laterTitleAllDayMemo.id,
                    laterStartAllDayMemo.id,
                    earlierDateTimeMemo.id,
                )
        }

        test("TC-CALENDAR-MEMO-DOMAIN-019 시각이 있는 메모는 시작과 종료가 이른 순으로 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val earlierStartMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 6, 8, 0),
                    endInclusive = LocalDateTime(2026, 7, 6, 12, 0),
                )
            val earlierEndMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 6, 9, 0),
                    endInclusive = LocalDateTime(2026, 7, 6, 10, 0),
                )
            val firstTitleMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 6, 9, 0),
                    endInclusive = LocalDateTime(2026, 7, 6, 11, 0),
                ).withTitle("A")
            val laterTitleMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 6, 9, 0),
                    endInclusive = LocalDateTime(2026, 7, 6, 11, 0),
                ).withTitle("B")
            val laterStartMemo =
                dateTimeMemo(
                    start = LocalDateTime(2026, 7, 6, 10, 0),
                    endInclusive = LocalDateTime(2026, 7, 6, 10, 30),
                )

            upsert(
                accountId,
                laterStartMemo,
                laterTitleMemo,
                firstTitleMemo,
                earlierEndMemo,
                earlierStartMemo,
            )

            calendarMemoIdList(accountId = accountId) shouldContainExactly
                listOf(
                    earlierStartMemo.id,
                    earlierEndMemo.id,
                    firstTitleMemo.id,
                    laterTitleMemo.id,
                    laterStartMemo.id,
                )
        }

        test("TC-CALENDAR-MEMO-DOMAIN-016 같은 표시 대상 기간을 다시 조회해도 순서가 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            upsert(
                accountId,
                allDayMemo(start = LocalDate(2026, 7, 5), endInclusive = LocalDate(2026, 7, 6)),
                allDayMemo(start = LocalDate(2026, 7, 5), endInclusive = LocalDate(2026, 7, 8)),
                dateTimeMemo(start = LocalDateTime(2026, 7, 7, 9, 0), endInclusive = LocalDateTime(2026, 7, 7, 10, 0)),
            )

            calendarMemoIdList(accountId = accountId) shouldContainExactly calendarMemoIdList(accountId = accountId)
        }

        test("TC-CALENDAR-MEMO-DATA-001 대표 태그의 컬러가 바뀌면 조작 없이 표시 색상이 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isDeleted = false)
            val memo = overlappingMemo().copy(primaryTagId = tag.id)
            val changedDetail = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            upsert(accountId, memo)

            calendarMemoFlow(accountId = accountId).test {
                awaitItem().single().color shouldBe tag.detail.color

                tagTransaction.updateDetail(
                    accountId = accountId,
                    tagId = tag.id,
                    detail = changedDetail,
                    updatedAt = instant(),
                )

                awaitUntil { calendarMemoList -> calendarMemoList.single().color == changedDetail.color }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-CALENDAR-MEMO-DATA-002 TC-CALENDAR-MEMO-DATA-005 대표 태그가 나중에 저장되거나 삭제되면 표시 색상이 그에 맞게 바뀐다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isDeleted = false)
            val memo = overlappingMemo().copy(primaryTagId = tag.id)
            upsert(accountId, memo)

            calendarMemoFlow(accountId = accountId).test {
                awaitItem().single().color shouldBe memo.detail.color

                tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
                awaitUntil { calendarMemoList -> calendarMemoList.single().color == tag.detail.color }

                tagTransaction.updateDeleted(
                    accountId = accountId,
                    tagId = tag.id,
                    isDeleted = true,
                    updatedAt = instant(),
                )
                awaitUntil { calendarMemoList -> calendarMemoList.single().color == memo.detail.color }

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-MEMO-PRIMARY-TAG-DOMAIN-006 삭제한 태그의 삭제가 다른 기기에서 받은 내용으로 풀려도 대표 태그 지정이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isDeleted = true)
            val memo = overlappingMemo().copy(primaryTagId = tag.id)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            upsert(accountId, memo)

            calendarMemoFlow(accountId = accountId).test {
                awaitUntil { calendarMemoList -> calendarMemoList.single().color == memo.detail.color }

                AccountTagSyncTransactionImpl(database = database).save(
                    accountId = accountId,
                    tagList = listOf(tag.copy(isDeleted = false)),
                    cursor = fixtureMonkey.giveMeOne<Long>(),
                )
                awaitUntil { calendarMemoList -> calendarMemoList.single().color == tag.detail.color }

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-CALENDAR-MEMO-DATA-003 TC-CALENDAR-MEMO-DATA-006 메모의 기간이 바뀌면 표시 대상 여부가 다시 정해진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = overlappingMemo()
            upsert(accountId, memo)

            calendarMemoFlow(accountId = accountId).test {
                awaitItem().map { calendarMemo -> calendarMemo.id } shouldBe listOf(memo.id)

                memoTransaction.updateDetail(
                    accountId = accountId,
                    memoId = memo.id,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(date = LocalDate(2026, 7, 20), time = Midnight),
                            endInclusive = LocalDateTime(date = LocalDate(2026, 7, 21), time = Midnight),
                        ),
                    updatedAt = instant(),
                )
                awaitUntil { calendarMemoList -> calendarMemoList.isEmpty() }

                memoTransaction.updateDetail(
                    accountId = accountId,
                    memoId = memo.id,
                    detail = memo.detail,
                    updatedAt = instant(),
                )
                awaitUntil { calendarMemoList -> calendarMemoList.map { calendarMemo -> calendarMemo.id } == listOf(memo.id) }

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-CALENDAR-MEMO-DATA-004 메모가 삭제되면 결과에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = overlappingMemo().copy(isDeleted = false)
            upsert(accountId, memo)

            calendarMemoFlow(accountId = accountId).test {
                awaitItem().map { calendarMemo -> calendarMemo.id } shouldBe listOf(memo.id)

                memoTransaction.updateDeleted(
                    accountId = accountId,
                    memoId = memo.id,
                    isDeleted = true,
                    updatedAt = instant(),
                )

                awaitUntil { calendarMemoList -> calendarMemoList.isEmpty() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("초 단위까지 기록된 기간도 날짜만으로 겹침을 판단한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoList =
                listOf(
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 4, 23, 59, 59),
                        endInclusive = LocalDateTime(2026, 7, 4, 23, 59, 59),
                    ) to false,
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 11, 23, 59, 59),
                        endInclusive = LocalDateTime(2026, 7, 12, 0, 0, 1),
                    ) to true,
                    dateTimeMemo(
                        start = LocalDateTime(2026, 7, 12, 0, 0, 1),
                        endInclusive = LocalDateTime(2026, 7, 12, 0, 0, 2),
                    ) to false,
                )
            upsert(accountId, *memoList.map { (memo, _) -> memo }.toTypedArray())

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder
                memoList.filter { (_, isIncluded) -> isIncluded }.map { (memo, _) -> memo.id }
        }

        test("표시 대상 기간과 겹치는 메모가 없으면 빈 목록을 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            upsert(accountId, allDayMemo(start = LocalDate(2026, 8, 1), endInclusive = LocalDate(2026, 8, 2)))

            calendarMemoList(accountId = accountId).shouldBeEmpty()
        }

        test("표시 대상 기간이 하루면 그 날과 겹치는 메모만 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = allDayMemo(start = LocalDate(2026, 7, 5), endInclusive = LocalDate(2026, 7, 6))
            val otherMemo = allDayMemo(start = LocalDate(2026, 7, 7), endInclusive = LocalDate(2026, 7, 8))
            upsert(accountId, memo, otherMemo)

            dataSource
                .get(
                    accountId = accountId,
                    dateRange = LocalDate(2026, 7, 6)..LocalDate(2026, 7, 6),
                ).first()
                .map { calendarMemo -> calendarMemo.id } shouldBe listOf(memo.id)
        }

        suspend fun upsertWithTag(
            accountId: Uuid,
            memo: MemoLocalEntity,
            vararg tagIdList: Uuid,
            isTagDeleted: Boolean = false,
        ) {
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList =
                    tagIdList.map { tagId ->
                        MemoTagLocalEntity(
                            memoId = memo.id,
                            tagId = tagId,
                            isDeleted = isTagDeleted,
                            updatedAt = instant(),
                            createdAt = instant(),
                        )
                    },
            )
        }

        suspend fun selectFilterTag(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            database.calendarFilterTagDao().upsert(
                entity =
                    CalendarFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = tagId,
                    ),
            )
        }

        test("TC-CALENDAR-HOME-FEATURE-049 태그를 필터에 선택하면 그 태그와 연결된 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag().copy(isFinished = false, isDeleted = false)
            val secondTag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(firstTag, secondTag), tagLinkList = emptyList())
            val firstTagMemo = overlappingMemo()
            val secondTagMemo = overlappingMemo()
            upsertWithTag(accountId, firstTagMemo, firstTag.id)
            upsertWithTag(accountId, secondTagMemo, secondTag.id)

            selectFilterTag(accountId = accountId, tagId = firstTag.id)

            calendarMemoIdList(accountId = accountId) shouldBe listOf(firstTagMemo.id)
        }

        test("TC-CALENDAR-HOME-FEATURE-050 여러 태그를 선택하면 하나 이상과 연결된 메모만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag().copy(isFinished = false, isDeleted = false)
            val secondTag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(firstTag, secondTag), tagLinkList = emptyList())
            val firstTagMemo = overlappingMemo()
            val secondTagMemo = overlappingMemo()
            val noTagMemo = overlappingMemo()
            upsertWithTag(accountId, firstTagMemo, firstTag.id)
            upsertWithTag(accountId, secondTagMemo, secondTag.id)
            upsert(accountId, noTagMemo)

            selectFilterTag(accountId = accountId, tagId = firstTag.id)
            selectFilterTag(accountId = accountId, tagId = secondTag.id)

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(firstTagMemo.id, secondTagMemo.id)
        }

        test("TC-CALENDAR-HOME-FEATURE-051 마지막 태그의 선택을 해제하면 모든 메모가 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val tagMemo = overlappingMemo()
            val noTagMemo = overlappingMemo()
            upsertWithTag(accountId, tagMemo, tag.id)
            upsert(accountId, noTagMemo)
            selectFilterTag(accountId = accountId, tagId = tag.id)
            calendarMemoIdList(accountId = accountId) shouldBe listOf(tagMemo.id)

            database.calendarFilterTagDao().delete(accountId = accountId, tagId = tag.id)

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }

        test("TC-CALENDAR-HOME-DOMAIN-003 필터와 일치하면 완료된 메모도 조회하고 삭제된 메모는 조회하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val activeMemo = overlappingMemo().copy(isFinished = false)
            val finishedMemo = overlappingMemo().copy(isFinished = true)
            val deletedMemo = overlappingMemo().copy(isDeleted = true)
            upsertWithTag(accountId, activeMemo, tag.id)
            upsertWithTag(accountId, finishedMemo, tag.id)
            upsertWithTag(accountId, deletedMemo, tag.id)

            selectFilterTag(accountId = accountId, tagId = tag.id)

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(activeMemo.id, finishedMemo.id)
        }

        test("TC-CALENDAR-HOME-DOMAIN-004 선택된 유일한 태그가 선택할 수 없게 되면 모든 메모가 조회된다") {
            suspend fun unselectableCase(makeUnselectable: suspend (accountId: Uuid, tagId: Uuid) -> Unit) {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val tag = tag().copy(isFinished = false, isDeleted = false)
                tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
                val tagMemo = overlappingMemo()
                val noTagMemo = overlappingMemo()
                upsertWithTag(accountId, tagMemo, tag.id)
                upsert(accountId, noTagMemo)
                selectFilterTag(accountId = accountId, tagId = tag.id)
                calendarMemoIdList(accountId = accountId) shouldBe listOf(tagMemo.id)

                makeUnselectable(accountId, tag.id)

                calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
            }

            unselectableCase { accountId, tagId ->
                database.accountTagDao().updateFinished(
                    accountId = accountId,
                    tagId = tagId,
                    isFinished = true,
                    updatedAt = instant(),
                )
            }
            unselectableCase { accountId, tagId ->
                database.accountTagDao().updateDeleted(
                    accountId = accountId,
                    tagId = tagId,
                    isDeleted = true,
                    updatedAt = instant(),
                )
            }
        }

        test("TC-CALENDAR-HOME-DOMAIN-004 선택된 유일한 태그가 현재 계정과 연결되어 있지 않으면 모든 메모가 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = otherAccountId, tagList = listOf(tag), tagLinkList = emptyList())
            val tagMemo = overlappingMemo()
            val noTagMemo = overlappingMemo()
            upsertWithTag(accountId, tagMemo, tag.id)
            upsert(accountId, noTagMemo)

            selectFilterTag(accountId = accountId, tagId = tag.id)

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }

        test("TC-CALENDAR-HOME-DOMAIN-005 태그가 다시 선택할 수 있게 되면 이전 선택으로 다시 필터링된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val tagMemo = overlappingMemo()
            val noTagMemo = overlappingMemo()
            upsertWithTag(accountId, tagMemo, tag.id)
            upsert(accountId, noTagMemo)
            selectFilterTag(accountId = accountId, tagId = tag.id)
            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = true,
                updatedAt = instant(),
            )
            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = false,
                updatedAt = instant(),
            )

            calendarMemoIdList(accountId = accountId) shouldBe listOf(tagMemo.id)
        }

        test("TC-CALENDAR-HOME-DOMAIN-006 메모 목록 필터 선택은 캘린더 메모 조회에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val tagMemo = overlappingMemo()
            val noTagMemo = overlappingMemo()
            upsertWithTag(accountId, tagMemo, tag.id)
            upsert(accountId, noTagMemo)

            database.memoFilterTagDao().upsert(
                entity =
                    MemoFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = tag.id,
                    ),
            )

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }

        test("해제된 메모 태그 연결은 필터 일치로 보지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            val linkedMemo = overlappingMemo()
            val unlinkedMemo = overlappingMemo()
            upsertWithTag(accountId, linkedMemo, tag.id)
            upsertWithTag(accountId, unlinkedMemo, tag.id, isTagDeleted = true)

            selectFilterTag(accountId = accountId, tagId = tag.id)

            calendarMemoIdList(accountId = accountId) shouldBe listOf(linkedMemo.id)
        }

        test("다른 계정의 캘린더 필터 선택은 현재 계정의 조회에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag().copy(isFinished = false, isDeleted = false)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            tagTransaction.upsert(accountId = otherAccountId, tagList = listOf(tag), tagLinkList = emptyList())
            val tagMemo = overlappingMemo()
            val noTagMemo = overlappingMemo()
            upsertWithTag(accountId, tagMemo, tag.id)
            upsert(accountId, noTagMemo)

            selectFilterTag(accountId = otherAccountId, tagId = tag.id)

            calendarMemoIdList(accountId = accountId) shouldContainExactlyInAnyOrder listOf(tagMemo.id, noTagMemo.id)
        }
    }) {
    public companion object {
        private val RANGE_START: LocalDate = LocalDate(2026, 7, 5)
        private val RANGE_END_INCLUSIVE: LocalDate = LocalDate(2026, 7, 11)
        private val Midnight: LocalTime = LocalTime(hour = 0, minute = 0)

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private suspend fun ReceiveTurbine<List<CalendarMemoLocalEntity>>.awaitUntil(predicate: (List<CalendarMemoLocalEntity>) -> Boolean): List<CalendarMemoLocalEntity> {
            while (true) {
                val calendarMemoList = awaitItem()
                if (predicate(calendarMemoList)) return calendarMemoList
            }
        }

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

        private fun overlappingMemo(): MemoLocalEntity = allDayMemo(start = LocalDate(2026, 7, 6), endInclusive = LocalDate(2026, 7, 7))

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
    }
}
