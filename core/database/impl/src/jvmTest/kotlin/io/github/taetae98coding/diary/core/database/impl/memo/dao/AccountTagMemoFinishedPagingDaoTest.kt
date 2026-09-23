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
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountTagMemoFinishedPagingDaoTest :
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

        suspend fun insertMemo(
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

        suspend fun insertTag(
            accountId: Uuid,
            tag: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(listOf(tag))
                database.accountTagDao().upsert(
                    AccountTagLocalEntity(
                        accountId = accountId,
                        tagId = tag.id,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertMemoTag(
            accountId: Uuid,
            memoTag: MemoTagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoTagDao().upsert(listOf(memoTag))
                database.accountMemoTagDao().upsert(
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoTag.memoId,
                        tagId = memoTag.tagId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertTagMemo(
            accountId: Uuid,
            tag: TagLocalEntity,
            memo: MemoLocalEntity,
            isConnectionDeleted: Boolean = false,
        ) {
            insertTag(accountId = accountId, tag = tag)
            insertMemo(accountId, memo)
            insertMemoTag(
                accountId = accountId,
                memoTag =
                    memoTag(
                        memoId = memo.id,
                        tagId = tag.id,
                        isDeleted = isConnectionDeleted,
                    ),
            )
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

        suspend fun finishedTagPagedIds(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            database
                .accountTagMemoDao()
                .pageFinished(
                    accountId = accountId,
                    tagId = tagId,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIds()

        suspend fun activeTagPagedIds(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            database
                .accountTagMemoDao()
                .page(
                    accountId = accountId,
                    tagId = tagId,
                    scope = TagScopeLocalEntity.SELF.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIds()

        suspend fun assertFinishedTagPageInvalidated(
            pagingSource: PagingSource<Int, MemoLocalEntity>,
            accountId: Uuid,
            tagId: Uuid,
            expectedIds: List<Uuid>,
            change: suspend () -> Unit,
        ) {
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(5_000) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            finishedTagPagedIds(accountId = accountId, tagId = tagId) shouldBe expectedIds
        }

        test(
            "TC-TAG-MEMO-FINISHED-LIST-FEATURE-001 TC-TAG-MEMO-FINISHED-LIST-DATA-001 " +
                "현재 계정의 대상 태그와 활성 연결을 가진 완료·미삭제 메모만 조회한다",
        ) {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val otherTag = tag()
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedDeletedMemo = memo(isFinished = true, isDeleted = true)
            val disconnectedFinishedMemo = memo(isFinished = true, isDeleted = false)
            val otherTagFinishedMemo = memo(isFinished = true, isDeleted = false)
            val otherAccountFinishedMemo = memo(isFinished = true, isDeleted = false)
            insertTagMemo(accountId, targetTag, finishedMemo)
            insertTagMemo(accountId, targetTag, activeMemo)
            insertTagMemo(accountId, targetTag, finishedDeletedMemo)
            insertTagMemo(accountId, targetTag, disconnectedFinishedMemo, isConnectionDeleted = true)
            insertTagMemo(accountId, otherTag, otherTagFinishedMemo)
            insertTagMemo(otherAccountId, targetTag, otherAccountFinishedMemo)

            finishedTagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe listOf(finishedMemo.id)
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DOMAIN-001 태그가 완료되거나 삭제되어도 연결된 완료 메모는 계속 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetList =
                listOf(
                    tag(isFinished = true, isDeleted = false) to memo(isFinished = true),
                    tag(isFinished = false, isDeleted = true) to memo(isFinished = true),
                )
            targetList.forEach { (targetTag, targetMemo) ->
                insertTagMemo(accountId, targetTag, targetMemo)
            }

            targetList.forEach { (targetTag, targetMemo) ->
                finishedTagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetMemo.id)
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-002 완료된 태그별 메모는 기간 없음, 시작 시점, 종료 시점, 제목 순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val noDateTimeBravoMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Bravo",
                            isAllDay = null,
                            start = null,
                            endInclusive = null,
                        ),
                )
            val noDateTimeAlphaMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = null,
                            start = null,
                            endInclusive = null,
                        ),
                )
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
            listOf(
                nextDayMemo,
                sameDayLateEndMemo,
                sameDayEarlyEndBravoMemo,
                sameDayEarlyEndAlphaMemo,
                allDayMemo,
                noDateTimeBravoMemo,
                noDateTimeAlphaMemo,
            ).forEach { targetMemo ->
                insertTagMemo(accountId, targetTag, targetMemo)
            }

            finishedTagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe
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

        test("TC-TAG-MEMO-FINISHED-LIST-DOMAIN-003 완료한 시점은 목록 순서에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
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
            insertTagMemo(accountId, targetTag, earlyStartMemo)
            insertTagMemo(accountId, targetTag, lateStartMemo)

            finishedTagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe listOf(earlyStartMemo.id, lateStartMemo.id)
        }

        test(
            "TC-TAG-MEMO-FINISHED-LIST-DOMAIN-004 TC-TAG-MEMO-FINISHED-LIST-DATA-003 " +
                "메모를 다시 시작하면 완료 목록에서 사라지고 같은 태그의 미완료 목록에 나타난다",
        ) {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetMemo = memo(isFinished = true)
            insertTagMemo(accountId, targetTag, targetMemo)
            val pagingSource = database.accountTagMemoDao().pageFinished(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)
            activeTagPagedIds(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()

            assertFinishedTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = emptyList(),
            ) {
                database.accountMemoDao().updateFinished(
                    accountId = accountId,
                    memoId = targetMemo.id,
                    isFinished = false,
                    updatedAt = instant(),
                )
            }

            activeTagPagedIds(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-003 메모 삭제는 페이지를 무효화하고 메모를 완료 목록에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetMemo = memo(isFinished = true)
            insertTagMemo(accountId, targetTag, targetMemo)
            val pagingSource = database.accountTagMemoDao().pageFinished(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertFinishedTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = emptyList(),
            ) {
                database.accountMemoDao().updateDeleted(
                    accountId = accountId,
                    memoId = targetMemo.id,
                    isDeleted = true,
                    updatedAt = instant(),
                )
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-003 미완료 메모를 완료하면 페이지를 무효화하고 완료 목록에 포함한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetMemo = memo(isFinished = false)
            insertTagMemo(accountId, targetTag, targetMemo)
            val pagingSource = database.accountTagMemoDao().pageFinished(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds().shouldBeEmpty()

            assertFinishedTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = listOf(targetMemo.id),
            ) {
                database.accountMemoDao().updateFinished(
                    accountId = accountId,
                    memoId = targetMemo.id,
                    isFinished = true,
                    updatedAt = instant(),
                )
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-003 연결 해제는 페이지를 무효화하고 메모를 완료 목록에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetMemo = memo(isFinished = true)
            val relation = memoTag(memoId = targetMemo.id, tagId = targetTag.id)
            insertTagMemo(accountId, targetTag, targetMemo)
            val pagingSource = database.accountTagMemoDao().pageFinished(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertFinishedTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = emptyList(),
            ) {
                database.memoTagDao().upsert(
                    listOf(
                        relation.copy(
                            isDeleted = true,
                            updatedAt = instant(),
                        ),
                    ),
                )
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-003 연결 복구는 페이지를 무효화하고 메모를 완료 목록에 다시 포함한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetMemo = memo(isFinished = true)
            val relation = memoTag(memoId = targetMemo.id, tagId = targetTag.id, isDeleted = true)
            insertTagMemo(accountId, targetTag, targetMemo, isConnectionDeleted = true)
            val pagingSource = database.accountTagMemoDao().pageFinished(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds().shouldBeEmpty()

            assertFinishedTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = listOf(targetMemo.id),
            ) {
                database.memoTagDao().upsert(
                    listOf(
                        relation.copy(
                            isDeleted = false,
                            updatedAt = instant(),
                        ),
                    ),
                )
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-003 기간 변경은 페이지를 무효화하고 메모를 변경된 정렬 위치로 옮긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val movingMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val anchorMemo =
                memo(
                    isFinished = true,
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                )
            val movedDetail =
                detail(
                    isAllDay = true,
                    start = LocalDateTime(year = 2026, month = 7, day = 21, hour = 0, minute = 0),
                    endInclusive = LocalDateTime(year = 2026, month = 7, day = 21, hour = 0, minute = 0),
                )
            insertTagMemo(accountId, targetTag, movingMemo)
            insertTagMemo(accountId, targetTag, anchorMemo)
            val pagingSource = database.accountTagMemoDao().pageFinished(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.pagedIds() shouldBe listOf(movingMemo.id, anchorMemo.id)

            assertFinishedTagPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                tagId = targetTag.id,
                expectedIds = listOf(anchorMemo.id, movingMemo.id),
            ) {
                database.accountMemoDao().updateDetail(
                    accountId = accountId,
                    memoId = movingMemo.id,
                    title = movedDetail.title,
                    description = movedDetail.description,
                    color = movedDetail.color,
                    isAllDay = movedDetail.isAllDay,
                    start = movedDetail.start,
                    endInclusive = movedDetail.endInclusive,
                    updatedAt = instant(),
                )
            }
        }
    }) {
    public companion object {
        private fun memo(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            detail: MemoDetailLocalEntity = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
            updatedAt: Instant = instant(),
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::id, id)
                .setExp(MemoLocalEntity::detail, detail)
                .setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, updatedAt)
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun tag(
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ): MemoTagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoTagLocalEntity>()
                .setExp(MemoTagLocalEntity::memoId, memoId)
                .setExp(MemoTagLocalEntity::tagId, tagId)
                .setExp(MemoTagLocalEntity::isDeleted, isDeleted)
                .setExp(MemoTagLocalEntity::updatedAt, instant())
                .setExp(MemoTagLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun detail(
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
            title: String = fixtureMonkey.giveMeOne<String>(),
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
