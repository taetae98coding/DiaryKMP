package io.github.taetae98coding.diary.core.database.impl.checklist.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.datasource.AccountChecklistSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class AccountChecklistSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountChecklistSyncTransactionImpl
        lateinit var checklistTransaction: AccountChecklistTransactionImpl
        lateinit var syncDataSource: AccountChecklistSyncLocalDataSourceImpl
        lateinit var cursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountChecklistSyncTransactionImpl(database = database)
            checklistTransaction = AccountChecklistTransactionImpl(database = database)
            syncDataSource = AccountChecklistSyncLocalDataSourceImpl(database = database)
            cursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 업로드 대상은 그 계정의 업로드 대기 체크리스트뿐이고 게스트 상태에서 만든 체크리스트는 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pending = fixtureMonkey.localChecklist(isDeleted = false)
            val guest = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = accountId, checklist = pending, itemList = emptyList())
            checklistTransaction.upsert(accountId = Uuid.NIL, checklist = guest, itemList = emptyList())

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(pending)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드한 수정 시각이 그대로면 동기화 완료가 되고, 그사이 바뀌었으면 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val unchanged = fixtureMonkey.localChecklist(isDeleted = false)
            val changed = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = accountId, checklist = unchanged, itemList = emptyList())
            checklistTransaction.upsert(accountId = accountId, checklist = changed, itemList = emptyList())
            checklistTransaction.updateDeleted(
                accountId = accountId,
                checklistId = changed.id,
                isDeleted = true,
                updatedAt = changed.updatedAt + 1.seconds,
            )

            transaction.clearPending(accountId = accountId, checklistList = listOf(unchanged, changed))

            syncDataSource.readPendingList(accountId = accountId).map { checklist -> checklist.id } shouldBe listOf(changed.id)
        }

        test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 내려받은 체크리스트는 기기보다 늦으면 반영하고, 기기가 더 늦으면 유지하며 체크리스트 커서만 전진한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = fixtureMonkey.localChecklist(isDeleted = false)
            val newer = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = accountId, checklist = local, itemList = emptyList())
            checklistTransaction.upsert(accountId = accountId, checklist = newer, itemList = emptyList())
            val pulledNewer = local.copy(isDeleted = true, updatedAt = local.updatedAt + 1.seconds)
            val pulledOlder = newer.copy(isDeleted = true, updatedAt = newer.updatedAt - 1.seconds)
            val cursor = fixtureMonkey.giveMeOne<Long>()

            transaction.upsert(accountId = accountId, checklistList = listOf(pulledNewer, pulledOlder), cursor = cursor)

            database.findChecklistList() shouldBe listOf(pulledNewer, newer).sortedBy { checklist -> checklist.id.toString() }
            cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CHECKLIST) shouldBe cursor
            cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CHECKLIST_ITEM) shouldBe 0L
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 체크리스트는 계정과 연결되어 동기화 완료로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pulled = fixtureMonkey.localChecklist(isDeleted = false)

            transaction.upsert(accountId = accountId, checklistList = listOf(pulled), cursor = fixtureMonkey.giveMeOne<Long>())

            database.findChecklistList() shouldBe listOf(pulled)
            database.findAccountChecklistList() shouldBe
                listOf(AccountChecklistLocalEntity(accountId = accountId, checklistId = pulled.id, isDirty = false))
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }
        test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기와 같으면 서버 내용을 반영한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = accountId, checklist = local, itemList = emptyList())
            val pulled = local.copy(detail = ChecklistDetailLocalEntity(title = "title", description = "description"), isDeleted = true)

            transaction.upsert(accountId = accountId, checklistList = listOf(pulled), cursor = 5L)

            database.findChecklistList() shouldBe listOf(pulled)
        }

        listOf(
            "늦음" to 1.seconds,
            "같음" to 0.seconds,
            "이른" to (-1).seconds,
        ).forEach { (label, offset) ->
            test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = fixtureMonkey.localChecklist(isDeleted = false)
                checklistTransaction.upsert(accountId = accountId, checklist = local, itemList = emptyList())
                val pulled = local.copy(isDeleted = true, updatedAt = local.updatedAt + offset)

                transaction.upsert(accountId = accountId, checklistList = listOf(pulled), cursor = 5L)

                syncDataSource.readPendingList(accountId = accountId).map { checklist -> checklist.id } shouldBe listOf(local.id)
            }
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 체크리스트와 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws ChecklistSyncTestException()
            val failingTransaction = AccountChecklistSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<ChecklistSyncTestException> {
                failingTransaction.upsert(accountId = accountId, checklistList = listOf(fixtureMonkey.localChecklist(isDeleted = false)), cursor = 5L)
            }

            database.findChecklistList().shouldBeEmpty()
            database.findAccountChecklistList().shouldBeEmpty()
            cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CHECKLIST) shouldBe 0L
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

private class ChecklistSyncTestException : RuntimeException()
