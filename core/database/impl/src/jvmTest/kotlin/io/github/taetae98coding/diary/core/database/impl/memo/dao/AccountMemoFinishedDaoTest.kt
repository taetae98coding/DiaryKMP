package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
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
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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
                        updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                        createdAt = fixtureMonkey.giveMeOne<Instant>(),
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

        test("TC-MEMO-FINISHED-LIST-FEATURE-001 TC-MEMO-FINISHED-LIST-DATA-001 현재 계정의 완료·미삭제 메모만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedDeletedMemo = memo(isFinished = true, isDeleted = true)
            val activeDeletedMemo = memo(isFinished = false, isDeleted = true)
            val otherAccountFinishedMemo = memo(isFinished = true, isDeleted = false)
            insert(accountId, finishedMemo, activeMemo, finishedDeletedMemo, activeDeletedMemo)
            insert(otherAccountId, otherAccountFinishedMemo)

            finishedIds(accountId) shouldBe listOf(finishedMemo.id)
        }

        test("TC-MEMO-FINISHED-LIST-FEATURE-008 TC-MEMO-FINISHED-LIST-DATA-002 태그 필터 선택을 조회 조건에 넣지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-FINISHED-LIST-DATA-003 목록은 기간, 종일 여부, 시작 시점, 종료 시점, 제목 순으로 정렬해 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-FINISHED-LIST-DATA-007 제목순은 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTitleMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 21)
            val lastTitleMemo = sortMemo(title = "Bravo", updatedAt = 3_000, startDay = 19)
            insert(accountId, lastTitleMemo, firstTitleMemo)

            database
                .accountMemoDao()
                .pageFinished(accountId = accountId, sort = ListSortLocalEntity.TITLE.queryValue)
                .pagedIds() shouldBe listOf(firstTitleMemo.id, lastTitleMemo.id)
        }

        test("TC-MEMO-FINISHED-LIST-DATA-007 최근 수정순은 수정 시각 내림차순으로 조회하고 같으면 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val latestMemo = sortMemo(title = "Zebra", updatedAt = 3_000, startDay = 19)
            val sameUpdatedFirstMemo = sortMemo(title = "Alpha", updatedAt = 1_000, startDay = 21)
            val sameUpdatedLastMemo = sortMemo(title = "Bravo", updatedAt = 1_000, startDay = 20)
            insert(accountId, sameUpdatedLastMemo, latestMemo, sameUpdatedFirstMemo)

            database
                .accountMemoDao()
                .pageFinished(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED.queryValue)
                .pagedIds() shouldBe listOf(latestMemo.id, sameUpdatedFirstMemo.id, sameUpdatedLastMemo.id)
        }

        test("TC-MEMO-FINISHED-LIST-DOMAIN-007 완료 시점은 목록 순서에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-FINISHED-LIST-DATA-004 상태와 기간 변경이 열어 둔 완료된 메모 목록에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-FINISHED-LIST-DATA-006 상태 변경은 현재 계정과 연결된 메모에만 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("삭제를 되돌린 완료 메모는 자신의 시작 시점에 해당하는 자리에 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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
        private const val INVALIDATION_TIMEOUT_MILLIS = 5_000L

        private fun memo(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            updatedAt: Instant = fixtureMonkey.giveMeOne<Instant>(),
            detail: MemoDetailLocalEntity = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::id, id)
                .setExp(MemoLocalEntity::detail, detail)
                .setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, updatedAt)
                .setExp(MemoLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

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
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>())
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(TagLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun detail(
            title: String = fixtureMonkey.giveMeOne<String>(),
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
        ): MemoDetailLocalEntity =
            fixtureMonkey
                .giveMeOne<MemoDetailLocalEntity>()
                .copy(
                    title = title,
                    isAllDay = isAllDay,
                    start = start,
                    endInclusive = endInclusive,
                )
    }
}
