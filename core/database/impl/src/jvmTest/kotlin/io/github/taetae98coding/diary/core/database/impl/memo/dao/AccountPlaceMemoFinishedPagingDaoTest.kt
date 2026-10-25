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
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountPlaceMemoFinishedPagingDaoTest :
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

        suspend fun insertPlace(
            accountId: Uuid,
            place: PlaceLocalEntity,
        ) {
            database.withWriteTransaction {
                database.placeDao().upsert(listOf(place))
                database.accountPlaceDao().upsert(
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertMemoPlace(
            accountId: Uuid,
            memoPlace: MemoPlaceLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoPlaceDao().upsert(listOf(memoPlace))
                database.accountMemoPlaceDao().upsert(
                    AccountMemoPlaceLocalEntity(
                        accountId = accountId,
                        memoId = memoPlace.memoId,
                        placeId = memoPlace.placeId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun insertPlaceMemo(
            accountId: Uuid,
            place: PlaceLocalEntity,
            memo: MemoLocalEntity,
            isConnectionDeleted: Boolean = false,
        ) {
            insertPlace(accountId = accountId, place = place)
            insertMemo(accountId, memo)
            insertMemoPlace(
                accountId = accountId,
                memoPlace =
                    memoPlace(
                        memoId = memo.id,
                        placeId = place.id,
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
            placeId: Uuid,
        ): PagingSource<Int, MemoLocalEntity> =
            database
                .accountPlaceMemoDao()
                .pageFinished(
                    accountId = accountId,
                    placeId = placeId,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                )

        suspend fun pagedIds(
            accountId: Uuid,
            placeId: Uuid,
        ): List<Uuid> = pagingSource(accountId = accountId, placeId = placeId).pagedIds()

        suspend fun assertPageInvalidated(
            pagingSource: PagingSource<Int, MemoLocalEntity>,
            accountId: Uuid,
            placeId: Uuid,
            expectedIds: List<Uuid>,
            change: suspend () -> Unit,
        ) {
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(30_000) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            pagedIds(accountId = accountId, placeId = placeId) shouldBe expectedIds
        }

        suspend fun activePagedIds(
            accountId: Uuid,
            placeId: Uuid,
        ): List<Uuid> =
            database
                .accountPlaceMemoDao()
                .page(
                    accountId = accountId,
                    placeId = placeId,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIds()

        test(
            "TC-04a10882-eabb-4dc0-a848-eda2ed47ff40 TC-e446b0d4-096a-4328-a3fa-1e26b34bd16f " +
                "현재 계정의 대상 장소와 활성 연결을 가진 완료·미삭제 메모만 조회한다",
        ) {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val other = place()
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedDeletedMemo = memo(isFinished = true, isDeleted = true)
            val disconnectedMemo = memo(isFinished = true, isDeleted = false)
            val otherTargetMemo = memo(isFinished = true, isDeleted = false)
            val otherAccountMemo = memo(isFinished = true, isDeleted = false)
            insertPlaceMemo(accountId, target, finishedMemo)
            insertPlaceMemo(accountId, target, activeMemo)
            insertPlaceMemo(accountId, target, finishedDeletedMemo)
            insertPlaceMemo(accountId, target, disconnectedMemo, isConnectionDeleted = true)
            insertPlaceMemo(accountId, other, otherTargetMemo)
            insertPlaceMemo(otherAccountId, target, otherAccountMemo)

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(finishedMemo.id)
        }

        test("TC-53603dea-4027-4100-b4b0-238877598359 MemoHome의 태그 필터를 골라 두어도 대상 장소의 완료 메모는 그대로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val finishedMemo = memo()
            insertPlaceMemo(accountId, target, finishedMemo)
            database.memoFilterTagDao().upsert(entity = MemoFilterTagLocalEntity(accountId = accountId, tagId = fixtureMonkey.giveMeOne<Uuid>()))

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(finishedMemo.id)
        }

        test("TC-23057b0d-8d8d-4ad7-a6fa-0a4d695be691 대상 장소를 삭제해도 연결된 완료 메모는 계속 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)

            insertPlace(accountId = accountId, place = target.copy(isDeleted = true, updatedAt = instant()))

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-ec85fc8e-d63c-4b35-a381-f4fda3bc12a3 완료된 장소별 메모는 기간 없음, 시작 날짜, 종일 여부, 시작 시각, 종료 시점, 제목 순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
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
            val multiDayAllDayMemo =
                memo(
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
                    detail =
                        detail(
                            title = "Alpha",
                            isAllDay = false,
                            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 1, minute = 0),
                        ),
                )
            listOf(
                nextDayMemo,
                midnightMemo,
                multiDayAllDayMemo,
                sameDayLateEndMemo,
                sameDayEarlyEndBravoMemo,
                sameDayEarlyEndAlphaMemo,
                allDayMemo,
                noDateTimeBravoMemo,
                noDateTimeAlphaMemo,
            ).forEach { targetMemo ->
                insertPlaceMemo(accountId, target, targetMemo)
            }

            pagedIds(accountId = accountId, placeId = target.id) shouldBe
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

        test("TC-10bd10fb-996c-4f1a-90ce-b51ef68fd097 완료한 시점은 목록 순서에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val earlyStartMemo =
                memo(
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
                    detail =
                        detail(
                            isAllDay = true,
                            start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 0, minute = 0),
                        ),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertPlaceMemo(accountId, target, earlyStartMemo)
            insertPlaceMemo(accountId, target, lateStartMemo)

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(earlyStartMemo.id, lateStartMemo.id)
        }

        test(
            "TC-3dcf65cb-40a3-42be-a251-b759b2e322d2 TC-98a51fa0-cf14-4a72-b098-a2100f0e3f0c TC-235ef526-ec6e-465b-a139-06d9cb2573a4 " +
                "메모를 다시 시작하면 완료 목록에서 사라지고 연결이 유지되어 같은 장소의 메모 탭 목록에 나타난다",
        ) {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)
            activePagedIds(accountId = accountId, placeId = target.id).shouldBeEmpty()

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = emptyList()) {
                database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = false, updatedAt = instant())
            }

            activePagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)

            database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = true, updatedAt = instant())

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-98a51fa0-cf14-4a72-b098-a2100f0e3f0c TC-235ef526-ec6e-465b-a139-06d9cb2573a4 메모를 삭제하면 완료 목록에서 사라지고 복구하면 연결이 유지되어 다시 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = emptyList()) {
                database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = true, updatedAt = instant())
            }

            database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = false, updatedAt = instant())

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-91a11878-ec7e-4518-864b-df647754913e 삭제한 메모는 완료 목록과 메모 탭 목록 어디에도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)

            database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = true, updatedAt = instant())

            pagedIds(accountId = accountId, placeId = target.id).shouldBeEmpty()
            activePagedIds(accountId = accountId, placeId = target.id).shouldBeEmpty()
        }

        test("TC-8d945960-ec43-4753-86d7-2a7716a55880 다시 시작·삭제·실행 취소는 상세 대상 장소를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)
            val storedPlace = database.accountPlaceDao().find(accountId = accountId, placeId = target.id).first()
            val memoDao = database.accountMemoDao()

            memoDao.updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = false, updatedAt = instant())
            memoDao.updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = true, updatedAt = instant())
            memoDao.updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = true, updatedAt = instant())
            memoDao.updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = false, updatedAt = instant())

            database.accountPlaceDao().find(accountId = accountId, placeId = target.id).first() shouldBe storedPlace
        }

        test("TC-98a51fa0-cf14-4a72-b098-a2100f0e3f0c 미완료 메모를 완료하면 페이지를 무효화하고 완료 목록에 포함한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo(isFinished = false)
            insertPlaceMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds().shouldBeEmpty()

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = listOf(targetMemo.id)) {
                database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = true, updatedAt = instant())
            }
        }

        test("TC-98a51fa0-cf14-4a72-b098-a2100f0e3f0c 연결 해제는 페이지를 무효화하고 메모를 완료 목록에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            val relation = memoPlace(memoId = targetMemo.id, placeId = target.id)
            insertPlaceMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = emptyList()) {
                database.memoPlaceDao().upsert(listOf(relation.copy(isDeleted = true, updatedAt = instant())))
            }
        }

        test("TC-98a51fa0-cf14-4a72-b098-a2100f0e3f0c 연결 복구는 페이지를 무효화하고 메모를 완료 목록에 다시 포함한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
            val targetMemo = memo()
            val relation = memoPlace(memoId = targetMemo.id, placeId = target.id, isDeleted = true)
            insertPlaceMemo(accountId, target, targetMemo, isConnectionDeleted = true)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds().shouldBeEmpty()

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = listOf(targetMemo.id)) {
                database.memoPlaceDao().upsert(listOf(relation.copy(isDeleted = false, updatedAt = instant())))
            }
        }

        test("TC-98a51fa0-cf14-4a72-b098-a2100f0e3f0c 기간 변경은 페이지를 무효화하고 메모를 변경된 정렬 위치로 옮긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val target = place()
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
            insertPlaceMemo(accountId, target, movingMemo)
            insertPlaceMemo(accountId, target, anchorMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds() shouldBe listOf(movingMemo.id, anchorMemo.id)

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = listOf(anchorMemo.id, movingMemo.id)) {
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
            isFinished: Boolean = true,
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

        private fun place(isDeleted: Boolean = false): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::isDeleted, isDeleted)
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun memoPlace(
            memoId: Uuid,
            placeId: Uuid,
            isDeleted: Boolean = false,
        ): MemoPlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoPlaceLocalEntity>()
                .setExp(MemoPlaceLocalEntity::memoId, memoId)
                .setExp(MemoPlaceLocalEntity::placeId, placeId)
                .setExp(MemoPlaceLocalEntity::isDeleted, isDeleted)
                .setExp(MemoPlaceLocalEntity::updatedAt, instant())
                .setExp(MemoPlaceLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

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
