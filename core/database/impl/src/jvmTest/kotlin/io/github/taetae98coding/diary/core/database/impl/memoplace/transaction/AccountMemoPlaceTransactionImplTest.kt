package io.github.taetae98coding.diary.core.database.impl.memoplace.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.datasource.AccountMemoPlaceLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.datasource.AccountMemoPlaceSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoPlaceTestException : RuntimeException()

class AccountMemoPlaceTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl
        lateinit var accountMemoPlaceTransaction: AccountMemoPlaceTransactionImpl
        lateinit var syncTransaction: AccountMemoPlaceSyncTransactionImpl
        lateinit var dataSource: AccountMemoPlaceLocalDataSourceImpl
        lateinit var syncDataSource: AccountMemoPlaceSyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            memoTransaction = AccountMemoTransactionImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
            accountMemoPlaceTransaction = AccountMemoPlaceTransactionImpl(database = database)
            syncTransaction = AccountMemoPlaceSyncTransactionImpl(database = database)
            dataSource = AccountMemoPlaceLocalDataSourceImpl(database = database)
            syncDataSource = AccountMemoPlaceSyncLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemoWithPlaceList(
            accountId: Uuid,
            memo: MemoLocalEntity,
            placeList: List<PlaceLocalEntity>,
        ) {
            placeTransaction.upsert(accountId = accountId, placeList = placeList, placeTagList = emptyList())
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoPlaceList = placeList.map { place -> memoPlace(memoId = memo.id, placeId = place.id, memo = memo) },
            )
        }

        suspend fun findMemo(
            accountId: Uuid,
            memoId: Uuid,
        ): MemoLocalEntity? = database.accountMemoDao().find(accountId = accountId, memoId = memoId).first()

        suspend fun findMemoPlaceList(memoId: Uuid): List<MemoPlaceLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT memo_id, place_id, is_deleted, updated_at, created_at
                    FROM memo_place
                    WHERE memo_id = '$memoId'
                    ORDER BY place_id ASC
                    """,
                ) { statement -> statement.readAll { it.toMemoPlace() } }
            }

        suspend fun getPlaceList(
            accountId: Uuid,
            memoId: Uuid,
        ): List<PlaceLocalEntity> = dataSource.getPlaceList(accountId = accountId, memoId = memoId).first()

        suspend fun findPendingPlaceIdList(accountId: Uuid): List<Uuid> =
            syncDataSource
                .findPending(accountId = accountId)
                .map { memoPlace -> memoPlace.placeId }

        suspend fun clearAllPending(accountId: Uuid) {
            syncTransaction.clearPending(
                accountId = accountId,
                memoPlaceList = syncDataSource.findPending(accountId = accountId),
            )
        }

        test("TC-MEMO-PLACE-DOMAIN-001 하나의 메모에 여러 장소를 연결해 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val placeList = List(2) { place() }

            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = placeList)

            getPlaceList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder placeList
        }

        test("TC-MEMO-PLACE-DOMAIN-002 같은 장소를 여러 메모에 연결해도 서로 대체되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstMemo = memo()
            val secondMemo = memo()

            listOf(firstMemo, secondMemo).forEach { memo ->
                insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            }

            getPlaceList(accountId = accountId, memoId = firstMemo.id) shouldBe listOf(place)
            getPlaceList(accountId = accountId, memoId = secondMemo.id) shouldBe listOf(place)
        }

        test("TC-MEMO-PLACE-DOMAIN-003 같은 메모와 장소의 연결을 다시 저장해도 한 건으로 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()

            repeat(2) {
                insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            }

            findMemoPlaceList(memoId = memo.id) shouldHaveSize 1
        }

        test("TC-MEMO-PLACE-DOMAIN-004 연결할 장소가 없으면 장소 연결 없이 메모만 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            findMemoPlaceList(memoId = memo.id).shouldBeEmpty()
            getPlaceList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DOMAIN-005 장소를 삭제해도 메모와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo))
        }

        test("TC-MEMO-PLACE-DOMAIN-006 메모를 완료하거나 삭제해도 장소와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(isFinished = false, isDeleted = false)
            val place = place()
            val memoPlace = memoPlace(memoId = memo.id, placeId = place.id, memo = memo)
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            memoTransaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = true, updatedAt = instant())
            findMemoPlaceList(memoId = memo.id) shouldBe listOf(memoPlace)

            memoTransaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = true, updatedAt = instant())
            findMemoPlaceList(memoId = memo.id) shouldBe listOf(memoPlace)
        }

        test("TC-MEMO-PLACE-DOMAIN-007 메모의 제목·설명·컬러·기간 수정은 장소 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()
            val updatedAt = instant()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            memoTransaction.updateDetail(accountId = accountId, memoId = memo.id, detail = newDetail, updatedAt = updatedAt)

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo.copy(detail = newDetail, updatedAt = updatedAt)
            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo))
            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
        }

        test("TC-MEMO-PLACE-DOMAIN-008 TC-MEMO-DETAIL-DATA-021 연결을 해제하면 조회에서 제외되고 해제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            val removedAt = instant()

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = true, updatedAt = removedAt)

            getPlaceList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = true,
                        updatedAt = removedAt,
                        createdAt = memo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DOMAIN-009 해제된 연결을 다시 만들면 생성 시각을 유지한 채 되살아난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            val createdAt = instant()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            database.memoPlaceDao().upsert(
                MemoPlaceLocalEntity(
                    memoId = memo.id,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = instant(),
                    createdAt = createdAt,
                ),
            )
            val restoredAt = instant()

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = false, updatedAt = restoredAt)

            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = restoredAt,
                        createdAt = createdAt,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DATA-010 연결을 하나 해제하면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val removedPlace = place()
            val keptPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(removedPlace, keptPlace))
            clearAllPending(accountId = accountId)

            accountMemoPlaceTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                placeId = removedPlace.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            findPendingPlaceIdList(accountId = accountId) shouldBe listOf(removedPlace.id)
        }

        test("TC-MEMO-PLACE-DATA-010 연결을 하나 만들면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptPlace = place()
            val addedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(keptPlace))
            placeTransaction.upsert(accountId = accountId, placeList = listOf(addedPlace), placeTagList = emptyList())
            clearAllPending(accountId = accountId)

            accountMemoPlaceTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                placeId = addedPlace.id,
                isDeleted = false,
                updatedAt = instant(),
            )

            findPendingPlaceIdList(accountId = accountId) shouldBe listOf(addedPlace.id)
        }

        test("TC-MEMO-PLACE-DATA-001 저장된 연결의 생성 시각과 수정 시각은 저장 시점으로 서로 같고 해제되지 않은 상태다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val memo = memo().copy(updatedAt = now, createdAt = now)
            val place = place()

            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = now,
                        createdAt = now,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DATA-002 저장이 실패하면 메모와 계정 연결, 태그 연결, 장소 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            val syncDataSource = AccountMemoSyncLocalDataSourceImpl(database = database)
            val failingDatabase = spyk(database)
            every { failingDatabase.memoPlaceDao() } throws MemoPlaceTestException()
            val failingTransaction = AccountMemoTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoPlaceTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                    memoPlaceList = listOf(memoPlace(memoId = memo.id, placeId = place.id, memo = memo)),
                )
            }

            findMemo(accountId = accountId, memoId = memo.id).shouldBeNull()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
            findMemoPlaceList(memoId = memo.id).shouldBeEmpty()
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-PLACE-DATA-003 같은 연결을 다른 수정 시각으로 저장하면 마지막 내용으로 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(updatedAt = instant())
            val place = place()

            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(place))
            insertMemoWithPlaceList(accountId = accountId, memo = changedMemo, placeList = listOf(place))

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = changedMemo.updatedAt,
                        createdAt = changedMemo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-PLACE-DATA-004 해제되지 않은 연결의 장소만 메모의 장소로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptPlace = place()
            val removedPlace = place()
            insertMemoWithPlaceList(accountId = accountId, memo = memo, placeList = listOf(keptPlace, removedPlace))

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = removedPlace.id, isDeleted = true, updatedAt = instant())

            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptPlace)
        }

        test("TC-MEMO-DETAIL-DATA-020 장소를 선택하면 연결이 선택 시점으로 저장되고 메모의 다른 값은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            val updatedAt = instant()

            accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = false, updatedAt = updatedAt)

            findMemoPlaceList(memoId = memo.id) shouldBe
                listOf(
                    MemoPlaceLocalEntity(
                        memoId = memo.id,
                        placeId = place.id,
                        isDeleted = false,
                        updatedAt = updatedAt,
                        createdAt = updatedAt,
                    ),
                )
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
        }

        listOf(
            "완료된" to { memo: MemoLocalEntity -> memo.copy(isFinished = true, isDeleted = false) },
            "삭제된" to { memo: MemoLocalEntity -> memo.copy(isFinished = false, isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-MEMO-DETAIL-DOMAIN-006 $label 메모도 장소 연결을 만들 수 있다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = change(memo())
                val place = place()
                placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
                memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

                accountMemoPlaceTransaction.upsert(accountId = accountId, memoId = memo.id, placeId = place.id, isDeleted = false, updatedAt = instant())

                getPlaceList(accountId = accountId, memoId = memo.id) shouldBe listOf(place)
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun place(): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::detail, placeDetail())
                .setExp(PlaceLocalEntity::isDeleted, false)
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun placeDetail(): PlaceDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetailLocalEntity>()
                .setExp(PlaceDetailLocalEntity::latitude, fixtureMonkey.giveMeOne<Long>() % 90 + 0.5)
                .setExp(PlaceDetailLocalEntity::longitude, fixtureMonkey.giveMeOne<Long>() % 180 + 0.5)
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun memoPlace(
            memoId: Uuid,
            placeId: Uuid,
            memo: MemoLocalEntity,
        ): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = memoId,
                placeId = placeId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
            memo: MemoLocalEntity,
        ): MemoTagLocalEntity =
            MemoTagLocalEntity(
                memoId = memoId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toMemoPlace(): MemoPlaceLocalEntity =
            MemoPlaceLocalEntity(
                memoId = Uuid.parse(getText(0)),
                placeId = Uuid.parse(getText(1)),
                isDeleted = getBoolean(2),
                updatedAt = Instant.fromEpochMilliseconds(getLong(3)),
                createdAt = Instant.fromEpochMilliseconds(getLong(4)),
            )
    }
}
