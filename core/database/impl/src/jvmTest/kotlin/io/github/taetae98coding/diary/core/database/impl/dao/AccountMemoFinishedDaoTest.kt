package io.github.taetae98coding.diary.core.database.impl.dao

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
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
                        updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                        createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
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

        test("TC-MEMO-FINISHED-LIST-DATA-003 목록은 기간, 시작 시점, 종료 시점, 제목 순으로 정렬해 조회한다") {
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
            insert(
                accountId,
                nextDayMemo,
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
                    sameDayEarlyEndAlphaMemo.id,
                    sameDayEarlyEndBravoMemo.id,
                    sameDayLateEndMemo.id,
                    nextDayMemo.id,
                )
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

        test("TC-MEMO-FINISHED-LIST-DATA-004 상태와 기간 변경이 완료된 메모 목록 조회 결과에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedMemo = memo(isFinished = true)
            val activeMemo = memo(isFinished = false)
            insert(accountId, finishedMemo, activeMemo)
            finishedIds(accountId) shouldBe listOf(finishedMemo.id)

            database.accountMemoDao().updateFinished(
                accountId = accountId,
                memoId = finishedMemo.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1
            finishedIds(accountId).shouldBeEmpty()

            database.accountMemoDao().updateFinished(
                accountId = accountId,
                memoId = activeMemo.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            ) shouldBe 1
            finishedIds(accountId) shouldBe listOf(activeMemo.id)

            database.accountMemoDao().updateDeleted(
                accountId = accountId,
                memoId = activeMemo.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(4_000),
            ) shouldBe 1
            finishedIds(accountId).shouldBeEmpty()
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
        private fun memo(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            updatedAt: Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            detail: MemoDetailLocalEntity = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::id, id)
                .setExp(MemoLocalEntity::detail, detail)
                .setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, updatedAt)
                .setExp(MemoLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, fixtureMonkey.giveMeOne<Uuid>())
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>())
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
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
