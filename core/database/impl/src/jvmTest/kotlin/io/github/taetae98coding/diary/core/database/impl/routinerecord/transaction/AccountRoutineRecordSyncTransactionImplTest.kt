package io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.calendarRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.findAccountRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource.AccountRoutineRecordLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.datasource.AccountRoutineRecordSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.spyk
import kotlinx.datetime.LocalDate
import java.util.TimeZone
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class RoutineRecordSyncTestException : RuntimeException()

class AccountRoutineRecordSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var syncTransaction: AccountRoutineRecordSyncTransactionImpl
        lateinit var syncDataSource: AccountRoutineRecordSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database = routineTestDatabase()
            syncTransaction = AccountRoutineRecordSyncTransactionImpl(database = database)
            syncDataSource = AccountRoutineRecordSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            record: RoutineRecordLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.routineRecordDao().upsert(record)
                database.accountRoutineRecordSyncDao().upsert(AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = record.id, isDirty = isDirty))
            }
        }

        fun record(updatedAt: Instant? = null): RoutineRecordLocalEntity = Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next()).next().let { record -> record.copy(updatedAt = updatedAt ?: record.updatedAt) }

        test("TC-aac365bb-b751-48aa-8d35-f88b442e15b1 TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 업로드 대상은 그 계정의 업로드 대기 루틴 기록뿐이고 게스트 상태에서 만든 루틴 기록은 포함되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val pending = record()
            insertWithSyncState(accountId = accountId, record = pending, isDirty = true)
            insertWithSyncState(accountId = accountId, record = record(), isDirty = false)
            insertWithSyncState(accountId = Uuid.NIL, record = record(), isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(pending)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드한 수정 시각이 그대로면 동기화 완료가 되고, 그사이 삭제 여부가 바뀌었으면 업로드 대기로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val unchanged = record()
            val pushed = record(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(isDeleted = !pushed.isDeleted, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, record = unchanged, isDirty = true)
            insertWithSyncState(accountId = accountId, record = changed, isDirty = true)

            syncTransaction.clearPending(accountId = accountId, recordList = listOf(unchanged, pushed))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changed)
        }

        test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 내려받은 루틴 기록은 기록 하나 단위로 수정 시각을 비교해 반영하고 루틴 기록 커서만 전진한다") {
            val accountId = Arb.kotlinUuid().next()
            val olderLocal = record(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val sameLocal = record(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val newerLocal = record(updatedAt = Instant.fromEpochMilliseconds(2_000))
            listOf(olderLocal, sameLocal, newerLocal).forEach { local -> insertWithSyncState(accountId = accountId, record = local, isDirty = false) }
            val olderRemote = olderLocal.copy(isDeleted = !olderLocal.isDeleted, updatedAt = Instant.fromEpochMilliseconds(2_000))
            val sameRemote = sameLocal.copy(isDeleted = !sameLocal.isDeleted)
            val newerRemote = newerLocal.copy(isDeleted = !newerLocal.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))

            syncTransaction.upsert(accountId = accountId, recordList = listOf(olderRemote, sameRemote, newerRemote), cursor = 7L)

            database.findRoutineRecordList() shouldBe listOf(olderRemote, sameRemote, newerLocal).sortedBy { record -> record.id.toString() }
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE_RECORD) shouldBe 7L
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val local = record(updatedAt = Instant.fromEpochMilliseconds(2_000))
                insertWithSyncState(accountId = accountId, record = local, isDirty = true)

                syncTransaction.upsert(accountId = accountId, recordList = listOf(local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)), cursor = 5L)

                syncDataSource.readPendingList(accountId = accountId).map { record -> record.id } shouldBe listOf(local.id)
            }
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 루틴보다 먼저 내려받은 루틴 기록도 계정과 연결되어 동기화 완료로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val remote = record()

            syncTransaction.upsert(accountId = accountId, recordList = listOf(remote), cursor = 5L)

            database.findRoutineRecordList() shouldBe listOf(remote)
            database.findAccountRoutineRecordList() shouldBe listOf(AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = remote.id, isDirty = false))
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE_RECORD) shouldBe 5L
        }

        test("TC-471e4725-9b1e-4442-9fe3-dbd6165d525e 루틴보다 먼저 내려받은 루틴 기록은 루틴이 반영될 때까지 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val today = LocalDate(2026, 7, 15)
            val routine = Arb.localRoutine(startDate = LocalDate(2026, 7, 1)).next()
            val remote = Arb.localRoutineRecord(routineId = routine.id, date = today, amount = 250).next()
            val recordDataSource = AccountRoutineRecordLocalDataSourceImpl(database = database)

            syncTransaction.upsert(accountId = accountId, recordList = listOf(remote), cursor = 1L)

            recordDataSource.page(accountId = accountId, routineId = routine.id).loadAll().shouldBeEmpty()
            database.routineSummaryList(accountId = accountId, today = today).shouldBeEmpty()
            database.calendarRoutineRecordList(accountId = accountId, start = today, endInclusive = today).shouldBeEmpty()

            AccountRoutineSyncTransactionImpl(database = database).upsert(accountId = accountId, routineList = listOf(routine), cursor = 1L)

            recordDataSource.page(accountId = accountId, routineId = routine.id).loadAll() shouldBe listOf(remote)
            database.routineSummaryList(accountId = accountId, today = today).map { summary -> summary.routine.id to summary.cycleAmount } shouldBe listOf(routine.id to 250L)
            database.calendarRoutineRecordList(accountId = accountId, start = today, endInclusive = today).map { calendarRecord -> calendarRecord.routineId to calendarRecord.amount } shouldBe
                listOf(routine.id to 250L)
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 루틴 기록과 커서가 모두 반영되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws RoutineRecordSyncTestException()

            shouldThrowExactly<RoutineRecordSyncTestException> {
                AccountRoutineRecordSyncTransactionImpl(database = failingDatabase).upsert(accountId = accountId, recordList = listOf(record()), cursor = 5L)
            }

            database.findRoutineRecordList().shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.ROUTINE_RECORD) shouldBe 0L
        }

        test("TC-d9851777-4709-4023-9a95-513efbdf274b 기기의 시간대와 관계없이 내려받은 루틴 기록의 날짜는 같은 연·월·일로 저장된다") {
            val defaultTimeZone = TimeZone.getDefault()
            try {
                // Etc/GMT-14는 협정 세계시보다 14시간 빠르고 Etc/GMT+12는 12시간 느리다.
                listOf("Etc/GMT-14", "UTC", "Etc/GMT+12").forEach { timeZoneId ->
                    TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
                    val accountId = Arb.kotlinUuid().next()
                    val remote = Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next(), date = LocalDate(2026, 3, 1)).next()

                    syncTransaction.upsert(accountId = accountId, recordList = listOf(remote), cursor = 1L)

                    database.findRoutineRecordList().single { record -> record.id == remote.id }.date shouldBe LocalDate(2026, 3, 1)
                }
            } finally {
                TimeZone.setDefault(defaultTimeZone)
            }
        }
    })
