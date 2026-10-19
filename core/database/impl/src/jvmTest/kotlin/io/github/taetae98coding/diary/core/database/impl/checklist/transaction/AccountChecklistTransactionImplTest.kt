package io.github.taetae98coding.diary.core.database.impl.checklist.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.datasource.AccountChecklistLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource.AccountChecklistItemLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountChecklistTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountChecklistTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountChecklistTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-CHECKLIST-ADD-DATA-001 TC-CHECKLIST-ADD-DATA-004 TC-CHECKLIST-ADD-DOMAIN-012 TC-DATA-SYNC-DOMAIN-001 체크리스트와 체크 항목, 각각의 현재 계정 연결을 업로드 대기 상태로 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val itemList = List(3) { fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id) }

            transaction.upsert(accountId = accountId, checklist = checklist, itemList = itemList)

            database.findChecklistList() shouldBe listOf(checklist)
            database.findChecklistItemList() shouldBe itemList.sortedBy { item -> item.id.toString() }
            database.findAccountChecklistList() shouldBe
                listOf(AccountChecklistLocalEntity(accountId = accountId, checklistId = checklist.id, isDirty = true))
            database.findAccountChecklistItemList() shouldBe
                itemList
                    .map { item -> AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = true) }
                    .sortedBy { accountItem -> accountItem.checklistItemId.toString() }
        }

        test("TC-CHECKLIST-ADD-DOMAIN-005 체크 항목 없이도 체크리스트를 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)

            transaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())

            database.findChecklistList() shouldBe listOf(checklist)
            database.findChecklistItemList() shouldBe emptyList()
        }

        test("TC-CHECKLIST-DETAIL-DOMAIN-006 TC-CHECKLIST-DETAIL-DATA-005 TC-DATA-SYNC-DOMAIN-001 수정은 제목, 설명과 수정 시각만 바꾸고 업로드 대기로 기록한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)
            transaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item))
            database.markChecklistUploaded(accountId = accountId)
            val detail = ChecklistDetailLocalEntity(title = "  ${fixtureMonkey.giveMeOne<String>()}  ", description = "")
            val updatedAt = instant()

            transaction.updateDetail(accountId = accountId, checklistId = checklist.id, detail = detail, updatedAt = updatedAt) shouldBe 1

            database.findChecklistList() shouldBe listOf(checklist.copy(detail = detail, updatedAt = updatedAt))
            database.findChecklistItemList() shouldBe listOf(item)
            database.findAccountChecklistList() shouldBe
                listOf(AccountChecklistLocalEntity(accountId = accountId, checklistId = checklist.id, isDirty = true))
            database.findAccountChecklistItemList() shouldBe
                listOf(AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = false))
        }

        listOf(true, false).forEach { isDeleted ->
            test("TC-CHECKLIST-HOME-DOMAIN-006 TC-CHECKLIST-HOME-DOMAIN-007 TC-CHECKLIST-HOME-DATA-004 TC-CHECKLIST-DETAIL-DOMAIN-012 TC-DATA-SYNC-DOMAIN-001 삭제 여부를 $isDeleted 로 바꾸면 수정 시각과 함께 저장하고 체크 항목은 바꾸지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val checklist = fixtureMonkey.localChecklist(isDeleted = !isDeleted)
                val item = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)
                transaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item))
                database.markChecklistUploaded(accountId = accountId)
                val updatedAt = instant()

                transaction.updateDeleted(accountId = accountId, checklistId = checklist.id, isDeleted = isDeleted, updatedAt = updatedAt) shouldBe 1

                database.findChecklistList() shouldBe listOf(checklist.copy(isDeleted = isDeleted, updatedAt = updatedAt))
                database.findChecklistItemList() shouldBe listOf(item)
                database.findAccountChecklistList() shouldBe
                    listOf(AccountChecklistLocalEntity(accountId = accountId, checklistId = checklist.id, isDirty = true))
                database.findAccountChecklistItemList() shouldBe
                    listOf(AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = item.id, isDirty = false))
            }
        }

        test("TC-CHECKLIST-DETAIL-DATA-001 다른 계정의 체크리스트는 수정하거나 삭제하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            transaction.upsert(accountId = otherAccountId, checklist = checklist, itemList = emptyList())
            database.markChecklistUploaded(accountId = otherAccountId)

            transaction.updateDetail(
                accountId = accountId,
                checklistId = checklist.id,
                detail = ChecklistDetailLocalEntity(title = "title", description = "description"),
                updatedAt = instant(),
            ) shouldBe 0
            transaction.updateDeleted(accountId = accountId, checklistId = checklist.id, isDeleted = true, updatedAt = instant()) shouldBe 0

            database.findChecklistList() shouldBe listOf(checklist)
            database.findAccountChecklistList() shouldBe
                listOf(AccountChecklistLocalEntity(accountId = otherAccountId, checklistId = checklist.id, isDirty = false))
        }
        test("TC-CHECKLIST-DETAIL-DATA-001 기기에 없는 체크리스트는 수정하거나 삭제하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val missingChecklistId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.updateDetail(
                accountId = accountId,
                checklistId = missingChecklistId,
                detail = ChecklistDetailLocalEntity(title = "title", description = "description"),
                updatedAt = instant(),
            ) shouldBe 0
            transaction.updateDeleted(accountId = accountId, checklistId = missingChecklistId, isDeleted = true, updatedAt = instant()) shouldBe 0

            database.findChecklistList() shouldBe emptyList()
            database.findAccountChecklistList() shouldBe emptyList()
        }

        test("TC-CHECKLIST-ADD-DOMAIN-007 제목과 설명을 앞뒤 공백과 줄바꿈을 포함해 입력한 그대로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = ChecklistDetailLocalEntity(title = " 여행 준비 ", description = "여름\n휴가 ")
            val checklist = fixtureMonkey.localChecklist(isDeleted = false).copy(detail = detail)

            transaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())

            database.findChecklistList().map { saved -> saved.detail } shouldBe listOf(detail)
        }

        test("TC-CHECKLIST-ADD-DOMAIN-006 같은 내용의 체크리스트를 추가해도 이미 저장된 체크리스트와 체크 항목을 덮어쓰지 않고 따로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val existing = fixtureMonkey.localChecklist(isDeleted = false)
            val existingItem = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = existing.id)
            transaction.upsert(accountId = accountId, checklist = existing, itemList = listOf(existingItem))
            val added = fixtureMonkey.localChecklist(isDeleted = false).copy(detail = existing.detail)
            val addedItem =
                fixtureMonkey
                    .localChecklistItem(isChecked = false, isDeleted = false, checklistId = added.id)
                    .copy(content = existingItem.content)

            transaction.upsert(accountId = accountId, checklist = added, itemList = listOf(addedItem))

            database.findChecklistList() shouldBe listOf(existing, added).sortedBy { checklist -> checklist.id.toString() }
            database.findChecklistItemList() shouldBe listOf(existingItem, addedItem).sortedBy { item -> item.id.toString() }
        }

        test("TC-CHECKLIST-ADD-DATA-003 저장한 체크리스트와 그 체크 항목을 함께 조회할 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val itemList =
                List(2) { index ->
                    fixtureMonkey
                        .localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
                        .copy(createdAt = Instant.fromEpochSeconds(index.toLong()))
                }

            transaction.upsert(accountId = accountId, checklist = checklist, itemList = itemList)

            AccountChecklistLocalDataSourceImpl(database = database).find(accountId = accountId, checklistId = checklist.id).first() shouldBe checklist
            AccountChecklistItemLocalDataSourceImpl(database = database)
                .get(accountId = accountId, checklistId = checklist.id, sort = ChecklistItemSortLocalEntity.ADDED)
                .first() shouldBe itemList
        }

        test("TC-CHECKLIST-ADD-DATA-002 저장에 실패하면 체크리스트, 체크 항목과 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val itemList = List(2) { fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id) }
            val failingDatabase = spyk(database)
            every { failingDatabase.accountChecklistItemDao() } throws ChecklistTestException()
            val failingTransaction = AccountChecklistTransactionImpl(database = failingDatabase)

            shouldThrowExactly<ChecklistTestException> {
                failingTransaction.upsert(accountId = accountId, checklist = checklist, itemList = itemList)
            }

            database.findChecklistList() shouldBe emptyList()
            database.findChecklistItemList() shouldBe emptyList()
            database.findAccountChecklistList() shouldBe emptyList()
            database.findAccountChecklistItemList() shouldBe emptyList()
        }

        test("TC-CHECKLIST-DETAIL-DOMAIN-002 삭제 상태인 체크리스트를 수정해도 삭제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = true)
            transaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())
            val detail = ChecklistDetailLocalEntity(title = "title", description = "description")
            val updatedAt = instant()

            transaction.updateDetail(accountId = accountId, checklistId = checklist.id, detail = detail, updatedAt = updatedAt) shouldBe 1

            database.findChecklistList() shouldBe listOf(checklist.copy(detail = detail, updatedAt = updatedAt))
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}

private class ChecklistTestException : RuntimeException()
