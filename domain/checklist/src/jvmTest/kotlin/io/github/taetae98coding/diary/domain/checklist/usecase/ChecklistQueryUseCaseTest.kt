package io.github.taetae98coding.diary.domain.checklist.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

class ChecklistQueryUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 체크리스트와 진행 정도가 기기에 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val summaryList =
                List(2) { ChecklistSummary(checklist = fixtureMonkey.checklist(isDeleted = false), checkedCount = 1, totalCount = 2) }
            val repository = mockk<AccountChecklistRepository>()
            every { repository.page(account = account) } returns flowOf(PagingData.from(summaryList))

            When("체크리스트 목록을 조회한다") {
                val pagingData = PageChecklistUseCase(getAccountUseCase(account), repository)(parameter = Unit).first().shouldBeSuccess()

                Then("TC-5474c60f-6cdd-47da-9556-061c02a51d7a TC-ffcb4f61-636d-4088-9c8d-3f376f926bbf 현재 계정의 체크리스트를 진행 정도와 함께 페이지로 전달한다") {
                    flowOf(pagingData).asSnapshot() shouldBe summaryList
                    verify(exactly = 1) { repository.page(account = account) }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val checklistRepository = mockk<AccountChecklistRepository>()
            val itemRepository = mockk<AccountChecklistItemRepository>()

            When("체크리스트 목록을 조회한다") {
                val result = PageChecklistUseCase(failingGetAccountUseCase(throwable), checklistRepository)(parameter = Unit).first()

                Then("TC-38cb2689-01a1-4743-a6a3-8ecc6efc2141 노출할 체크리스트를 전달하지 않고 계정 조회 실패를 그대로 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    verify(exactly = 0) { checklistRepository.page(account = any()) }
                }
            }

            When("체크리스트 하나를 조회한다") {
                val result = FindChecklistUseCase(failingGetAccountUseCase(throwable), checklistRepository)(parameter = fixtureMonkey.giveMeOne()).first()

                Then("TC-cd7f5929-faaf-4830-9230-c34621b1f7ab 계정 조회 실패를 그대로 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                }
            }

            When("체크 항목을 조회한다") {
                val result =
                    GetChecklistItemUseCase(failingGetAccountUseCase(throwable), itemRepository)(
                        parameter = GetChecklistItemUseCase.Parameter(checklistId = fixtureMonkey.giveMeOne(), sort = ChecklistItemSort.ADDED),
                    ).first()

                Then("계정 조회 실패를 그대로 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                }
            }
        }

        Given("현재 계정과 연결된 체크리스트가 삭제 상태로 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val checklist = fixtureMonkey.checklist(isDeleted = true)
            val repository = mockk<AccountChecklistRepository>()
            every { repository.find(account = account, checklistId = checklist.id) } returns flowOf(checklist)

            When("그 체크리스트를 상세 대상으로 조회한다") {
                val result = FindChecklistUseCase(getAccountUseCase(account), repository)(parameter = checklist.id).first()

                Then("TC-3b75e95e-f535-4154-bfeb-5e95f65cdea4 삭제 상태인 체크리스트를 그대로 전달한다") {
                    result.shouldBeSuccess(checklist)
                }
            }
        }

        listOf(ChecklistItemSort.ADDED, ChecklistItemSort.UNCHECKED_FIRST).forEach { sort ->
            Given("현재 계정과 연결된 체크리스트의 체크 항목이 저장되어 있다 ($sort)") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val checklistId = fixtureMonkey.giveMeOne<Uuid>()
                val itemList =
                    listOf(true, false).map { isChecked -> fixtureMonkey.checklistItem(isChecked = isChecked, isDeleted = false, checklistId = checklistId) }
                val repository = mockk<AccountChecklistItemRepository>()
                every { repository.get(account = account, checklistId = checklistId, sort = sort) } returns flowOf(itemList)

                When("고른 정렬로 체크 항목을 조회한다") {
                    val result =
                        GetChecklistItemUseCase(getAccountUseCase(account), repository)(
                            parameter = GetChecklistItemUseCase.Parameter(checklistId = checklistId, sort = sort),
                        ).first()

                    Then("TC-9596835f-24cf-4a58-86a3-0c84c04d05db TC-289d5d96-f27f-49bf-8b57-dbc8e44c77e6 현재 계정 기준으로 고른 정렬의 체크 항목을 체크 여부와 관계없이 전달한다") {
                        result.shouldBeSuccess(itemList)
                    }
                }
            }
        }
    })
