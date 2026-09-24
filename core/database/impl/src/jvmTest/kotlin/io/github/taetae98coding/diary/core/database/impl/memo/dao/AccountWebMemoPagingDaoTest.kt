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
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
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

class AccountWebMemoPagingDaoTest :
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

        suspend fun insertWeb(
            accountId: Uuid,
            web: WebLocalEntity,
        ) {
            database.withWriteTransaction {
                database.webDao().upsert(listOf(web))
                database.accountWebDao().upsert(
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertMemoWeb(
            accountId: Uuid,
            memoWeb: MemoWebLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoWebDao().upsert(listOf(memoWeb))
                database.accountMemoWebDao().upsert(
                    AccountMemoWebLocalEntity(
                        accountId = accountId,
                        memoId = memoWeb.memoId,
                        webId = memoWeb.webId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertWebMemo(
            accountId: Uuid,
            web: WebLocalEntity,
            memo: MemoLocalEntity,
            isConnectionDeleted: Boolean = false,
        ) {
            insertWeb(accountId = accountId, web = web)
            insertMemo(accountId, memo)
            insertMemoWeb(
                accountId = accountId,
                memoWeb =
                    memoWeb(
                        memoId = memo.id,
                        webId = web.id,
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

        fun pagingSource(
            accountId: Uuid,
            webId: Uuid,
        ): PagingSource<Int, MemoLocalEntity> =
            database
                .accountWebMemoDao()
                .page(
                    accountId = accountId,
                    webId = webId,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                )

        suspend fun pagedIds(
            accountId: Uuid,
            webId: Uuid,
        ): List<Uuid> = pagingSource(accountId = accountId, webId = webId).pagedIds()

        suspend fun assertPageInvalidated(
            pagingSource: PagingSource<Int, MemoLocalEntity>,
            accountId: Uuid,
            webId: Uuid,
            expectedIds: List<Uuid>,
            change: suspend () -> Unit,
        ) {
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(5_000) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            pagedIds(accountId = accountId, webId = webId) shouldBe expectedIds
        }

        test("TC-WEB-DETAIL-MEMO-DATA-001 현재 계정의 대상 웹 항목와 활성 연결을 가진 미완료·미삭제 메모만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val other = web()
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val deletedMemo = memo(isFinished = false, isDeleted = true)
            val disconnectedMemo = memo(isFinished = false, isDeleted = false)
            val otherTargetMemo = memo(isFinished = false, isDeleted = false)
            val otherAccountMemo = memo(isFinished = false, isDeleted = false)
            insertWebMemo(accountId, target, activeMemo)
            insertWebMemo(accountId, target, finishedMemo)
            insertWebMemo(accountId, target, deletedMemo)
            insertWebMemo(accountId, target, disconnectedMemo, isConnectionDeleted = true)
            insertWebMemo(accountId, other, otherTargetMemo)
            insertWebMemo(otherAccountId, target, otherAccountMemo)

            pagedIds(accountId = accountId, webId = target.id) shouldBe listOf(activeMemo.id)
        }

        test("TC-WEB-DETAIL-MEMO-DOMAIN-001 대상 웹 항목가 삭제되어도 연결된 미완료 메모는 계속 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web(isDeleted = true)
            val targetMemo = memo()
            insertWebMemo(accountId, target, targetMemo)

            pagedIds(accountId = accountId, webId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-WEB-DETAIL-MEMO-DATA-002 웹 항목별 메모는 기간 없음, 시작 시점, 종료 시점, 제목 순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val noDateTimeBravoMemo = memo(detail = detail(title = "Bravo", isAllDay = null, start = null, endInclusive = null))
            val noDateTimeAlphaMemo = memo(detail = detail(title = "Alpha", isAllDay = null, start = null, endInclusive = null))
            val allDayMemo =
                memo(
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
                insertWebMemo(accountId, target, targetMemo)
            }

            pagedIds(accountId = accountId, webId = target.id) shouldBe
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

        test("TC-WEB-DETAIL-MEMO-DATA-003 TC-WEB-DETAIL-MEMO-DATA-004 메모를 완료하면 목록에서 사라지고 다시 시작하면 연결이 유지되어 다시 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val targetMemo = memo()
            insertWebMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, webId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, webId = target.id, expectedIds = emptyList()) {
                database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = true, updatedAt = instant())
            }

            database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = false, updatedAt = instant())

            pagedIds(accountId = accountId, webId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-WEB-DETAIL-MEMO-DATA-003 TC-WEB-DETAIL-MEMO-DATA-004 메모를 삭제하면 목록에서 사라지고 복구하면 연결이 유지되어 다시 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val targetMemo = memo()
            insertWebMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, webId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, webId = target.id, expectedIds = emptyList()) {
                database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = true, updatedAt = instant())
            }

            database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = false, updatedAt = instant())

            pagedIds(accountId = accountId, webId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-WEB-DETAIL-MEMO-DATA-003 연결 해제는 페이지를 무효화하고 메모를 목록에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val targetMemo = memo()
            val relation = memoWeb(memoId = targetMemo.id, webId = target.id)
            insertWebMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, webId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, webId = target.id, expectedIds = emptyList()) {
                database.memoWebDao().upsert(listOf(relation.copy(isDeleted = true, updatedAt = instant())))
            }
        }

        test("TC-WEB-DETAIL-MEMO-DATA-003 연결 복구는 페이지를 무효화하고 메모를 목록에 다시 포함한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val targetMemo = memo()
            val relation = memoWeb(memoId = targetMemo.id, webId = target.id, isDeleted = true)
            insertWebMemo(accountId, target, targetMemo, isConnectionDeleted = true)
            val pagingSource = pagingSource(accountId = accountId, webId = target.id)
            pagingSource.pagedIds().shouldBeEmpty()

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, webId = target.id, expectedIds = listOf(targetMemo.id)) {
                database.memoWebDao().upsert(listOf(relation.copy(isDeleted = false, updatedAt = instant())))
            }
        }

        test("TC-WEB-DETAIL-MEMO-DATA-003 기간 변경은 페이지를 무효화하고 메모를 변경된 정렬 위치로 옮긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = web()
            val movingMemo =
                memo(
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                        ),
                )
            val anchorMemo =
                memo(
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
            insertWebMemo(accountId, target, movingMemo)
            insertWebMemo(accountId, target, anchorMemo)
            val pagingSource = pagingSource(accountId = accountId, webId = target.id)
            pagingSource.pagedIds() shouldBe listOf(movingMemo.id, anchorMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, webId = target.id, expectedIds = listOf(anchorMemo.id, movingMemo.id)) {
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

        private fun web(isDeleted: Boolean = false): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::isDeleted, isDeleted)
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun memoWeb(
            memoId: Uuid,
            webId: Uuid,
            isDeleted: Boolean = false,
        ): MemoWebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoWebLocalEntity>()
                .setExp(MemoWebLocalEntity::memoId, memoId)
                .setExp(MemoWebLocalEntity::webId, webId)
                .setExp(MemoWebLocalEntity::isDeleted, isDeleted)
                .setExp(MemoWebLocalEntity::updatedAt, instant())
                .setExp(MemoWebLocalEntity::createdAt, instant())
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
