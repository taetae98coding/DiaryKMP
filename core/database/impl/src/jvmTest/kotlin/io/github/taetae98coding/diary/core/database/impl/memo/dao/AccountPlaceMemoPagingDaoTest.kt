package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.memoplace.memoPlaceLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountPlaceMemoPagingDaoTest :
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
                .page(
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

        test("TC-21aab824-34c2-40e0-83cc-c863eb73a142 현재 계정의 대상 장소와 활성 연결을 가진 미완료·미삭제 메모만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val target = place()
            val other = place()
            val activeMemo = memo(isFinished = false, isDeleted = false)
            val finishedMemo = memo(isFinished = true, isDeleted = false)
            val deletedMemo = memo(isFinished = false, isDeleted = true)
            val disconnectedMemo = memo(isFinished = false, isDeleted = false)
            val otherTargetMemo = memo(isFinished = false, isDeleted = false)
            val otherAccountMemo = memo(isFinished = false, isDeleted = false)
            insertPlaceMemo(accountId, target, activeMemo)
            insertPlaceMemo(accountId, target, finishedMemo)
            insertPlaceMemo(accountId, target, deletedMemo)
            insertPlaceMemo(accountId, target, disconnectedMemo, isConnectionDeleted = true)
            insertPlaceMemo(accountId, other, otherTargetMemo)
            insertPlaceMemo(otherAccountId, target, otherAccountMemo)

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(activeMemo.id)
        }

        test("TC-b5ddb49d-bfcf-4ad8-af3b-f39c929391d5 대상 장소에 연결된 완료된 메모는 메모 탭 목록에 포함하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val activeMemo = memo(isFinished = false)
            val finishedMemo = memo(isFinished = true)
            insertPlaceMemo(accountId, target, activeMemo)
            insertPlaceMemo(accountId, target, finishedMemo)

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(activeMemo.id)
        }

        test("TC-d56bb86e-a3fa-4580-901e-654e87cc1a05 대상 장소를 삭제해도 연결된 미완료 메모는 계속 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)

            database.placeDao().upsert(listOf(target.copy(isDeleted = true, updatedAt = instant())))

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-d56bb86e-a3fa-4580-901e-654e87cc1a05 대상 장소의 태그 연결을 바꿔도 연결된 미완료 메모는 계속 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val targetMemo = memo()
            val tagId = Arb.kotlinUuid().next()
            insertPlaceMemo(accountId, target, targetMemo)

            database.placeTagDao().upsert(listOf(placeTag(placeId = target.id, tagId = tagId, isDeleted = false)))
            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)

            database.placeTagDao().upsert(listOf(placeTag(placeId = target.id, tagId = tagId, isDeleted = true)))
            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-d56bb86e-a3fa-4580-901e-654e87cc1a05 대상 장소의 좌표를 수정해도 연결된 미완료 메모는 계속 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)

            database.placeDao().upsert(
                listOf(
                    target.copy(
                        detail = target.detail.copy(latitude = -target.detail.latitude, longitude = -target.detail.longitude),
                        updatedAt = instant(),
                    ),
                ),
            )

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
        }

        test("TC-f7796e3c-6986-4f88-88d5-c35c377e4aa3 장소와 메모가 같은 태그에 연결되어 있어도 장소의 연결된 메모로 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val linkedMemo = memo()
            val sameTagMemo = memo()
            val tagId = Arb.kotlinUuid().next()
            insertPlaceMemo(accountId, target, linkedMemo)
            insertMemo(accountId, sameTagMemo)
            database.placeTagDao().upsert(listOf(placeTag(placeId = target.id, tagId = tagId, isDeleted = false)))
            database.memoTagDao().upsert(
                listOf(
                    MemoTagLocalEntity(
                        memoId = sameTagMemo.id,
                        tagId = tagId,
                        isDeleted = false,
                        updatedAt = instant(),
                        createdAt = instant(),
                    ),
                ),
            )

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(linkedMemo.id)
        }

        test("TC-653af079-5d1f-4943-b644-51fe9f1c4dfe 장소별 메모는 기간 없음, 종일 여부, 시작 시점, 종료 시점, 제목 순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
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

        test("TC-001a9d59-526b-479d-a426-22ba5407db07 TC-c39dbdfa-8f33-408a-ac18-cf1660e92aee 메모를 완료하면 목록에서 사라지고 다시 시작하면 연결이 유지되어 다시 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            val changedAt = instant()
            val revertedAt = instant()

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = emptyList()) {
                database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = true, updatedAt = changedAt)
            }
            database.memoDao().findUpdatedAt(listOf(targetMemo.id)) shouldBe mapOf(targetMemo.id to changedAt)
            database.memoPlaceDao().findByMemoIdList(listOf(targetMemo.id)).map { memoPlace -> memoPlace.isDeleted } shouldBe listOf(false)

            database.accountMemoDao().updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = false, updatedAt = revertedAt)

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
            database.memoDao().findUpdatedAt(listOf(targetMemo.id)) shouldBe mapOf(targetMemo.id to revertedAt)
            database.memoPlaceDao().findByMemoIdList(listOf(targetMemo.id)).map { memoPlace -> memoPlace.isDeleted } shouldBe listOf(false)
        }

        test("TC-001a9d59-526b-479d-a426-22ba5407db07 TC-c39dbdfa-8f33-408a-ac18-cf1660e92aee 메모를 삭제하면 목록에서 사라지고 복구하면 연결이 유지되어 다시 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)
            val pagingSource = pagingSource(accountId = accountId, placeId = target.id)
            pagingSource.pagedIds() shouldBe listOf(targetMemo.id)

            val changedAt = instant()
            val revertedAt = instant()

            assertPageInvalidated(pagingSource = pagingSource, accountId = accountId, placeId = target.id, expectedIds = emptyList()) {
                database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = true, updatedAt = changedAt)
            }
            database.memoDao().findUpdatedAt(listOf(targetMemo.id)) shouldBe mapOf(targetMemo.id to changedAt)
            database.memoPlaceDao().findByMemoIdList(listOf(targetMemo.id)).map { memoPlace -> memoPlace.isDeleted } shouldBe listOf(false)

            database.accountMemoDao().updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = false, updatedAt = revertedAt)

            pagedIds(accountId = accountId, placeId = target.id) shouldBe listOf(targetMemo.id)
            database.memoDao().findUpdatedAt(listOf(targetMemo.id)) shouldBe mapOf(targetMemo.id to revertedAt)
            database.memoPlaceDao().findByMemoIdList(listOf(targetMemo.id)).map { memoPlace -> memoPlace.isDeleted } shouldBe listOf(false)
        }

        test("TC-7280d1c3-ca4b-46fc-9c0c-b10d4f79c7c7 메모의 완료·삭제와 실행 취소는 대상 장소의 내용과 삭제 여부를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val target = place()
            val targetMemo = memo()
            insertPlaceMemo(accountId, target, targetMemo)
            val memoDao = database.accountMemoDao()

            memoDao.updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = true, updatedAt = instant())
            database.accountPlaceDao().find(accountId = accountId, placeId = target.id).first() shouldBe target
            memoDao.updateFinished(accountId = accountId, memoId = targetMemo.id, isFinished = false, updatedAt = instant())
            database.accountPlaceDao().find(accountId = accountId, placeId = target.id).first() shouldBe target
            memoDao.updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = true, updatedAt = instant())
            database.accountPlaceDao().find(accountId = accountId, placeId = target.id).first() shouldBe target
            memoDao.updateDeleted(accountId = accountId, memoId = targetMemo.id, isDeleted = false, updatedAt = instant())
            database.accountPlaceDao().find(accountId = accountId, placeId = target.id).first() shouldBe target
        }

        test("TC-001a9d59-526b-479d-a426-22ba5407db07 연결 해제는 페이지를 무효화하고 메모를 목록에서 제외한다") {
            val accountId = Arb.kotlinUuid().next()
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

        test("TC-001a9d59-526b-479d-a426-22ba5407db07 연결 복구는 페이지를 무효화하고 메모를 목록에 다시 포함한다") {
            val accountId = Arb.kotlinUuid().next()
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

        test("TC-001a9d59-526b-479d-a426-22ba5407db07 기간 변경은 페이지를 무효화하고 메모를 변경된 정렬 위치로 옮긴다") {
            val accountId = Arb.kotlinUuid().next()
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
            id: Uuid = Arb.kotlinUuid().next(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            detail: MemoDetailLocalEntity = Arb.memoDetailLocalEntity().next(),
            updatedAt: Instant = instant(),
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
                    createdAt = instant(),
                )

        private fun place(isDeleted: Boolean = false): PlaceLocalEntity =
            Arb
                .placeLocalEntity()
                .next()
                .copy(
                    isDeleted = isDeleted,
                    updatedAt = instant(),
                    createdAt = instant(),
                )

        private fun memoPlace(
            memoId: Uuid,
            placeId: Uuid,
            isDeleted: Boolean = false,
        ): MemoPlaceLocalEntity =
            Arb
                .memoPlaceLocalEntity()
                .next()
                .copy(
                    memoId = memoId,
                    placeId = placeId,
                    isDeleted = isDeleted,
                    updatedAt = instant(),
                    createdAt = instant(),
                )

        private fun placeTag(
            placeId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean,
        ): PlaceTagLocalEntity =
            PlaceTagLocalEntity(
                placeId = placeId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun instant(): Instant = Arb.secondPrecisionInstant().next()

        private fun detail(
            isAllDay: Boolean?,
            start: LocalDateTime?,
            endInclusive: LocalDateTime?,
            title: String = Arb.string().next(),
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
