package io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction.AccountChecklistItemTransactionImpl
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountChecklistItemLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountChecklistItemLocalDataSourceImpl
        lateinit var checklistTransaction: AccountChecklistTransactionImpl
        lateinit var itemTransaction: AccountChecklistItemTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountChecklistItemLocalDataSourceImpl(database = database)
            checklistTransaction = AccountChecklistTransactionImpl(database = database)
            itemTransaction = AccountChecklistItemTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        fun item(
            checklistId: Uuid,
            isChecked: Boolean,
            createdAtSeconds: Long,
            isDeleted: Boolean = false,
        ): ChecklistItemLocalEntity =
            fixtureMonkey
                .localChecklistItem(isChecked = isChecked, isDeleted = isDeleted, checklistId = checklistId)
                .copy(createdAt = Instant.fromEpochSeconds(createdAtSeconds))

        test("TC-9596835f-24cf-4a58-86a3-0c84c04d05db TC-289d5d96-f27f-49bf-8b57-dbc8e44c77e6 추가순은 그 체크리스트의 미삭제 항목을 체크 여부와 관계없이 생성 시각 오름차순으로 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val first = item(checklistId = checklist.id, isChecked = true, createdAtSeconds = 1)
            val second = item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 2)
            val third = item(checklistId = checklist.id, isChecked = true, createdAtSeconds = 3)
            val deleted = item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 0, isDeleted = true)
            val otherChecklistItem = item(checklistId = fixtureMonkey.giveMeOne<Uuid>(), isChecked = false, createdAtSeconds = 0)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(third, deleted, first, second))
            checklistTransaction.upsert(
                accountId = accountId,
                checklist = fixtureMonkey.localChecklist(isDeleted = false).copy(id = otherChecklistItem.checklistId),
                itemList = listOf(otherChecklistItem),
            )

            dataSource.get(accountId = accountId, checklistId = checklist.id, sort = ChecklistItemSortLocalEntity.ADDED).test {
                awaitItem() shouldBe listOf(first, second, third)
            }
        }

        test("TC-289d5d96-f27f-49bf-8b57-dbc8e44c77e6 체크 안 된 항목 우선은 체크되지 않은 항목을 앞에 두고 각 묶음 안은 추가순이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val first = item(checklistId = checklist.id, isChecked = true, createdAtSeconds = 1)
            val second = item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 2)
            val third = item(checklistId = checklist.id, isChecked = true, createdAtSeconds = 3)
            val fourth = item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 4)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(first, second, third, fourth))

            dataSource.get(accountId = accountId, checklistId = checklist.id, sort = ChecklistItemSortLocalEntity.UNCHECKED_FIRST).test {
                awaitItem() shouldBe listOf(second, fourth, first, third)
            }
        }

        test("체크 상태가 바뀌면 고른 정렬에 맞게 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val first = item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 1)
            val second = item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 2)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(first, second))
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()

            dataSource.get(accountId = accountId, checklistId = checklist.id, sort = ChecklistItemSortLocalEntity.UNCHECKED_FIRST).test {
                awaitItem() shouldBe listOf(first, second)

                itemTransaction.updateChecked(accountId = accountId, itemId = first.id, isChecked = true, updatedAt = updatedAt)

                awaitItem() shouldBe listOf(second, first.copy(isChecked = true, updatedAt = updatedAt))
            }
        }

        test("TC-9596835f-24cf-4a58-86a3-0c84c04d05db 다른 계정과만 연결된 체크 항목은 담지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(
                accountId = fixtureMonkey.giveMeOne<Uuid>(),
                checklist = checklist,
                itemList = listOf(item(checklistId = checklist.id, isChecked = false, createdAtSeconds = 1)),
            )

            dataSource.get(accountId = accountId, checklistId = checklist.id, sort = ChecklistItemSortLocalEntity.ADDED).test {
                awaitItem() shouldBe emptyList()
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
