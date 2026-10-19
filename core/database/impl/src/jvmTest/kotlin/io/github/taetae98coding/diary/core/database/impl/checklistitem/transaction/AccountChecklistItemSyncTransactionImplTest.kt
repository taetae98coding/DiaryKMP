package io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findAccountChecklistItemList
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findChecklistItemList
import io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource.AccountChecklistItemSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class AccountChecklistItemSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountChecklistItemSyncTransactionImpl
        lateinit var checklistTransaction: AccountChecklistTransactionImpl
        lateinit var itemTransaction: AccountChecklistItemTransactionImpl
        lateinit var syncDataSource: AccountChecklistItemSyncLocalDataSourceImpl
        lateinit var cursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountChecklistItemSyncTransactionImpl(database = database)
            checklistTransaction = AccountChecklistTransactionImpl(database = database)
            itemTransaction = AccountChecklistItemTransactionImpl(database = database)
            syncDataSource = AccountChecklistItemSyncLocalDataSourceImpl(database = database)
            cursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-DATA-SYNC-DOMAIN-087 업로드 대상은 그 계정의 업로드 대기 체크 항목뿐이고 게스트 상태에서 만든 체크 항목은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val guestChecklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            val guestItem = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = guestChecklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item))
            checklistTransaction.upsert(accountId = Uuid.NIL, checklist = guestChecklist, itemList = listOf(guestItem))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(item)
        }

        test("TC-DATA-SYNC-DOMAIN-026 TC-DATA-SYNC-DOMAIN-027 업로드한 수정 시각이 그대로면 동기화 완료가 되고, 그사이 체크가 바뀌었으면 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val unchanged = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            val changed = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(unchanged, changed))
            itemTransaction.updateChecked(
                accountId = accountId,
                itemId = changed.id,
                isChecked = true,
                updatedAt = changed.updatedAt + 1.seconds,
            )

            transaction.clearPending(accountId = accountId, itemList = listOf(unchanged, changed))

            syncDataSource.readPendingList(accountId = accountId).map { item -> item.id } shouldBe listOf(changed.id)
        }

        test("TC-DATA-SYNC-DATA-022 TC-DATA-SYNC-DATA-023 TC-DATA-SYNC-DATA-028 내려받은 체크 항목은 항목 하나 단위로 수정 시각을 비교해 반영하고 체크 항목 커서만 전진한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val local = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            val newer = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(local, newer))
            val pulledNewer = local.copy(isChecked = true, updatedAt = local.updatedAt + 1.seconds)
            val pulledOlder = newer.copy(isChecked = true, updatedAt = newer.updatedAt - 1.seconds)
            val cursor = fixtureMonkey.giveMeOne<Long>()

            transaction.upsert(accountId = accountId, itemList = listOf(pulledNewer, pulledOlder), cursor = cursor)

            database.findChecklistItemList() shouldBe listOf(pulledNewer, newer).sortedBy { item -> item.id.toString() }
            cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CHECKLIST_ITEM) shouldBe cursor
            cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CHECKLIST) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-025 체크리스트보다 먼저 내려받은 체크 항목도 계정과 연결되어 동기화 완료로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pulled = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false)

            transaction.upsert(accountId = accountId, itemList = listOf(pulled), cursor = fixtureMonkey.giveMeOne<Long>())

            database.findChecklistItemList() shouldBe listOf(pulled)
            database.findAccountChecklistItemList() shouldBe
                listOf(AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = pulled.id, isDirty = false))
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }
        test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기와 같으면 서버 내용을 반영한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val local = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(local))
            val pulled = local.copy(content = "content", isChecked = true, isDeleted = true)

            transaction.upsert(accountId = accountId, itemList = listOf(pulled), cursor = 5L)

            database.findChecklistItemList() shouldBe listOf(pulled)
        }

        listOf(
            "늦음" to 1.seconds,
            "같음" to 0.seconds,
            "이른" to (-1).seconds,
        ).forEach { (label, offset) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val checklist = fixtureMonkey.localChecklist(isDeleted = false)
                val local = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
                checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(local))
                val pulled = local.copy(isChecked = true, updatedAt = local.updatedAt + offset)

                transaction.upsert(accountId = accountId, itemList = listOf(pulled), cursor = 5L)

                syncDataSource.readPendingList(accountId = accountId).map { item -> item.id } shouldBe listOf(local.id)
            }
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 체크 항목과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws ChecklistItemSyncTestException()
            val failingTransaction = AccountChecklistItemSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<ChecklistItemSyncTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    itemList = listOf(fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false)),
                    cursor = 5L,
                )
            }

            database.findChecklistItemList().shouldBeEmpty()
            database.findAccountChecklistItemList().shouldBeEmpty()
            cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CHECKLIST_ITEM) shouldBe 0L
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

private class ChecklistItemSyncTestException : RuntimeException()
