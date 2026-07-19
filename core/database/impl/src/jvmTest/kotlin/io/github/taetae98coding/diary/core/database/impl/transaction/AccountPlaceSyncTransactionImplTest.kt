package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.datasource.AccountPlaceSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class PlaceSyncTestException : RuntimeException()

class AccountPlaceSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountPlaceSyncTransactionImpl
        lateinit var syncDataSource: AccountPlaceSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl
        lateinit var accountTransaction: AccountPlaceTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountPlaceSyncTransactionImpl(database = database)
            syncDataSource = AccountPlaceSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
            accountTransaction = AccountPlaceTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            place: PlaceLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.placeDao().upsert(listOf(place))
                database.accountPlaceDao().upsert(
                    listOf(
                        AccountPlaceLocalEntity(
                            accountId = accountId,
                            placeId = place.id,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findPlace(
            accountId: Uuid,
            placeId: Uuid,
        ): PlaceLocalEntity? = database.accountPlaceDao().find(accountId = accountId, placeId = placeId).first()

        suspend fun isPending(
            accountId: Uuid,
            placeId: Uuid,
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { place -> place.id == placeId }

        test("TC-PLACE-ADD-DATA-005 현재 계정의 업로드 대기 장소만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPendingPlace = place()
            val secondPendingPlace = place()
            val syncedPlace = place()
            val otherAccountPlace = place()
            insertWithSyncState(accountId, firstPendingPlace, isDirty = true)
            insertWithSyncState(accountId, secondPendingPlace, isDirty = true)
            insertWithSyncState(accountId, syncedPlace, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountPlace, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPendingPlace, secondPendingPlace)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertWithSyncState(accountId, place, isDirty = true)

            transaction.clearPending(accountId = accountId, placeList = listOf(place))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 장소는 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedPlace = place(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedPlace = pushedPlace.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changedPlace, isDirty = true)

            transaction.clearPending(accountId = accountId, placeList = listOf(pushedPlace))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedPlace)
        }

        test("TC-DATA-SYNC-DOMAIN-030 장소가 업로드 대기가 되어도 내려받기 위치는 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val cursor = 7L
            transaction.save(accountId = accountId, placeList = listOf(place), cursor = cursor)

            accountTransaction.updateDetail(
                accountId = accountId,
                placeId = place.id,
                detail = detail(),
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )

            isPending(accountId = accountId, placeId = place.id) shouldBe true
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe cursor
        }

        test("TC-PLACE-DETAIL-DATA-008 장소를 삭제하면 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place(updatedAt = Instant.fromEpochMilliseconds(1_000))
            transaction.save(accountId = accountId, placeList = listOf(place), cursor = 3L)

            accountTransaction.updateDeleted(
                accountId = accountId,
                placeId = place.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )

            isPending(accountId = accountId, placeId = place.id) shouldBe true
        }

        test("TC-DATA-SYNC-DATA-016 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstCursor = 3L
            val secondCursor = 11L

            transaction.save(accountId = accountId, placeList = listOf(place()), cursor = firstCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe firstCursor

            transaction.save(accountId = accountId, placeList = listOf(place()), cursor = secondCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe secondCursor
        }

        test("TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            transaction.save(accountId = otherAccountId, placeList = listOf(place()), cursor = 9L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localPlace = place(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remotePlace =
                    localPlace.copy(
                        detail = detail(),
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId, localPlace, isDirty = false)

                transaction.save(accountId = accountId, placeList = listOf(remotePlace), cursor = 5L)

                findPlace(accountId = accountId, placeId = localPlace.id) shouldBe remotePlace
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localPlace = place(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remotePlace =
                localPlace.copy(
                    detail = detail(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId, localPlace, isDirty = true)
            val cursor = 5L

            transaction.save(accountId = accountId, placeList = listOf(remotePlace), cursor = cursor)

            findPlace(accountId = accountId, placeId = localPlace.id) shouldBe localPlace
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe cursor
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이름" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localPlace = place(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remotePlace = localPlace.copy(detail = detail(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, localPlace, isDirty = true)

                transaction.save(accountId = accountId, placeList = listOf(remotePlace), cursor = 5L)

                isPending(accountId = accountId, placeId = localPlace.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 장소는 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remotePlace = place()

            transaction.save(accountId = accountId, placeList = listOf(remotePlace), cursor = 5L)

            findPlace(accountId = accountId, placeId = remotePlace.id) shouldBe remotePlace
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 장소와 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remotePlace = place()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws PlaceSyncTestException()
            val failingTransaction = AccountPlaceSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<PlaceSyncTestException> {
                failingTransaction.save(accountId = accountId, placeList = listOf(remotePlace), cursor = 5L)
            }

            findPlace(accountId = accountId, placeId = remotePlace.id).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-027 업로드한 장소가 같은 내용으로 다시 내려와도 기기 내용은 그대로다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertWithSyncState(accountId, place, isDirty = false)

            transaction.save(accountId = accountId, placeList = listOf(place), cursor = 5L)

            findPlace(accountId = accountId, placeId = place.id) shouldBe place
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertWithSyncState(accountId, place, isDirty = true)
            insertWithSyncState(otherAccountId, place, isDirty = true)

            transaction.clearPending(accountId = accountId, placeList = listOf(place))

            isPending(accountId = accountId, placeId = place.id) shouldBe false
            isPending(accountId = otherAccountId, placeId = place.id) shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(updatedAt: Instant = instant()): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::detail, detail())
                .setExp(PlaceLocalEntity::isDeleted, false)
                .setExp(PlaceLocalEntity::updatedAt, updatedAt)
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun detail(): PlaceDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetailLocalEntity>()
                .setExp(PlaceDetailLocalEntity::latitude, 37.5)
                .setExp(PlaceDetailLocalEntity::longitude, 127.0)
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
