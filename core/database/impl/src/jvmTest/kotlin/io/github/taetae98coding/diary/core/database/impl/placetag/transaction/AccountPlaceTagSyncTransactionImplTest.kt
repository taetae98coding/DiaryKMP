package io.github.taetae98coding.diary.core.database.impl.placetag.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.placetag.datasource.AccountPlaceTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class PlaceTagSyncTestException : RuntimeException()

class AccountPlaceTagSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountPlaceTagSyncTransactionImpl
        lateinit var syncDataSource: AccountPlaceTagSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountPlaceTagSyncTransactionImpl(database = database)
            syncDataSource = AccountPlaceTagSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            placeTag: PlaceTagLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.placeTagDao().upsert(placeTag)
                database.accountPlaceTagSyncDao().upsert(
                    AccountPlaceTagLocalEntity(
                        accountId = accountId,
                        placeId = placeTag.placeId,
                        tagId = placeTag.tagId,
                        isDirty = isDirty,
                    ),
                )
            }
        }

        suspend fun findPlaceTag(
            placeId: Uuid,
            tagId: Uuid,
        ): PlaceTagLocalEntity? =
            database
                .placeTagDao()
                .findByPlaceIdList(listOf(placeId))
                .firstOrNull { placeTag -> placeTag.tagId == tagId }

        suspend fun isPending(
            placeTag: PlaceTagLocalEntity,
            accountId: Uuid,
        ): Boolean =
            syncDataSource
                .findPending(accountId = accountId)
                .any { pending -> pending.placeId == placeTag.placeId && pending.tagId == placeTag.tagId }

        test("TC-PLACE-TAG-DATA-004 해제된 연결도 포함해 현재 계정의 업로드 대기 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPending = placeTag()
            val secondPending = placeTag(isDeleted = true)
            val synced = placeTag()
            val otherAccountPending = placeTag()
            insertWithSyncState(accountId, firstPending, isDirty = true)
            insertWithSyncState(accountId, secondPending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountPending, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 장소와 태그의 연결은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = placeTag()
            val accountEntity = placeTag()
            insertWithSyncState(accountId = Uuid.NIL, placeTag = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, placeTag = accountEntity, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val placeTag = placeTag()
            insertWithSyncState(accountId, placeTag, isDirty = true)

            transaction.clearPending(accountId = accountId, placeTagList = listOf(placeTag))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = placeTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, placeTagList = listOf(pushed))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changed)
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val placeTag = placeTag()
            insertWithSyncState(accountId, placeTag, isDirty = true)
            insertWithSyncState(otherAccountId, placeTag, isDirty = true)

            transaction.clearPending(accountId = accountId, placeTagList = listOf(placeTag))

            isPending(placeTag, accountId) shouldBe false
            isPending(placeTag, otherAccountId) shouldBe true
        }

        test("TC-DATA-SYNC-DATA-016 TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용하고 저장이 끝나면 커서가 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 0L

            transaction.save(accountId = otherAccountId, placeTagList = listOf(placeTag()), cursor = 9L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 0L

            transaction.save(accountId = accountId, placeTagList = listOf(placeTag()), cursor = 3L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 3L

            transaction.save(accountId = accountId, placeTagList = listOf(placeTag()), cursor = 11L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 11L
        }

        test("TC-DATA-SYNC-DATA-028 장소와 태그의 연결의 내려받기 위치는 다른 종류와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, placeTagList = listOf(placeTag()), cursor = 7L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 7L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe 0L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe 0L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB_TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = placeTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.save(accountId = accountId, placeTagList = listOf(remote), cursor = 5L)

                findPlaceTag(placeId = local.placeId, tagId = local.tagId) shouldBe remote
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = placeTag(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, placeTagList = listOf(remote), cursor = 5L)

            findPlaceTag(placeId = local.placeId, tagId = local.tagId) shouldBe local
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-025 TC-PLACE-TAG-DATA-007 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = placeTag()

            transaction.save(accountId = accountId, placeTagList = listOf(remote), cursor = 5L)

            findPlaceTag(placeId = remote.placeId, tagId = remote.tagId) shouldBe remote
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-024 내려받기 저장은 이미 있는 연결의 대기 여부를 덮어쓰지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pending = placeTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val synced = placeTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, pending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)

            transaction.save(
                accountId = accountId,
                placeTagList = listOf(pending, synced),
                cursor = 5L,
            )

            isPending(pending, accountId) shouldBe true
            isPending(synced, accountId) shouldBe false
        }

        test("TC-PLACE-TAG-DATA-003 같은 장소의 다른 연결은 서로 독립적으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val local = placeTag(placeId = placeId, updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteOfLocal = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remoteOfNew = placeTag(placeId = placeId, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(
                accountId = accountId,
                placeTagList = listOf(remoteOfLocal, remoteOfNew),
                cursor = 5L,
            )

            findPlaceTag(placeId = placeId, tagId = local.tagId) shouldBe local
            findPlaceTag(placeId = placeId, tagId = remoteOfNew.tagId) shouldBe remoteOfNew
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = placeTag()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws PlaceTagSyncTestException()
            val failingTransaction = AccountPlaceTagSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<PlaceTagSyncTestException> {
                failingTransaction.save(accountId = accountId, placeTagList = listOf(remote), cursor = 5L)
            }

            findPlaceTag(placeId = remote.placeId, tagId = remote.tagId).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE_TAG) shouldBe 0L
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun placeTag(
            placeId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            tagId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isDeleted: Boolean = false,
            updatedAt: Instant = instant(),
        ): PlaceTagLocalEntity =
            PlaceTagLocalEntity(
                placeId = placeId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
                createdAt = instant(),
            )

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
