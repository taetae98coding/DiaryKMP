package io.github.taetae98coding.diary.data.checklist.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountChecklistLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.transaction.AccountChecklistTransaction
import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.transaction.AccountChecklistItemTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.data.checklist.mapper.toDomain
import io.github.taetae98coding.diary.data.checklist.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ChecklistRepositoryImplTest :
    FunSpec({
        test("TC-CHECKLIST-ADD-DATA-001 TC-CHECKLIST-ADD-DOMAIN-012 체크리스트와 체크 항목을 현재 계정 식별자와 로컬 모델로 바꿔 함께 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val checklist = fixtureMonkey.checklist(isDeleted = false)
            val itemList = List(2) { fixtureMonkey.checklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id) }
            val transaction = mockk<AccountChecklistTransaction>()
            coEvery { transaction.upsert(accountId = any(), checklist = any(), itemList = any()) } just Runs
            val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = mockk(), accountChecklistTransaction = transaction)

            repository.upsert(account = account, checklist = checklist, itemList = itemList)

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, checklist = checklist.toLocal(), itemList = itemList.map { item -> item.toLocal() })
            }
        }

        test("TC-CHECKLIST-DETAIL-DATA-002 수정의 기기 저장이 실패하면 실패를 그대로 전달한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountChecklistTransaction>()
            coEvery { transaction.updateDetail(accountId = any(), checklistId = any(), detail = any(), updatedAt = any()) } throws throwable
            val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = mockk(), accountChecklistTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateDetail(
                    account = fixtureMonkey.giveMeOne<Account.User>(),
                    checklistId = fixtureMonkey.giveMeOne<Uuid>(),
                    detail = fixtureMonkey.checklistDetail(),
                    updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                )
            } shouldBeSameInstanceAs throwable
        }

        test("TC-CHECKLIST-DETAIL-DOMAIN-001 체크리스트 하나를 삭제 여부와 관계없이 도메인 모델로 바꿔 전달하고, 없으면 없음을 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val checklist = fixtureMonkey.checklist(isDeleted = true)
            val missingId = fixtureMonkey.giveMeOne<Uuid>()
            val dataSource = mockk<AccountChecklistLocalDataSource>()
            every { dataSource.find(accountId = account.id, checklistId = checklist.id) } returns flowOf(checklist.toLocal())
            every { dataSource.find(accountId = account.id, checklistId = missingId) } returns flowOf(null)
            val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = dataSource, accountChecklistTransaction = mockk())

            repository.find(account = account, checklistId = checklist.id).first() shouldBe checklist
            repository.find(account = account, checklistId = missingId).first().shouldBeNull()
        }

        test("TC-CHECKLIST-DETAIL-DOMAIN-004 체크 항목 목록을 고른 정렬의 로컬 정렬로 조회해 도메인 모델로 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val checklistId = fixtureMonkey.giveMeOne<Uuid>()
            val itemList = List(2) { fixtureMonkey.checklistItem(isChecked = true, isDeleted = false, checklistId = checklistId) }
            val dataSource = mockk<AccountChecklistItemLocalDataSource>()
            every {
                dataSource.get(accountId = account.id, checklistId = checklistId, sort = ChecklistItemSortLocalEntity.UNCHECKED_FIRST)
            } returns flowOf(itemList.map { item -> item.toLocal() })
            val repository =
                AccountChecklistItemRepositoryImpl(accountChecklistItemLocalDataSource = dataSource, accountChecklistItemTransaction = mockk())

            repository.get(account = account, checklistId = checklistId, sort = ChecklistItemSort.UNCHECKED_FIRST).first() shouldBe itemList
        }

        test("체크 항목 변경을 현재 계정 식별자로 기기 저장에 넘기고 바뀐 개수를 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val item = fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountChecklistItemTransaction>()
            coEvery { transaction.upsert(accountId = account.id, item = item.toLocal()) } returns 1
            coEvery { transaction.updateChecked(accountId = account.id, itemId = item.id, isChecked = true, updatedAt = updatedAt) } returns 1
            coEvery { transaction.updateDeleted(accountId = account.id, itemId = item.id, isDeleted = true, updatedAt = updatedAt) } returns 1
            coEvery {
                transaction.updateAllChecked(accountId = account.id, checklistId = item.checklistId, isChecked = false, updatedAt = updatedAt)
            } returns 2
            val repository =
                AccountChecklistItemRepositoryImpl(accountChecklistItemLocalDataSource = mockk(), accountChecklistItemTransaction = transaction)

            repository.upsert(account = account, item = item) shouldBe 1
            repository.updateChecked(account = account, itemId = item.id, isChecked = true, updatedAt = updatedAt) shouldBe 1
            repository.updateDeleted(account = account, itemId = item.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1
            repository.updateAllChecked(account = account, checklistId = item.checklistId, isChecked = false, updatedAt = updatedAt) shouldBe 2
        }
        test("TC-CHECKLIST-ADD-DATA-002 체크리스트 추가의 기기 저장이 실패하면 추가를 성공으로 다루지 않고 실패를 전달한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountChecklistTransaction>()
            coEvery { transaction.upsert(accountId = any(), checklist = any(), itemList = any()) } throws throwable
            val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = mockk(), accountChecklistTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.upsert(
                    account = fixtureMonkey.giveMeOne<Account.User>(),
                    checklist = fixtureMonkey.checklist(isDeleted = false),
                    itemList = listOf(fixtureMonkey.checklistItem(isChecked = false, isDeleted = false)),
                )
            } shouldBeSameInstanceAs throwable
        }

        test("TC-CHECKLIST-HOME-DATA-001 체크리스트 목록 페이지 조회는 현재 계정의 로컬 체크리스트를 진행 정도와 함께 도메인 모델로 바꿔 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localList =
                List(2) { index ->
                    ChecklistSummaryLocalEntity(checklist = fixtureMonkey.checklist(isDeleted = false).toLocal(), checkedCount = index, totalCount = 2)
                }
            val dataSource = mockk<AccountChecklistLocalDataSource>()
            every { dataSource.page(accountId = account.id) } returns pagingSource(localList)
            val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = dataSource, accountChecklistTransaction = mockk())

            repository.page(account = account).first().items() shouldBe localList.map { local -> local.toDomain() }
        }

        test("TC-CHECKLIST-DETAIL-DOMAIN-006 수정 요청은 제목, 설명과 수정 시각을 현재 계정 기준의 로컬 모델로 반영한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val checklistId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = fixtureMonkey.checklistDetail()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountChecklistTransaction>()
            coEvery {
                transaction.updateDetail(accountId = account.id, checklistId = checklistId, detail = detail.toLocal(), updatedAt = updatedAt)
            } returns 1
            val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = mockk(), accountChecklistTransaction = transaction)

            repository.updateDetail(account = account, checklistId = checklistId, detail = detail, updatedAt = updatedAt) shouldBe 1
        }

        listOf(true, false).forEach { isDeleted ->
            test("TC-CHECKLIST-HOME-DATA-004 삭제 여부를 $isDeleted 로 바꾸는 요청은 삭제 여부와 수정 시각만 현재 계정 기준으로 반영한다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val updatedAt = fixtureMonkey.giveMeOne<Instant>()
                val transaction = mockk<AccountChecklistTransaction>()
                coEvery {
                    transaction.updateDeleted(accountId = account.id, checklistId = checklistId, isDeleted = isDeleted, updatedAt = updatedAt)
                } returns 1
                val repository = AccountChecklistRepositoryImpl(accountChecklistLocalDataSource = mockk(), accountChecklistTransaction = transaction)

                repository.updateDeleted(account = account, checklistId = checklistId, isDeleted = isDeleted, updatedAt = updatedAt) shouldBe 1
            }
        }

        test("TC-CHECKLIST-DETAIL-DATA-002 체크 항목 변경의 기기 저장이 실패하면 실패를 그대로 전달한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountChecklistItemTransaction>()
            coEvery { transaction.updateAllChecked(accountId = any(), checklistId = any(), isChecked = any(), updatedAt = any()) } throws throwable
            val repository =
                AccountChecklistItemRepositoryImpl(accountChecklistItemLocalDataSource = mockk(), accountChecklistItemTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateAllChecked(
                    account = fixtureMonkey.giveMeOne<Account.User>(),
                    checklistId = fixtureMonkey.giveMeOne<Uuid>(),
                    isChecked = false,
                    updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                )
            } shouldBeSameInstanceAs throwable
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun pagingSource(localList: List<ChecklistSummaryLocalEntity>): PagingSource<Int, ChecklistSummaryLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = localList,
                        prevKey = null,
                        nextKey = null,
                    )
            }

        private suspend fun <T : Any> PagingData<T>.items(): List<T> =
            coroutineScope {
                val presenter =
                    object : PagingDataPresenter<T>(mainContext = coroutineContext) {
                        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) = Unit
                    }
                val collection = launch { presenter.collectFrom(this@items) }

                presenter.loadStateFlow
                    .filterNotNull()
                    .first { loadStates -> loadStates.refresh is LoadState.NotLoading }
                val result = presenter.snapshot().items

                collection.cancelAndJoin()
                result
            }
    }
}
