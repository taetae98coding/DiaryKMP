package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource.AccountChecklistItemLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction.AccountChecklistItemSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.transaction.AccountChecklistItemTransactionImpl
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountChecklistLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountChecklistLocalDataSourceImpl
        lateinit var checklistTransaction: AccountChecklistTransactionImpl
        lateinit var checklistSyncTransaction: AccountChecklistSyncTransactionImpl
        lateinit var itemTransaction: AccountChecklistItemTransactionImpl
        lateinit var itemSyncTransaction: AccountChecklistItemSyncTransactionImpl
        lateinit var itemDataSource: AccountChecklistItemLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountChecklistLocalDataSourceImpl(database = database)
            checklistTransaction = AccountChecklistTransactionImpl(database = database)
            checklistSyncTransaction = AccountChecklistSyncTransactionImpl(database = database)
            itemTransaction = AccountChecklistItemTransactionImpl(database = database)
            itemSyncTransaction = AccountChecklistItemSyncTransactionImpl(database = database)
            itemDataSource = AccountChecklistItemLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun PagingSource<Int, ChecklistSummaryLocalEntity>.loaded(): List<ChecklistSummaryLocalEntity> =
            load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 100,
                    placeholdersEnabled = false,
                ),
            ).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, ChecklistSummaryLocalEntity>>().data

        suspend fun loaded(accountId: Uuid): List<ChecklistSummaryLocalEntity> = dataSource.page(accountId = accountId).loaded()

        suspend fun assertPageInvalidated(
            accountId: Uuid,
            expected: List<ChecklistSummaryLocalEntity>,
            change: suspend () -> Unit,
        ) {
            val pagingSource = dataSource.page(accountId = accountId)
            pagingSource.loaded()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            loaded(accountId) shouldBe expected
        }

        fun checklist(title: String): ChecklistLocalEntity = fixtureMonkey.localChecklist(isDeleted = false).copy(detail = ChecklistDetailLocalEntity(title = title, description = ""))

        fun item(
            checklistId: Uuid,
            isChecked: Boolean,
            isDeleted: Boolean = false,
        ): ChecklistItemLocalEntity = fixtureMonkey.localChecklistItem(isChecked = isChecked, isDeleted = isDeleted, checklistId = checklistId)

        fun summary(
            checklist: ChecklistLocalEntity,
            checkedCount: Int = 0,
            totalCount: Int = 0,
        ): ChecklistSummaryLocalEntity = ChecklistSummaryLocalEntity(checklist = checklist, checkedCount = checkedCount, totalCount = totalCount)

        test("TC-5474c60f-6cdd-47da-9556-061c02a51d7a TC-b604e41d-9351-4e5c-a118-0dfd772241f1 목록은 계정의 미삭제 체크리스트를 제목 오름차순으로 담는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val b = checklist(title = "b")
            val a = checklist(title = "a")
            val deleted = checklist(title = "0").copy(isDeleted = true)
            val other = checklist(title = "00")
            listOf(b, a, deleted).forEach { checklist ->
                checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())
            }
            checklistTransaction.upsert(accountId = fixtureMonkey.giveMeOne<Uuid>(), checklist = other, itemList = emptyList())

            loaded(accountId) shouldBe listOf(summary(a), summary(b))
        }

        test("TC-5474c60f-6cdd-47da-9556-061c02a51d7a 다른 계정의 체크리스트만 저장되어 있으면 빈 목록으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            checklistTransaction.upsert(accountId = fixtureMonkey.giveMeOne<Uuid>(), checklist = checklist(title = "title"), itemList = emptyList())

            loaded(accountId).shouldBeEmpty()
        }

        // 체크한 항목, 체크하지 않은 항목, 삭제된 체크한 항목, 삭제된 체크하지 않은 항목의 수와 기대하는 진행 정도
        listOf(
            listOf(0, 0, 0, 0) to (0 to 0),
            listOf(2, 1, 0, 0) to (2 to 3),
            listOf(1, 1, 1, 1) to (1 to 2),
            listOf(3, 0, 0, 0) to (3 to 3),
        ).forEach { (countList, expected) ->
            test("TC-ab6c3860-96a1-4bb3-8243-2ab958930e75 체크 항목이 $countList 이면 진행 정도는 ${expected.first}/${expected.second} 다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val checklist = checklist(title = "title")
                val itemList =
                    List(countList[0]) { item(checklistId = checklist.id, isChecked = true) } +
                        List(countList[1]) { item(checklistId = checklist.id, isChecked = false) } +
                        List(countList[2]) { item(checklistId = checklist.id, isChecked = true, isDeleted = true) } +
                        List(countList[3]) { item(checklistId = checklist.id, isChecked = false, isDeleted = true) }
                checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = itemList)

                loaded(accountId) shouldBe listOf(summary(checklist, checkedCount = expected.first, totalCount = expected.second))
            }
        }

        test("TC-b604e41d-9351-4e5c-a118-0dfd772241f1 진행 정도는 목록 순서에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val a = checklist(title = "a")
            val b = checklist(title = "b")
            val c = checklist(title = "c")
            checklistTransaction.upsert(accountId = accountId, checklist = c, itemList = emptyList())
            checklistTransaction.upsert(accountId = accountId, checklist = a, itemList = listOf(item(checklistId = a.id, isChecked = false)))
            checklistTransaction.upsert(
                accountId = accountId,
                checklist = b,
                itemList = List(3) { item(checklistId = b.id, isChecked = true) },
            )

            loaded(accountId) shouldBe
                listOf(
                    summary(a, checkedCount = 0, totalCount = 1),
                    summary(b, checkedCount = 3, totalCount = 3),
                    summary(c),
                )
        }

        test("TC-ffcb4f61-636d-4088-9c8d-3f376f926bbf 요청한 크기만큼 페이지로 나누어 진행 정도와 함께 이어서 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklistList = List(3) { index -> checklist(title = "$index-${fixtureMonkey.giveMeOne<String>()}") }
            checklistList.forEach { checklist ->
                checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(item(checklistId = checklist.id, isChecked = true)))
            }

            val pagingSource = dataSource.page(accountId = accountId)
            val firstPage =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            val nextPage =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = checkNotNull(firstPage.nextKey), loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page

            val expected = checklistList.map { checklist -> summary(checklist, checkedCount = 1, totalCount = 1) }
            firstPage.data shouldBe expected.take(2)
            nextPage.data shouldBe expected.drop(2)
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 노출 기준을 만족하는 체크리스트를 추가하면 제목 순서에 맞는 자리에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val a = checklist(title = "a")
            val b = checklist(title = "b")
            val c = checklist(title = "c")
            checklistTransaction.upsert(accountId = accountId, checklist = c, itemList = emptyList())
            checklistTransaction.upsert(accountId = accountId, checklist = a, itemList = emptyList())

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(a), summary(b), summary(c))) {
                checklistTransaction.upsert(accountId = accountId, checklist = b, itemList = emptyList())
            }
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 목록에 있는 체크리스트가 삭제 상태로 바뀌면 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val a = checklist(title = "a")
            val b = checklist(title = "b")
            listOf(a, b).forEach { checklist -> checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList()) }

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(b))) {
                checklistTransaction.updateDeleted(accountId = accountId, checklistId = a.id, isDeleted = true, updatedAt = instant())
            }
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 삭제 상태였던 체크리스트를 미삭제로 되돌리면 제목 순서에 맞는 자리에 다시 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val a = checklist(title = "a")
            val b = checklist(title = "b")
            val c = checklist(title = "c")
            listOf(a, b, c).forEach { checklist -> checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList()) }
            checklistTransaction.updateDeleted(accountId = accountId, checklistId = b.id, isDeleted = true, updatedAt = instant())
            val restoredAt = instant()

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(a), summary(b.copy(updatedAt = restoredAt)), summary(c))) {
                checklistTransaction.updateDeleted(accountId = accountId, checklistId = b.id, isDeleted = false, updatedAt = restoredAt)
            }
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 목록에 있는 체크리스트의 제목이 수정되면 바뀐 제목 순서에 맞는 자리로 옮겨진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val a = checklist(title = "a")
            val b = checklist(title = "b")
            listOf(a, b).forEach { checklist -> checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList()) }
            val detail = ChecklistDetailLocalEntity(title = "c", description = a.detail.description)
            val updatedAt = instant()

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(b), summary(a.copy(detail = detail, updatedAt = updatedAt)))) {
                checklistTransaction.updateDetail(accountId = accountId, checklistId = a.id, detail = detail, updatedAt = updatedAt)
            }
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 체크 항목이 추가되거나 체크되거나 삭제되면 진행 정도가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = checklist(title = "title")
            val checkedItem = item(checklistId = checklist.id, isChecked = true)
            val uncheckedItem = item(checklistId = checklist.id, isChecked = false)
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = listOf(checkedItem))

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(checklist, checkedCount = 1, totalCount = 2))) {
                itemTransaction.upsert(accountId = accountId, item = uncheckedItem)
            }
            assertPageInvalidated(accountId = accountId, expected = listOf(summary(checklist, checkedCount = 2, totalCount = 2))) {
                itemTransaction.updateChecked(accountId = accountId, itemId = uncheckedItem.id, isChecked = true, updatedAt = instant())
            }
            assertPageInvalidated(accountId = accountId, expected = listOf(summary(checklist, checkedCount = 1, totalCount = 1))) {
                itemTransaction.updateDeleted(accountId = accountId, itemId = checkedItem.id, isDeleted = true, updatedAt = instant())
            }
        }

        test("TC-ddca765c-69f3-4113-b1c5-31b12926b3a3 서버에서 내려받은 체크리스트와 체크 항목은 노출, 정렬, 진행 정도 기준에 따라 목록에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val a = checklist(title = "a")
            val c = checklist(title = "c")
            listOf(a, c).forEach { checklist -> checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList()) }
            val downloaded = checklist(title = "b")
            val downloadedDeleted = checklist(title = "0").copy(isDeleted = true)
            val downloadedItem = item(checklistId = a.id, isChecked = true)

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(a), summary(downloaded), summary(c))) {
                checklistSyncTransaction.upsert(accountId = accountId, checklistList = listOf(downloadedDeleted, downloaded), cursor = 1L)
            }
            assertPageInvalidated(
                accountId = accountId,
                expected = listOf(summary(a, checkedCount = 1, totalCount = 1), summary(downloaded), summary(c)),
            ) {
                itemSyncTransaction.upsert(accountId = accountId, itemList = listOf(downloadedItem), cursor = 1L)
            }
        }

        test("TC-d6355518-e072-4377-94c1-d1b18f25a912 체크리스트보다 먼저 내려받은 체크 항목은 체크리스트가 반영되어야 그 체크리스트에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val existing = checklist(title = "a")
            checklistTransaction.upsert(accountId = accountId, checklist = existing, itemList = emptyList())
            val downloaded = checklist(title = "b")
            val downloadedItem = item(checklistId = downloaded.id, isChecked = true)

            itemSyncTransaction.upsert(accountId = accountId, itemList = listOf(downloadedItem), cursor = 1L)

            loaded(accountId) shouldBe listOf(summary(existing))
            itemDataSource.get(accountId = accountId, checklistId = existing.id, sort = ChecklistItemSortLocalEntity.ADDED).test {
                awaitItem().shouldBeEmpty()
            }

            assertPageInvalidated(accountId = accountId, expected = listOf(summary(existing), summary(downloaded, checkedCount = 1, totalCount = 1))) {
                checklistSyncTransaction.upsert(accountId = accountId, checklistList = listOf(downloaded), cursor = 1L)
            }
            itemDataSource.get(accountId = accountId, checklistId = downloaded.id, sort = ChecklistItemSortLocalEntity.ADDED).test {
                awaitItem() shouldBe listOf(downloadedItem)
            }
        }

        test("TC-3b75e95e-f535-4154-bfeb-5e95f65cdea4 상세 조회는 삭제 상태인 체크리스트도 현재 계정 기준으로 찾는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val deleted = fixtureMonkey.localChecklist(isDeleted = true)
            checklistTransaction.upsert(accountId = accountId, checklist = deleted, itemList = emptyList())

            dataSource.find(accountId = accountId, checklistId = deleted.id).test {
                awaitItem() shouldBe deleted
            }
        }

        test("TC-cd7f5929-faaf-4830-9230-c34621b1f7ab 현재 계정과 연결되지 않은 체크리스트는 찾지 못한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val other = fixtureMonkey.localChecklist(isDeleted = false)
            checklistTransaction.upsert(accountId = fixtureMonkey.giveMeOne<Uuid>(), checklist = other, itemList = emptyList())

            dataSource.find(accountId = accountId, checklistId = other.id).test {
                awaitItem().shouldBeNull()
            }
        }

        test("찾은 체크리스트는 저장 내용이 바뀌면 바뀐 내용으로 다시 전달된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val checklist = checklist(title = "a")
            checklistTransaction.upsert(accountId = accountId, checklist = checklist, itemList = emptyList())
            val detail = ChecklistDetailLocalEntity(title = "b", description = "description")
            val updatedAt = instant()

            dataSource.find(accountId = accountId, checklistId = checklist.id).test {
                awaitItem() shouldBe checklist
                checklistTransaction.updateDetail(accountId = accountId, checklistId = checklist.id, detail = detail, updatedAt = updatedAt)
                awaitItem() shouldBe checklist.copy(detail = detail, updatedAt = updatedAt)
            }
        }
    }) {
    public companion object {
        private const val INVALIDATION_TIMEOUT_MILLIS = 30_000L

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
