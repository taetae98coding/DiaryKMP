package io.github.taetae98coding.diary.domain.checklist.usecase

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistItemContentBlankException
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AddChecklistItemUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 연결된 체크리스트가 있고 현재 시각이 고정되어 있다") {
            val context = AddItemContext(account = fixtureMonkey.giveMeOne<Account.User>())
            context.stubUpdatedCount(updatedCount = 1)

            When("내용 ` 우산 `으로 항목을 넣는다") {
                context.useCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = context.checklistId, content = " 우산 ")).shouldBeSuccess(1)

                Then("TC-93f77e96-7b62-406b-8564-b3ddc37fbbfb TC-a0aea019-81b3-42f1-a8fc-133ba9b26542 대상 체크리스트에 속한 체크되지 않은 새 항목을 넣은 시각으로 현재 계정과 함께 저장한다") {
                    val item = context.savedItemList.single()
                    item shouldBe
                        ChecklistItem(
                            id = item.id,
                            checklistId = context.checklistId,
                            content = " 우산 ",
                            isChecked = false,
                            isDeleted = false,
                            updatedAt = context.now,
                            createdAt = context.now,
                        )
                    coVerify(exactly = 1) { context.repository.upsert(account = checkNotNull(context.account), item = any()) }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367 기기에 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("대상 체크리스트에 내용 `우산`인 항목을 이미 넣었다") {
            val context = AddItemContext(account = fixtureMonkey.giveMeOne<Account.User>())
            context.stubUpdatedCount(updatedCount = 1)
            val content = itemContent()
            val parameter = AddChecklistItemUseCase.Parameter(checklistId = context.checklistId, content = content)
            context.useCase(parameter = parameter).shouldBeSuccess(1)

            When("내용 `우산`으로 항목을 다시 넣는다") {
                context.useCase(parameter = parameter).shouldBeSuccess(1)

                Then("TC-3d921233-a531-41b1-b83d-2c31a1a8bc30 기존 항목과 다른 식별 정보의 새 항목으로 저장한다") {
                    context.savedItemList.map { item -> item.content } shouldBe listOf(content, content)
                    context.savedItemList[1].id shouldNotBe context.savedItemList[0].id
                }
            }
        }

        listOf("", "   ").forEach { content ->
            Given("항목 입력이 '$content' 이다") {
                val context = AddItemContext(account = fixtureMonkey.giveMeOne<Account.User>())
                context.stubUpdatedCount(updatedCount = 1)

                When("항목을 넣는다") {
                    val result = context.useCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = context.checklistId, content = content))

                    Then("항목을 저장하지 않고 내용 미입력으로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<ChecklistItemContentBlankException>()
                        coVerify(exactly = 0) { context.repository.upsert(account = any(), item = any()) }
                        coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = AddItemContext(account = fixtureMonkey.giveMeOne<Account.Guest>())
            context.stubUpdatedCount(updatedCount = 1)

            When("항목을 넣는다") {
                context.useCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = context.checklistId, content = itemContent())).shouldBeSuccess(1)

                Then("TC-20be2c49-4b48-41e2-861c-50dbec844576 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = checkNotNull(context.account), item = any()) }
                }
            }
        }

        Given("현재 계정과 연결된 체크리스트가 기기에 없다") {
            val context = AddItemContext(account = fixtureMonkey.giveMeOne<Account.User>())
            context.stubUpdatedCount(updatedCount = 0)

            When("항목을 넣는다") {
                val result = context.useCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = context.checklistId, content = itemContent()))

                Then("TC-fd1241c1-0667-4741-a1de-10602a660b42 넣은 항목이 없음을 전달한다") {
                    result.shouldBeSuccess(0)
                }
            }
        }

        Given("기기 저장이 실패하도록 설정되어 있다") {
            val context = AddItemContext(account = fixtureMonkey.giveMeOne<Account.User>())
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.repository.upsert(account = any(), item = any()) } throws throwable

            When("항목을 넣는다") {
                val result = context.useCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = context.checklistId, content = itemContent()))

                Then("TC-8e505659-9424-4d8b-abe1-6e8bc8d72794 TC-a0aea019-81b3-42f1-a8fc-133ba9b26542 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }
    })

private class AddItemContext(
    val account: Account?,
) {
    val checklistId: Uuid = fixtureMonkey.giveMeOne()
    val now: Instant = fixtureMonkey.giveMeOne()
    val requestSyncUseCase: RequestSyncUseCase = requestSyncUseCase()
    val repository: AccountChecklistItemRepository = mockk()
    val savedItemList = mutableListOf<ChecklistItem>()
    val useCase: AddChecklistItemUseCase =
        AddChecklistItemUseCase(
            getAccountUseCase(account = checkNotNull(account)),
            requestSyncUseCase,
            repository,
            clock(now = now),
        )

    fun stubUpdatedCount(updatedCount: Int) {
        coEvery { repository.upsert(account = any(), item = capture(savedItemList)) } returns updatedCount
    }
}
