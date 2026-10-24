package io.github.taetae98coding.diary.domain.checklist.usecase

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class UpdateChecklistUseCaseTest :
    BehaviorSpec({
        listOf(
            UpdateInput(title = " 제주 여행 ", description = "여름\n휴가", isStoredTitleUsed = false),
            UpdateInput(title = "제주 여행", description = "", isStoredTitleUsed = false),
            UpdateInput(title = "", description = "여름", isStoredTitleUsed = true),
            UpdateInput(title = "   ", description = "여름", isStoredTitleUsed = true),
        ).forEach { (title, description, isStoredTitleUsed) ->
            Given("현재 계정과 연결된 체크리스트가 있고 제목 '$title', 설명 '$description' 이 입력되어 있다") {
                val context = UpdateContext(account = fixtureMonkey.giveMeOne<Account.User>())
                context.stubUpdatedCount(updatedCount = 1)

                When("수정을 반영한다") {
                    context
                        .useCase(parameter = UpdateChecklistUseCase.Parameter(id = context.checklist.id, detail = ChecklistDetail(title, description)))
                        .shouldBeSuccess(1)

                    Then("TC-CHECKLIST-DETAIL-DOMAIN-006 제목과 설명을 기대한 값으로 수정 시각과 함께 저장한다") {
                        val expectedTitle = if (isStoredTitleUsed) context.checklist.detail.title else title
                        coVerify(exactly = 1) {
                            context.repository.updateDetail(
                                account = checkNotNull(context.account),
                                checklistId = context.checklist.id,
                                detail = ChecklistDetail(title = expectedTitle, description = description),
                                updatedAt = context.now,
                            )
                        }
                    }

                    Then("TC-SYNC-REFRESH-FEATURE-004 TC-CHECKLIST-DETAIL-DATA-005 기기에 저장한 뒤 동기화를 한 번 요청한다") {
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                    }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = UpdateContext(account = fixtureMonkey.giveMeOne<Account.Guest>())
            context.stubUpdatedCount(updatedCount = 1)
            val detail = fixtureMonkey.checklistDetail()

            When("수정을 반영한다") {
                context.useCase(parameter = UpdateChecklistUseCase.Parameter(id = context.checklist.id, detail = detail)).shouldBeSuccess(1)

                Then("TC-CHECKLIST-DETAIL-DATA-006 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) {
                        context.repository.updateDetail(
                            account = checkNotNull(context.account),
                            checklistId = context.checklist.id,
                            detail = detail,
                            updatedAt = context.now,
                        )
                    }
                }
            }
        }

        Given("현재 계정과 연결된 체크리스트가 기기에 없다") {
            val context = UpdateContext(account = fixtureMonkey.giveMeOne<Account.User>())
            context.stubUpdatedCount(updatedCount = 0)

            When("수정을 반영한다") {
                val result = context.useCase(parameter = UpdateChecklistUseCase.Parameter(id = context.checklist.id, detail = fixtureMonkey.checklistDetail()))

                Then("TC-CHECKLIST-DETAIL-DATA-001 바뀐 체크리스트가 없음을 전달한다") {
                    result.shouldBeSuccess(0)
                }
            }
        }

        Given("기기 저장이 실패하도록 설정되어 있다") {
            val context = UpdateContext(account = fixtureMonkey.giveMeOne<Account.User>())
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.repository.updateDetail(account = any(), checklistId = any(), detail = any(), updatedAt = any()) } throws throwable

            When("수정을 반영한다") {
                val result = context.useCase(parameter = UpdateChecklistUseCase.Parameter(id = context.checklist.id, detail = fixtureMonkey.checklistDetail()))

                Then("TC-CHECKLIST-DETAIL-DATA-002 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val context = UpdateContext(account = null, accountFailure = throwable)
            context.stubUpdatedCount(updatedCount = 1)

            When("수정을 반영한다") {
                val result = context.useCase(parameter = UpdateChecklistUseCase.Parameter(id = context.checklist.id, detail = fixtureMonkey.checklistDetail()))

                Then("아무것도 저장하지 않고 계정 조회 실패를 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { context.repository.updateDetail(account = any(), checklistId = any(), detail = any(), updatedAt = any()) }
                }
            }
        }
    })

private data class UpdateInput(
    val title: String,
    val description: String,
    val isStoredTitleUsed: Boolean,
)

private class UpdateContext(
    val account: Account?,
    accountFailure: Throwable? = null,
) {
    val checklist: Checklist = fixtureMonkey.checklist(isDeleted = false)
    val now: Instant = fixtureMonkey.giveMeOne()
    val requestSyncUseCase: RequestSyncUseCase = requestSyncUseCase()
    val repository: AccountChecklistRepository = mockk()
    private val getAccountUseCase: GetAccountUseCase =
        if (account != null) getAccountUseCase(account = account) else failingGetAccountUseCase(throwable = checkNotNull(accountFailure))
    val useCase: UpdateChecklistUseCase =
        UpdateChecklistUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            findChecklistUseCase = FindChecklistUseCase(getAccountUseCase = getAccountUseCase, accountChecklistRepository = repository),
            accountChecklistRepository = repository,
            clock = clock(now = now),
        )

    init {
        every { repository.find(account = any(), checklistId = checklist.id) } returns flowOf(checklist)
    }

    fun stubUpdatedCount(updatedCount: Int) {
        coEvery { repository.updateDetail(account = any(), checklistId = any(), detail = any(), updatedAt = any()) } returns updatedCount
    }
}
