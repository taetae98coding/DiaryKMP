package io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findAccountChecklistItemList
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findAccountChecklistList
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findChecklistItemList
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findChecklistList
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.markChecklistUploaded
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountChecklistItemTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var checklistTransaction: AccountChecklistTransactionImpl
        lateinit var transaction: AccountChecklistItemTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            checklistTransaction = AccountChecklistTransactionImpl(database = database)
            transaction = AccountChecklistItemTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-93f77e96-7b62-406b-8564-b3ddc37fbbfb TC-a0aea019-81b3-42f1-a8fc-133ba9b26542 TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367 TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 현재 계정과 연결된 체크리스트에 체크 항목을 넣으면 항목과 계정 연결을 업로드 대기로 저장하고 체크리스트는 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)

            database.markChecklistUploaded(accountId = accountId)

            transaction.upsert(accountId = accountId, item = item) shouldBe 1

            database.findChecklistList() shouldBe listOf(checklist)
            database.findAccountChecklistList() shouldBe
                listOf(AccountChecklistLocalEntity(accountId = accountId, checklistId = checklist.id, isDirty = false))
            database.findChecklistItemList() shouldBe listOf(item)
            database.findAccountChecklistItemList() shouldBe
                listOf(AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = true))
        }

        test("TC-fd1241c1-0667-4741-a1de-10602a660b42 현재 계정과 연결되지 않은 체크리스트에는 체크 항목을 넣지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = otherAccountId, checklist = checklist, itemList = emptyList())
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)

            transaction.upsert(accountId = accountId, item = item) shouldBe 0

            database.findChecklistItemList() shouldBe emptyList()
            database.findAccountChecklistItemList() shouldBe emptyList()
        }

        listOf(true, false).forEach { isChecked ->
            test("TC-7becaae5-50d1-46ca-874a-97280e482e04 TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367 TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 체크 여부를 $isChecked 로 바꾸면 그 항목의 수정 시각과 함께 저장하고 업로드 대기로 기록한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val checklist = fixtureMonkey.localChecklist(isDeleted = false)
                val item = fixtureMonkey.localChecklistItem(isChecked = !isChecked, isDeleted = false, checklistId = checklist.id)
                val otherItem = fixtureMonkey.localChecklistItem(isChecked = !isChecked, isDeleted = false, checklistId = checklist.id)
                checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item, otherItem))
                database.markChecklistUploaded(accountId = accountId)
                val updatedAt = instant()

                transaction.updateChecked(accountId = accountId, itemId = item.id, isChecked = isChecked, updatedAt = updatedAt) shouldBe 1

                database.findChecklistList() shouldBe listOf(checklist)
                database.findChecklistItemList() shouldBe
                    listOf(item.copy(isChecked = isChecked, updatedAt = updatedAt), otherItem).sortedBy { it.id.toString() }
                database.findAccountChecklistItemList() shouldBe
                    listOf(
                        AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = true),
                        AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = otherItem.id, isDirty = false),
                    ).sortedBy { it.checklistItemId.toString() }
            }
        }

        test("TC-d7bffc1e-30a6-4f1a-8dae-588da8f7d911 TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367 TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 항목 빼기는 삭제 여부와 수정 시각을 저장하고 항목과 계정 연결을 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item))
            database.markChecklistUploaded(accountId = accountId)
            val updatedAt = instant()

            transaction.updateDeleted(accountId = accountId, itemId = item.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            database.findChecklistList() shouldBe listOf(checklist)
            database.findChecklistItemList() shouldBe listOf(item.copy(isDeleted = true, updatedAt = updatedAt))
            database.findAccountChecklistItemList() shouldBe
                listOf(AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = true))
        }

        test("TC-fd1241c1-0667-4741-a1de-10602a660b42 다른 계정의 체크 항목은 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = otherAccountId, checklist = checklist, itemList = listOf(item))

            transaction.updateChecked(accountId = accountId, itemId = item.id, isChecked = true, updatedAt = instant()) shouldBe 0
            transaction.updateDeleted(accountId = accountId, itemId = item.id, isDeleted = true, updatedAt = instant()) shouldBe 0
            transaction.updateAllChecked(accountId = accountId, checklistId = checklist.id, isChecked = true, updatedAt = instant()) shouldBe 0

            database.findChecklistItemList() shouldBe listOf(item)
        }

        test("TC-fd1241c1-0667-4741-a1de-10602a660b42 다른 계정과만 연결된 체크리스트는 리셋하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = otherAccountId, checklist = checklist, itemList = listOf(item))
            database.markChecklistUploaded(accountId = otherAccountId)

            transaction.updateAllChecked(accountId = accountId, checklistId = checklist.id, isChecked = false, updatedAt = instant()) shouldBe 0

            database.findChecklistItemList() shouldBe listOf(item)
            database.findAccountChecklistItemList() shouldBe
                listOf(AccountChecklistItemLocalEntity(accountId = otherAccountId, checklistItemId = item.id, isDirty = false))
        }

        test("TC-c81f2cd3-659c-4d52-b6b4-571337e12f0b TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367 TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 리셋은 체크된 미삭제 항목만 체크하지 않음으로 바꾸고 바꾼 항목만 수정 시각과 업로드 대기를 기록한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val otherChecklist = fixtureMonkey.localChecklist(isDeleted = false)
            val checkedItemList = List(2) { fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id) }
            val uncheckedItem = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            val deletedCheckedItem = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = true, checklistId = checklist.id)
            val otherChecklistItem = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = otherChecklist.id)
            checklistTransaction.upsert(
                accountId = accountId,
                checklist = checklist,
                itemList = checkedItemList + uncheckedItem + deletedCheckedItem,
            )
            checklistTransaction.upsert(accountId = accountId, checklist = otherChecklist, itemList = listOf(otherChecklistItem))
            database.markChecklistUploaded(accountId = accountId)
            val updatedAt = instant()

            transaction.updateAllChecked(accountId = accountId, checklistId = checklist.id, isChecked = false, updatedAt = updatedAt) shouldBe 2

            database.findChecklistList() shouldBe listOf(checklist, otherChecklist).sortedBy { it.id.toString() }
            database.findChecklistItemList() shouldBe
                (
                    checkedItemList.map { item -> item.copy(isChecked = false, updatedAt = updatedAt) } +
                        listOf(uncheckedItem, deletedCheckedItem, otherChecklistItem)
                ).sortedBy { it.id.toString() }
            database
                .findAccountChecklistItemList()
                .filter { accountItem -> accountItem.isDirty }
                .map { it.checklistItemId }
                .toSet() shouldBe
                checkedItemList.map { item -> item.id }.toSet()
        }

        test("체크된 항목이 없으면 리셋은 아무것도 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item))
            database.markChecklistUploaded(accountId = accountId)

            transaction.updateAllChecked(accountId = accountId, checklistId = checklist.id, isChecked = false, updatedAt = instant()) shouldBe 0

            database.findChecklistItemList() shouldBe listOf(item)
            database.findAccountChecklistItemList() shouldBe
                listOf(AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = false))
        }
        test("TC-fd1241c1-0667-4741-a1de-10602a660b42 기기에 없는 체크리스트에는 항목을 넣거나 리셋하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val missingChecklistId = fixtureMonkey.giveMeOne<Uuid>()
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = missingChecklistId)

            transaction.upsert(accountId = accountId, item = item) shouldBe 0
            transaction.updateAllChecked(accountId = accountId, checklistId = missingChecklistId, isChecked = false, updatedAt = instant()) shouldBe 0

            database.findChecklistItemList() shouldBe emptyList()
            database.findAccountChecklistItemList() shouldBe emptyList()
        }

        test("TC-3d921233-a531-41b1-b83d-2c31a1a8bc30 같은 내용의 항목도 다른 식별 정보의 새 항목으로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val content = fixtureMonkey.giveMeOne<String>()
            val existing = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id).copy(content = content)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(existing))
            val added = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id).copy(content = content)

            transaction.upsert(accountId = accountId, item = added) shouldBe 1

            database.findChecklistItemList() shouldBe listOf(existing, added).sortedBy { it.id.toString() }
        }

        test("TC-7c4b3430-9fee-4392-910d-658b3307c5a8 삭제 상태인 체크리스트에서 항목을 바꿔도 체크리스트는 삭제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = true)
            val item = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)
            val otherItem = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item, otherItem))

            transaction.upsert(
                accountId = accountId,
                item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id),
            ) shouldBe 1
            transaction.updateChecked(accountId = accountId, itemId = item.id, isChecked = false, updatedAt = instant()) shouldBe 1
            transaction.updateDeleted(accountId = accountId, itemId = item.id, isDeleted = true, updatedAt = instant()) shouldBe 1
            transaction.updateAllChecked(accountId = accountId, checklistId = checklist.id, isChecked = false, updatedAt = instant()) shouldBe 1

            database.findChecklistList() shouldBe listOf(checklist)
        }

        test("TC-a0aea019-81b3-42f1-a8fc-133ba9b26542 항목 넣기의 저장이 실패하면 새 항목과 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountChecklistItemDao() } throws ChecklistItemTestException()
            val failingTransaction = AccountChecklistItemTransactionImpl(database = failingDatabase)

            shouldThrowExactly<ChecklistItemTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id),
                )
            }

            database.findChecklistItemList() shouldBe emptyList()
            database.findAccountChecklistItemList() shouldBe emptyList()
        }

        test("TC-827eaa71-b4ae-4a73-b7c2-84062a8552ec 리셋의 저장이 도중에 실패하면 모든 항목이 체크된 채로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val itemList = List(3) { fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id) }
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = itemList)
            database.markChecklistUploaded(accountId = accountId)
            val failingDao = spyk(database.accountChecklistItemDao())
            coEvery { failingDao.markPending(accountId = any(), itemIdList = any()) } throws ChecklistItemTestException()
            val failingDatabase = spyk(database)
            every { failingDatabase.accountChecklistItemDao() } returns failingDao
            val failingTransaction = AccountChecklistItemTransactionImpl(database = failingDatabase)

            shouldThrowExactly<ChecklistItemTestException> {
                failingTransaction.updateAllChecked(accountId = accountId, checklistId = checklist.id, isChecked = false, updatedAt = instant())
            }

            database.findChecklistItemList() shouldBe itemList.sortedBy { it.id.toString() }
            database.findAccountChecklistItemList().none { accountItem -> accountItem.isDirty } shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}

private class ChecklistItemTestException : RuntimeException()
