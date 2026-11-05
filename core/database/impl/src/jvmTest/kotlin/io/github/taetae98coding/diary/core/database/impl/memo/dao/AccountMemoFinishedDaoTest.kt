package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMemoFinishedDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
        }

        afterTest {
            database.close()
        }

        suspend fun insert(
            accountId: Uuid,
            vararg memoList: MemoLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(memoList.toList())
                database.accountMemoDao().upsert(
                    memoList.map { memo ->
                        AccountMemoLocalEntity(
                            accountId = accountId,
                            memoId = memo.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun PagingSource<Int, MemoLocalEntity>.pagedIds(): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 100,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>().data.map { memo -> memo.id }
        }

        suspend fun finishedIds(accountId: Uuid): List<Uuid> = database.accountMemoDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue).pagedIds()

        // 목록 화면은 열어 둔 조회가 무효화되면 스스로 다시 조회하므로, 새로 요청하지 않아도 반영된다는 것을 무효화로 확인한다.
        suspend fun assertOpenedPageInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = database.accountMemoDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(tagList.toList())
                database.accountTagDao().upsert(
                    tagList.map { tag ->
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun linkTag(
            accountId: Uuid,
            memoId: Uuid,
            tagId: Uuid,
        ) {
            database.withWriteTransaction {
                database.memoTagDao().upsert(
                    MemoTagLocalEntity(
                        memoId = memoId,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = Arb.secondPrecisionInstant().next(),
                        createdAt = Arb.secondPrecisionInstant().next(),
                    ),
                )
                database.accountMemoTagDao().upsert(
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoId,
                        tagId = tagId,
                        isDirty = true,
                    ),
                )
            }
        }

        test("TC-7f556861-2470-49ec-bae0-d41c58079f0f TC-a0437ed6-af73-40b1-9476-537e4aae6df0 현재 계정의 완료·미삭제 메모만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedDeletedMemo = memo(isFinished = true, isDeleted = true)
            val activeDeletedMemo = memo(isFinished = false, isDeleted = true)
            val otherAccountFinishedMemo = memo(isFinished = true, isDeleted = false)
            insert(accountId, finishedMemo, activeMemo, finishedDeletedMemo, activeDeletedMemo)
            insert(otherAccountId, otherAccountFinishedMemo)

            finishedIds(accountId) shouldBe listOf(finishedMemo.id)
        }

        test("TC-7d7b6970-a186-4ddb-93ed-44bc4f9cc789 TC-0a5654f9-bf77-4a13-a609-e635dc4e57c6 태그 필터 선택을 조회 조건에 넣지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            val taggedMemo = memo(isFinished = true)
            val noTagMemo = memo(isFinished = true)
            insert(accountId, taggedMemo, noTagMemo)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, memoId = taggedMemo.id, tagId = tag.id)

            database.memoFilterTagDao().upsert(
                entity =
                    MemoFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = tag.id,
                    ),
            )

            finishedIds(accountId) shouldContainExactlyInAnyOrder listOf(taggedMemo.id, noTagMemo.id)
        }

        test("TC-a634b384-f9f7-450f-8492-d1ceeda37c61 목록은 기간, 종일 여부, 시작 시점, 종료 시점, 제목 순으로 정렬해 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val noDateTimeBravoMemo = memo(isFinished = true, detail = detail(title = "Bravo", isAllDay = null, start = null, endInclusive = null))
            val noDateTimeAlphaMemo = memo(isFinished = true, detail = detail(title = "Alpha", isAllDay = null, start = null, endInclusive = null))
            val allDayMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Charlie",
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val sameDayEarlyEndBravoMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Bravo",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                        ),
                )
            val sameDayEarlyEndAlphaMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 0),
                        ),
                )
            val sameDayLateEndMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Delta",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 10, minute = 0),
                        ),
                )
            val nextDayMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Echo",
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                )
            val multiDayAllDayMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 21, hour = 0, minute = 0),
                        ),
                )
            val midnightMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 1, minute = 0),
                        ),
                )
            insert(
                accountId,
                nextDayMemo,
                midnightMemo,
                multiDayAllDayMemo,
                sameDayLateEndMemo,
                sameDayEarlyEndBravoMemo,
                sameDayEarlyEndAlphaMemo,
                allDayMemo,
                noDateTimeBravoMemo,
                noDateTimeAlphaMemo,
            )

            finishedIds(accountId) shouldBe
                listOf(
                    noDateTimeAlphaMemo.id,
                    noDateTimeBravoMemo.id,
                    allDayMemo.id,
                    multiDayAllDayMemo.id,
                    midnightMemo.id,
                    sameDayEarlyEndAlphaMemo.id,
                    sameDayEarlyEndBravoMemo.id,
                    sameDayLateEndMemo.id,
                    nextDayMemo.id,
                )
        }

        test("TC-3f0bc0d0-f056-46ee-bf0b-beabce241cfa 제목순은 제목 오름차순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val firstTitleMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 21)
            val lastTitleMemo = sortMemo(title = "Bravo", updatedAt = 3_000, startDay = 19)
            insert(accountId, lastTitleMemo, firstTitleMemo)

            database
                .accountMemoDao()
                .pageFinished(accountId = accountId, sort = ListSortLocalEntity.TITLE.queryValue)
                .pagedIds() shouldBe listOf(firstTitleMemo.id, lastTitleMemo.id)
        }

        test("TC-3f0bc0d0-f056-46ee-bf0b-beabce241cfa 최근 수정순은 수정 시각 내림차순으로 조회하고 같으면 제목 오름차순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val latestMemo = sortMemo(title = "Zebra", updatedAt = 3_000, startDay = 19)
            val sameUpdatedFirstMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 21)
            val sameUpdatedLastMemo = sortMemo(title = "Bravo", updatedAt = 1_000, startDay = 20)
            insert(accountId, sameUpdatedLastMemo, latestMemo, sameUpdatedFirstMemo)

            database
                .accountMemoDao()
                .pageFinished(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED.queryValue)
                .pagedIds() shouldBe listOf(latestMemo.id, sameUpdatedFirstMemo.id, sameUpdatedLastMemo.id)
        }

        test("TC-84fb2122-71fc-494b-83cb-49680a8cea0f 완료 시점은 목록 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val earlyStartMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                    updatedAt = Instant.fromEpochMilliseconds(3_000),
                )
            val lateStartMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insert(accountId, earlyStartMemo, lateStartMemo)

            finishedIds(accountId) shouldBe listOf(earlyStartMemo.id, lateStartMemo.id)
        }

        test("TC-a8e70d33-4184-47b5-9599-2667d5f536c2 상태와 기간 변경이 열어 둔 완료된 메모 목록에 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val firstMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 19)
            val secondMemo = sortMemo(title = "Bravo", updatedAt = 1_000, startDay = 21)
            val activeMemo = sortMemo(title = "Charlie", updatedAt = 1_000, startDay = 20).copy(isFinished = false)
            insert(accountId, firstMemo, secondMemo, activeMemo)
            finishedIds(accountId) shouldBe listOf(firstMemo.id, secondMemo.id)

            assertOpenedPageInvalidated(accountId) {
                database.accountMemoDao().updateFinished(
                    accountId = accountId,
                    memoId = activeMemo.id,
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                ) shouldBe 1
            }
            finishedIds(accountId) shouldBe listOf(firstMemo.id, activeMemo.id, secondMemo.id)

            assertOpenedPageInvalidated(accountId) {
                database.accountMemoDao().updateDetail(
                    accountId = accountId,
                    memoId = secondMemo.id,
                    title = secondMemo.detail.title,
                    description = secondMemo.detail.description,
                    color = secondMemo.detail.color,
                    isAllDay = true,
                    start = LocalDateTime(year = 2026, month = 7, day = 18, hour = 0, minute = 0),
                    endInclusive = LocalDateTime(year = 2026, month = 7, day = 18, hour = 0, minute = 0),
                    updatedAt = Instant.fromEpochMilliseconds(3_000),
                ) shouldBe 1
            }
            finishedIds(accountId) shouldBe listOf(secondMemo.id, firstMemo.id, activeMemo.id)

            assertOpenedPageInvalidated(accountId) {
                database.accountMemoDao().updateFinished(
                    accountId = accountId,
                    memoId = firstMemo.id,
                    isFinished = false,
                    updatedAt = Instant.fromEpochMilliseconds(4_000),
                ) shouldBe 1
            }
            finishedIds(accountId) shouldBe listOf(secondMemo.id, activeMemo.id)

            assertOpenedPageInvalidated(accountId) {
                database.accountMemoDao().updateDeleted(
                    accountId = accountId,
                    memoId = activeMemo.id,
                    isDeleted = true,
                    updatedAt = Instant.fromEpochMilliseconds(5_000),
                ) shouldBe 1
            }
            finishedIds(accountId) shouldBe listOf(secondMemo.id)
        }

        test("TC-c69dd675-54c0-4925-babc-5824a84279dc 상태 변경은 현재 계정과 연결된 메모에만 적용된다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val memo = memo(isFinished = true)
            insert(accountId, memo)

            database.accountMemoDao().updateFinished(
                accountId = otherAccountId,
                memoId = memo.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            finishedIds(accountId) shouldBe listOf(memo.id)
        }

        test("TC-21e15768-6c0a-4a48-afd6-51fb9b1e4d74 완료 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = Arb.kotlinUuid().next()
            val expectedMemoList =
                List(MEMO_COUNT) { index ->
                    memo(
                        isFinished = true,
                        detail = detail(title = "Memo-${index.toString().padStart(length = 3, padChar = '0')}", isAllDay = null, start = null, endInclusive = null),
                    )
                }
            insert(accountId, *expectedMemoList.reversed().toTypedArray())
            val pagingSource = database.accountMemoDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)

            val firstPage =
                pagingSource
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = PAGE_SIZE, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>()

            firstPage.data.map { memo -> memo.id } shouldBe expectedMemoList.take(PAGE_SIZE).map { memo -> memo.id }
            firstPage.nextKey shouldBe PAGE_SIZE

            val secondPage =
                pagingSource
                    .load(PagingSource.LoadParams.Append(key = requireNotNull(firstPage.nextKey), loadSize = PAGE_SIZE, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>()

            secondPage.data.map { memo -> memo.id } shouldBe expectedMemoList.drop(PAGE_SIZE).take(PAGE_SIZE).map { memo -> memo.id }
        }

        test("삭제를 되돌린 완료 메모는 자신의 시작 시점에 해당하는 자리에 다시 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val firstMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val secondMemo =
                memo(
                    isFinished = true,
                    isDeleted = true,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                )
            insert(accountId, firstMemo, secondMemo)

            database.accountMemoDao().updateDeleted(
                accountId = accountId,
                memoId = secondMemo.id,
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            )

            finishedIds(accountId) shouldBe listOf(firstMemo.id, secondMemo.id)
        }
    }) {
    public companion object {
        private const val INVALIDATION_TIMEOUT_MILLIS = 30_000L
        private const val MEMO_COUNT = 7
        private const val PAGE_SIZE = 3

        private fun memo(
            id: Uuid = Arb.kotlinUuid().next(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
            detail: MemoDetailLocalEntity = Arb.memoDetailLocalEntity().next(),
        ): MemoLocalEntity =
            Arb
                .memoLocalEntity()
                .next()
                .copy(
                    id = id,
                    detail = detail,
                    isFinished = isFinished,
                    isDeleted = isDeleted,
                    updatedAt = updatedAt,
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun sortMemo(
            title: String,
            updatedAt: Long,
            startDay: Int,
        ): MemoLocalEntity =
            memo(
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(updatedAt),
                detail =
                    detail(
                        title = title,
                        isAllDay = true,
                        start = LocalDateTime(year = 2026, month = 7, day = startDay, hour = 0, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 7, day = startDay, hour = 0, minute = 0),
                    ),
            )

        private fun tag(): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(
                    id = Arb.kotlinUuid().next(),
                    detail = Arb.tagDetailLocalEntity().next(),
                    isFinished = false,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun detail(
            title: String = Arb.string().next(),
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
        ): MemoDetailLocalEntity =
            Arb
                .memoDetailLocalEntity()
                .next()
                .copy(
                    title = title,
                    isAllDay = isAllDay,
                    start = start,
                    endInclusive = endInclusive,
                )
    }
}
