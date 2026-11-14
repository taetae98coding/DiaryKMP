package io.github.taetae98coding.diary.core.database.impl.entitytag.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagKindCase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTc
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagKindCaseList
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class EntityTagSyncTestException : RuntimeException()

// 웹·장소 연결 테스트가 쓰던 케이스 ID는 그 종류에서만 붙인다.
private val pendingTcMap: Map<EntityTagKindLocalEntity, String> =
    mapOf(
        EntityTagKindLocalEntity.WEB to "TC-505ff65c-4d9f-4c1c-bfa3-b3bc587275e3 ",
        EntityTagKindLocalEntity.PLACE to "TC-088d4249-7c3b-409f-94e8-dba21aacbdbf ",
    )

private val pulledTcMap: Map<EntityTagKindLocalEntity, String> =
    mapOf(
        EntityTagKindLocalEntity.WEB to "TC-3de3f780-f097-4009-b2ff-da214d0d3328 ",
        EntityTagKindLocalEntity.PLACE to "TC-96b8e102-0188-48ac-83f6-9f2651aaee11 ",
    )

private val independentTcMap: Map<EntityTagKindLocalEntity, String> =
    mapOf(
        EntityTagKindLocalEntity.WEB to "TC-1b5d8181-1df0-48a4-a4fd-729a64e1e6e6 ",
        EntityTagKindLocalEntity.PLACE to "TC-7a28c56d-8d87-4e0f-92a0-97784ad00d1f ",
    )

class AccountEntityTagSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountEntityTagSyncTransactionImpl
        lateinit var syncDataSource: AccountEntityTagSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountEntityTagSyncTransactionImpl(database = database)
            syncDataSource = AccountEntityTagSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun EntityTagKindCase.insertWithSyncState(
            accountId: Uuid,
            entityTag: EntityTagLocalEntity,
            isDirty: Boolean,
        ) {
            val table = database.entityTagTable(kind = kind)
            database.withWriteTransaction {
                table.upsert(entityTagList = listOf(entityTag))
                if (isDirty) {
                    table.upsertAccountPending(accountId = accountId, entityTagList = listOf(entityTag))
                } else {
                    table.insertIgnoreAccountSynced(accountId = accountId, entityTagList = listOf(entityTag))
                }
            }
        }

        suspend fun EntityTagKindCase.find(
            entityId: Uuid,
            tagId: Uuid,
        ): EntityTagLocalEntity? =
            database
                .entityTagTable(kind = kind)
                .findByEntityIdList(listOf(entityId))
                .firstOrNull { entityTag -> entityTag.tagId == tagId }

        suspend fun EntityTagKindCase.isPending(
            entityTag: EntityTagLocalEntity,
            accountId: Uuid,
        ): Boolean =
            syncDataSource
                .readPendingList(accountId = accountId, kind = kind)
                .any { pending -> pending.entityId == entityTag.entityId && pending.tagId == entityTag.tagId }

        entityTagKindCaseList.forEach { case ->
            val syncKind = case.kind.syncKind

            context(case.label) {
                test("${pendingTcMap[case.kind].orEmpty()}${case.tc(EntityTagTc.PENDING_PER_ACCOUNT)}해제된 연결도 포함해 현재 계정의 업로드 대기 연결만 조회한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val otherAccountId = Arb.kotlinUuid().next()
                    val firstPending = entityTag()
                    val secondPending = entityTag(isDeleted = true)
                    val synced = entityTag()
                    val otherAccountPending = entityTag()
                    case.insertWithSyncState(accountId, firstPending, isDirty = true)
                    case.insertWithSyncState(accountId, secondPending, isDirty = true)
                    case.insertWithSyncState(accountId, synced, isDirty = false)
                    case.insertWithSyncState(otherAccountId, otherAccountPending, isDirty = true)

                    syncDataSource
                        .readPendingList(accountId = accountId, kind = case.kind)
                        .shouldContainExactlyInAnyOrder(firstPending, secondPending)
                }

                test("TC-aac365bb-b751-48aa-8d35-f88b442e15b1 TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 ${case.label}과 태그의 연결은 포함되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val guestEntity = entityTag()
                    val accountEntity = entityTag()
                    case.insertWithSyncState(accountId = Uuid.NIL, entityTag = guestEntity, isDirty = true)
                    case.insertWithSyncState(accountId = accountId, entityTag = accountEntity, isDirty = true)

                    syncDataSource.readPendingList(accountId = accountId, kind = case.kind) shouldBe listOf(accountEntity)
                }

                test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 연결을 만들거나 해제하면 그 계정의 업로드 대기가 된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val entityId = Arb.kotlinUuid().next()
                    val tagId = Arb.kotlinUuid().next()
                    val linkedAt = Instant.fromEpochMilliseconds(1_000)
                    val unlinkedAt = Instant.fromEpochMilliseconds(2_000)
                    val linkTransaction = AccountEntityTagTransactionImpl(database = database)

                    linkTransaction.upsert(accountId = accountId, kind = case.kind, entityId = entityId, tagId = tagId, isDeleted = false, updatedAt = linkedAt)

                    val linkedList = syncDataSource.readPendingList(accountId = accountId, kind = case.kind)
                    linkedList shouldBe listOf(EntityTagLocalEntity(entityId = entityId, tagId = tagId, isDeleted = false, updatedAt = linkedAt, createdAt = linkedAt))

                    transaction.clearPending(accountId = accountId, kind = case.kind, entityTagList = linkedList)
                    syncDataSource.readPendingList(accountId = accountId, kind = case.kind).shouldBeEmpty()
                    linkTransaction.upsert(accountId = accountId, kind = case.kind, entityId = entityId, tagId = tagId, isDeleted = true, updatedAt = unlinkedAt)

                    syncDataSource.readPendingList(accountId = accountId, kind = case.kind) shouldBe
                        listOf(EntityTagLocalEntity(entityId = entityId, tagId = tagId, isDeleted = true, updatedAt = unlinkedAt, createdAt = linkedAt))
                }

                test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val entityTag = entityTag()
                    case.insertWithSyncState(accountId, entityTag, isDirty = true)

                    transaction.clearPending(accountId = accountId, kind = case.kind, entityTagList = listOf(entityTag))

                    syncDataSource.readPendingList(accountId = accountId, kind = case.kind).shouldBeEmpty()
                }

                test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val pushed = entityTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
                    val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
                    case.insertWithSyncState(accountId, changed, isDirty = true)

                    transaction.clearPending(accountId = accountId, kind = case.kind, entityTagList = listOf(pushed))

                    syncDataSource.readPendingList(accountId = accountId, kind = case.kind) shouldBe listOf(changed)
                }

                test("대기 해제는 요청한 계정의 연결만 바꾼다") {
                    val accountId = Arb.kotlinUuid().next()
                    val otherAccountId = Arb.kotlinUuid().next()
                    val entityTag = entityTag()
                    case.insertWithSyncState(accountId, entityTag, isDirty = true)
                    case.insertWithSyncState(otherAccountId, entityTag, isDirty = true)

                    transaction.clearPending(accountId = accountId, kind = case.kind, entityTagList = listOf(entityTag))

                    case.isPending(entityTag, accountId) shouldBe false
                    case.isPending(entityTag, otherAccountId) shouldBe true
                }

                test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 기록된 순번이 없으면 기본 커서를 사용하고 저장이 끝나면 커서가 기록된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val otherAccountId = Arb.kotlinUuid().next()
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 0L

                    transaction.upsert(accountId = otherAccountId, kind = case.kind, entityTagList = listOf(entityTag()), cursor = 9L)
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 0L

                    transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(entityTag()), cursor = 3L)
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 3L

                    transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(entityTag()), cursor = 11L)
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 11L
                }

                test("TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 ${case.label}과 태그의 연결의 내려받기 위치는 다른 종류와 따로 기록된다") {
                    val accountId = Arb.kotlinUuid().next()

                    transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(entityTag()), cursor = 7L)

                    SyncKindLocalEntity.entries.forEach { kind ->
                        syncCursorDataSource.read(accountId = accountId, kind = kind) shouldBe if (kind == syncKind) 7L else 0L
                    }
                }

                listOf(
                    "늦음" to Instant.fromEpochMilliseconds(2_000),
                    "같음" to Instant.fromEpochMilliseconds(1_000),
                ).forEach { (label, remoteUpdatedAt) ->
                    // 종류별 문서의 "내려받은 연결이 기기에 반영된다"는 서버 수정 시각이 더 늦은 경우라, 그 ID는 늦음 행에만 붙인다.
                    val pulledTc = if (label == "늦음") "${pulledTcMap[case.kind].orEmpty()}${case.tc(EntityTagTc.PULLED_APPLIED)}" else ""

                    test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 ${pulledTc}서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                        val accountId = Arb.kotlinUuid().next()
                        val local = entityTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
                        val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                        case.insertWithSyncState(accountId, local, isDirty = false)

                        transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(remote), cursor = 5L)

                        case.find(entityId = local.entityId, tagId = local.tagId) shouldBe remote
                        syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 5L
                    }
                }

                test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val local = entityTag(updatedAt = Instant.fromEpochMilliseconds(2_000))
                    val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
                    case.insertWithSyncState(accountId, local, isDirty = true)

                    transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(remote), cursor = 5L)

                    case.find(entityId = local.entityId, tagId = local.tagId) shouldBe local
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 5L
                }

                test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val remote = entityTag()

                    transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(remote), cursor = 5L)

                    case.find(entityId = remote.entityId, tagId = remote.tagId) shouldBe remote
                    syncDataSource.readPendingList(accountId = accountId, kind = case.kind).shouldBeEmpty()
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 5L
                }

                listOf(
                    "늦음" to Instant.fromEpochMilliseconds(3_000),
                    "같음" to Instant.fromEpochMilliseconds(2_000),
                    "이른" to Instant.fromEpochMilliseconds(1_000),
                ).forEach { (label, remoteUpdatedAt) ->
                    test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {

                        val accountId = Arb.kotlinUuid().next()
                        val pending = entityTag(updatedAt = Instant.fromEpochMilliseconds(2_000))
                        val synced = entityTag(updatedAt = Instant.fromEpochMilliseconds(2_000))
                        case.insertWithSyncState(accountId, pending, isDirty = true)
                        case.insertWithSyncState(accountId, synced, isDirty = false)

                        transaction.upsert(
                            accountId = accountId,
                            kind = case.kind,
                            entityTagList = listOf(pending.copy(updatedAt = remoteUpdatedAt), synced.copy(updatedAt = remoteUpdatedAt)),
                            cursor = 5L,
                        )

                        case.isPending(pending, accountId) shouldBe true
                        case.isPending(synced, accountId) shouldBe false
                    }
                }

                test("${independentTcMap[case.kind].orEmpty()}${case.tc(EntityTagTc.OTHER_LINK_INDEPENDENT)}같은 ${case.label}의 다른 연결은 서로 독립적으로 저장된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val entityId = Arb.kotlinUuid().next()
                    val local = entityTag(entityId = entityId, updatedAt = Instant.fromEpochMilliseconds(2_000))
                    val remoteOfLocal = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
                    val remoteOfNew = entityTag(entityId = entityId, updatedAt = Instant.fromEpochMilliseconds(1_000))
                    case.insertWithSyncState(accountId, local, isDirty = true)

                    transaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(remoteOfLocal, remoteOfNew), cursor = 5L)

                    case.find(entityId = entityId, tagId = local.tagId) shouldBe local
                    case.find(entityId = entityId, tagId = remoteOfNew.tagId) shouldBe remoteOfNew
                }

                test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val remote = entityTag()
                    val failingDatabase = spyk(database)
                    every { failingDatabase.syncCursorDao() } throws EntityTagSyncTestException()
                    val failingTransaction = AccountEntityTagSyncTransactionImpl(database = failingDatabase)

                    shouldThrowExactly<EntityTagSyncTestException> {
                        failingTransaction.upsert(accountId = accountId, kind = case.kind, entityTagList = listOf(remote), cursor = 5L)
                    }

                    case.find(entityId = remote.entityId, tagId = remote.tagId).shouldBeNull()
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 0L
                }

                test("다른 종류의 내려받은 연결은 이 종류의 연결로 저장되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val remote = entityTag()
                    entityTagKindCaseList
                        .filterNot { other -> other.kind == case.kind }
                        .forEach { other -> transaction.upsert(accountId = accountId, kind = other.kind, entityTagList = listOf(remote), cursor = 5L) }

                    case.find(entityId = remote.entityId, tagId = remote.tagId).shouldBeNull()
                    syncCursorDataSource.read(accountId = accountId, kind = syncKind) shouldBe 0L
                }
            }
        }
    }) {
    public companion object {
        private fun entityTag(
            entityId: Uuid = Arb.kotlinUuid().next(),
            tagId: Uuid = Arb.kotlinUuid().next(),
            isDeleted: Boolean = false,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ): EntityTagLocalEntity =
            EntityTagLocalEntity(
                entityId = entityId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
                createdAt = Arb.secondPrecisionInstant().next(),
            )
    }
}
