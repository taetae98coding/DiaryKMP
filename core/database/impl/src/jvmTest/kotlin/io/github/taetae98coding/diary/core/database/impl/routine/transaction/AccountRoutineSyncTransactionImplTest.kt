package io.github.taetae98coding.diary.core.database.impl.routine.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.datasource.AccountRoutineSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.routineDetailLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class RoutineSyncTestException : RuntimeException()

class AccountRoutineSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var syncTransaction: AccountRoutineSyncTransactionImpl
        lateinit var dataSource: AccountRoutineLocalDataSourceImpl
        lateinit var syncDataSource: AccountRoutineSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            syncTransaction = AccountRoutineSyncTransactionImpl(database = database)
            dataSource = AccountRoutineLocalDataSourceImpl(database = database)
            syncDataSource = AccountRoutineSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            routine: RoutineLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.routineDao().upsert(listOf(routine))
                database.accountRoutineSyncDao().upsert(AccountRoutineLocalEntity(accountId = accountId, routineId = routine.id, isDirty = isDirty))
            }
        }

        suspend fun find(
            accountId: Uuid,
            routineId: Uuid,
        ): RoutineLocalEntity? = dataSource.find(accountId = accountId, routineId = routineId).first()

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 루틴 추가·수정·삭제·실행 취소는 업로드 대기 상태가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val detail = Arb.routineDetailLocalEntity().next()
            val routineTransaction = AccountRoutineTransactionImpl(database = database)

            suspend fun markUploaded() {
                syncTransaction.clearPending(accountId = accountId, routineList = syncDataSource.readPendingList(accountId = accountId))
                syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            }

            routineTransaction.upsert(accountId = accountId, routine = routine, routineTagList = emptyList())
            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(routine)

            markUploaded()
            routineTransaction.updateDetail(accountId = accountId, routineId = routine.id, detail = detail, updatedAt = Instant.fromEpochMilliseconds(2_000)) shouldBe 1
            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(routine.copy(detail = detail, updatedAt = Instant.fromEpochMilliseconds(2_000)))

            markUploaded()
            routineTransaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(3_000)) shouldBe 1
            syncDataSource.readPendingList(accountId = accountId) shouldBe
                listOf(routine.copy(detail = detail, isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(3_000)))

            markUploaded()
            routineTransaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(4_000)) shouldBe 1
            syncDataSource.readPendingList(accountId = accountId) shouldBe
                listOf(routine.copy(detail = detail, isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(4_000)))
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 루틴은 포함되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val guestRoutine = Arb.localRoutine(isDeleted = false).next()
            val accountRoutine = Arb.localRoutine(isDeleted = false).next()
            insertWithSyncState(accountId = Uuid.NIL, routine = guestRoutine, isDirty = true)
            insertWithSyncState(accountId = accountId, routine = accountRoutine, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountRoutine)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val routine = Arb.localRoutine(isDeleted = false).next()
            insertWithSyncState(accountId = accountId, routine = routine, isDirty = true)

            syncTransaction.clearPending(accountId = accountId, routineList = listOf(routine))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 루틴은 업로드 대기로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val pushed = Arb.localRoutine(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, routine = changed, isDirty = true)

            syncTransaction.clearPending(accountId = accountId, routineList = listOf(pushed))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changed)
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = Arb.kotlinUuid().next()
                val local = Arb.localRoutine(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(detail = Arb.routineDetailLocalEntity().next(), isDeleted = true, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId = accountId, routine = local, isDirty = false)

                syncTransaction.upsert(accountId = accountId, routineList = listOf(remote), cursor = 5L)

                find(accountId = accountId, routineId = local.id) shouldBe remote
            }
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지하고 커서는 전진한다") {
            val accountId = Arb.kotlinUuid().next()
            val local = Arb.localRoutine(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(detail = Arb.routineDetailLocalEntity().next(), isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId = accountId, routine = local, isDirty = true)

            syncTransaction.upsert(accountId = accountId, routineList = listOf(remote), cursor = 5L)

            find(accountId = accountId, routineId = local.id) shouldBe local
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE) shouldBe 5L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val local = Arb.localRoutine(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remote = local.copy(detail = Arb.routineDetailLocalEntity().next(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId = accountId, routine = local, isDirty = true)

                syncTransaction.upsert(accountId = accountId, routineList = listOf(remote), cursor = 5L)

                syncDataSource.readPendingList(accountId = accountId).map { routine -> routine.id } shouldBe listOf(local.id)
            }
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 루틴은 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = Arb.kotlinUuid().next()
            val remote = Arb.localRoutine(isDeleted = false).next()

            syncTransaction.upsert(accountId = accountId, routineList = listOf(remote), cursor = 5L)

            find(accountId = accountId, routineId = remote.id) shouldBe remote
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE) shouldBe 5L
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 루틴과 커서가 모두 반영되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val remote = Arb.localRoutine(isDeleted = false).next()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws RoutineSyncTestException()
            val failingTransaction = AccountRoutineSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<RoutineSyncTestException> {
                failingTransaction.upsert(accountId = accountId, routineList = listOf(remote), cursor = 5L)
            }

            find(accountId = accountId, routineId = remote.id).shouldBeNull()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE) shouldBe 0L
        }

        test("TC-45c868d3-1f7f-44d3-9688-0d9d57ea964e 목표량이 비어 있는 루틴을 내려받으면 기기의 루틴은 목표량이 없어진다") {
            val accountId = Arb.kotlinUuid().next()
            val stored =
                Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next().let { routine ->
                    routine.copy(detail = routine.detail.copy(goalAmount = 2000))
                }
            AccountRoutineTransactionImpl(database = database).upsert(accountId = accountId, routine = stored, routineTagList = emptyList())
            val pulled = stored.copy(detail = stored.detail.copy(goalAmount = null), updatedAt = stored.updatedAt + 1.seconds)

            syncTransaction.upsert(accountId = accountId, routineList = listOf(pulled), cursor = 1L)

            val found = dataSource.find(accountId = accountId, routineId = stored.id).first().shouldNotBeNull()
            found.detail.goalAmount.shouldBeNull()
        }

        test("TC-d2c02683-b789-4a75-badf-29489a2c1cde 내려받은 루틴의 주기는 그 값이 뜻하는 주기로 저장된다") {
            RoutineCycleLocalEntity.entries.forEach { cycle ->
                val accountId = Arb.kotlinUuid().next()
                val pulled = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), cycle = cycle).next()

                syncTransaction.upsert(accountId = accountId, routineList = listOf(pulled), cursor = 1L)

                dataSource
                    .find(accountId = accountId, routineId = pulled.id)
                    .first()
                    .shouldNotBeNull()
                    .detail.cycle shouldBe cycle
            }
        }

        test("TC-4534eddf-89a0-4740-a48a-fd3accd33b78 내려받은 루틴의 단위는 글자 그대로 저장되고 내려받기 위치가 나아간다") {
            listOf("회", "km", "ML", "분 걷기", "kilometers", "").forEach { unit ->
                val accountId = Arb.kotlinUuid().next()
                val pulled = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), unit = unit).next()
                syncTransaction.upsert(accountId = accountId, routineList = emptyList(), cursor = 10L)

                syncTransaction.upsert(accountId = accountId, routineList = listOf(pulled), cursor = 11L)

                find(accountId = accountId, routineId = pulled.id).shouldNotBeNull().detail.unit shouldBe unit
                syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE) shouldBe 11L
            }
        }

        test("TC-d9851777-4709-4023-9a95-513efbdf274b 기기의 시간대와 관계없이 내려받은 루틴의 날짜는 같은 연·월·일로 저장된다") {
            val defaultTimeZone = TimeZone.getDefault()
            try {
                // Etc/GMT-14는 협정 세계시보다 14시간 빠르고 Etc/GMT+12는 12시간 느리다.
                listOf("Etc/GMT-14", "UTC", "Etc/GMT+12").forEach { timeZoneId ->
                    TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
                    val accountId = Arb.kotlinUuid().next()
                    val pulled = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()

                    syncTransaction.upsert(accountId = accountId, routineList = listOf(pulled), cursor = 1L)

                    val found = dataSource.find(accountId = accountId, routineId = pulled.id).first().shouldNotBeNull()
                    found.detail.startDate shouldBe LocalDate(2026, 3, 1)
                }
            } finally {
                TimeZone.setDefault(defaultTimeZone)
            }
        }

        test("내려받은 루틴은 날짜, 주기, 단위와 완료 여부를 포함해 그대로 저장된다") {
            listOf(false, true).forEach { isFinished ->
                val accountId = Arb.kotlinUuid().next()
                val pulled = Arb.localRoutine(isDeleted = false, isFinished = isFinished).next()

                syncTransaction.upsert(accountId = accountId, routineList = listOf(pulled), cursor = 1L)

                dataSource.find(accountId = accountId, routineId = pulled.id).first() shouldBe pulled
            }
        }
    })
