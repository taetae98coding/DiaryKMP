package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDaySyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.dday.ddayDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.localDDay
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class DDaySyncTestException : RuntimeException()

class AccountDDaySyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var syncTransaction: AccountDDaySyncTransactionImpl
        lateinit var dataSource: AccountDDayLocalDataSourceImpl
        lateinit var syncDataSource: AccountDDaySyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            syncTransaction = AccountDDaySyncTransactionImpl(database = database)
            dataSource = AccountDDayLocalDataSourceImpl(database = database)
            syncDataSource = AccountDDaySyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            dday: DDayLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.ddayDao().upsert(listOf(dday))
                database.accountDDaySyncDao().upsert(AccountDDayLocalEntity(accountId = accountId, ddayId = dday.id, isDirty = isDirty))
            }
        }

        suspend fun find(
            accountId: Uuid,
            ddayId: Uuid,
        ): DDayLocalEntity? = dataSource.find(accountId = accountId, ddayId = ddayId).first()

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 디데이 추가·수정·삭제·실행 취소는 업로드 대기 상태가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = Arb.localDDay(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val detail = Arb.ddayDetailLocalEntity().next()
            val ddayTransaction = AccountDDayTransactionImpl(database = database)

            suspend fun markUploaded() {
                syncTransaction.clearPending(accountId = accountId, ddayList = syncDataSource.readPendingList(accountId = accountId))
                syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            }

            ddayTransaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())
            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(dday)

            markUploaded()
            ddayTransaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = detail, updatedAt = Instant.fromEpochMilliseconds(2_000)) shouldBe 1
            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(dday.copy(detail = detail, updatedAt = Instant.fromEpochMilliseconds(2_000)))

            markUploaded()
            ddayTransaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(3_000)) shouldBe 1
            syncDataSource.readPendingList(accountId = accountId) shouldBe
                listOf(dday.copy(detail = detail, isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(3_000)))

            markUploaded()
            ddayTransaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(4_000)) shouldBe 1
            syncDataSource.readPendingList(accountId = accountId) shouldBe
                listOf(dday.copy(detail = detail, isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(4_000)))
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 디데이는 포함되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val guestDDay = Arb.localDDay(isDeleted = false).next()
            val accountDDay = Arb.localDDay(isDeleted = false).next()
            insertWithSyncState(accountId = Uuid.NIL, dday = guestDDay, isDirty = true)
            insertWithSyncState(accountId = accountId, dday = accountDDay, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountDDay)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = Arb.localDDay(isDeleted = false).next()
            insertWithSyncState(accountId = accountId, dday = dday, isDirty = true)

            syncTransaction.clearPending(accountId = accountId, ddayList = listOf(dday))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 디데이는 업로드 대기로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val pushed = Arb.localDDay(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, dday = changed, isDirty = true)

            syncTransaction.clearPending(accountId = accountId, ddayList = listOf(pushed))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changed)
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = Arb.kotlinUuid().next()
                val local = Arb.localDDay(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(detail = Arb.ddayDetailLocalEntity().next(), isDeleted = true, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId = accountId, dday = local, isDirty = false)

                syncTransaction.upsert(accountId = accountId, ddayList = listOf(remote), cursor = 5L)

                find(accountId = accountId, ddayId = local.id) shouldBe remote
            }
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지하고 커서는 전진한다") {
            val accountId = Arb.kotlinUuid().next()
            val local = Arb.localDDay(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(detail = Arb.ddayDetailLocalEntity().next(), isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId = accountId, dday = local, isDirty = true)

            syncTransaction.upsert(accountId = accountId, ddayList = listOf(remote), cursor = 5L)

            find(accountId = accountId, ddayId = local.id) shouldBe local
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.DDAY) shouldBe 5L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val local = Arb.localDDay(isDeleted = false).next().copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remote = local.copy(detail = Arb.ddayDetailLocalEntity().next(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId = accountId, dday = local, isDirty = true)

                syncTransaction.upsert(accountId = accountId, ddayList = listOf(remote), cursor = 5L)

                syncDataSource.readPendingList(accountId = accountId).map { dday -> dday.id } shouldBe listOf(local.id)
            }
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 디데이는 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = Arb.kotlinUuid().next()
            val remote = Arb.localDDay(isDeleted = false).next()

            syncTransaction.upsert(accountId = accountId, ddayList = listOf(remote), cursor = 5L)

            find(accountId = accountId, ddayId = remote.id) shouldBe remote
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.DDAY) shouldBe 5L
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 디데이와 커서가 모두 반영되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val remote = Arb.localDDay(isDeleted = false).next()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws DDaySyncTestException()
            val failingTransaction = AccountDDaySyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<DDaySyncTestException> {
                failingTransaction.upsert(accountId = accountId, ddayList = listOf(remote), cursor = 5L)
            }

            find(accountId = accountId, ddayId = remote.id).shouldBeNull()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.DDAY) shouldBe 0L
        }

        test("TC-1940bbd8-f6e9-4cd9-8845-d0c028854f0e 내려받은 디데이의 반복 간격 목록이 기기의 목록을 대체한다") {
            listOf(
                setOf(DateTimeUnit.MonthBased(months = 12), DateTimeUnit.DayBased(days = 1000)),
                emptySet(),
            ).forEach { pulledRepeats ->
                val accountId = Arb.kotlinUuid().next()
                val stored = Arb.localDDay(baseDate = LocalDate(2026, 1, 1), repeats = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12))).next()
                AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = stored, ddayTagList = emptyList())
                val pulled = stored.copy(detail = stored.detail.copy(repeats = pulledRepeats), updatedAt = stored.updatedAt + 1.seconds)

                syncTransaction.upsert(accountId = accountId, ddayList = listOf(pulled), cursor = 1L)

                dataSource
                    .find(accountId = accountId, ddayId = stored.id)
                    .first()
                    .shouldNotBeNull()
                    .detail.repeats shouldBe pulledRepeats
            }
        }

        test("TC-d3c9e994-62b9-432e-8218-9b2570678f20 종료일이 비어 있는 디데이를 내려받으면 기기의 디데이는 종료일이 없어진다") {
            val accountId = Arb.kotlinUuid().next()
            val stored = Arb.localDDay(baseDate = LocalDate(2026, 3, 1), endDate = LocalDate(2027, 3, 1)).next()
            AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = stored, ddayTagList = emptyList())
            val pulled = stored.copy(detail = stored.detail.copy(endDate = null), updatedAt = stored.updatedAt + 1.seconds)

            syncTransaction.upsert(accountId = accountId, ddayList = listOf(pulled), cursor = 1L)

            dataSource
                .find(accountId = accountId, ddayId = stored.id)
                .first()
                .shouldNotBeNull()
                .detail.endDate
                .shouldBeNull()
        }

        // 추가 테스트: 내려받은 디데이 전체가 저장 형식을 거쳐도 그대로 되돌아온다.
        test("내려받은 디데이는 반복 간격과 날짜를 포함해 그대로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val pulled = Arb.localDDay(isDeleted = false).next()

            syncTransaction.upsert(accountId = accountId, ddayList = listOf(pulled), cursor = 1L)

            dataSource.find(accountId = accountId, ddayId = pulled.id).first() shouldBe pulled
        }
    })
