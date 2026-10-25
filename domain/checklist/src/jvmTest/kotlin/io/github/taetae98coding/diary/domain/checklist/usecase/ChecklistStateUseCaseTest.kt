package io.github.taetae98coding.diary.domain.checklist.usecase

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ChecklistStateUseCaseTest :
    BehaviorSpec({
        stateCaseList.forEach { stateCase ->
            Given("${stateCase.label}: 현재 계정과 연결된 대상이 있고 현재 시각이 고정되어 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val context = StateContext(account = account)
                context.stubResult(updatedCount = 1)

                When("${stateCase.label}을 실행한다") {
                    val result = stateCase.invoke(context)

                    Then(testName(tcIdList = stateCase.savedTcIdList, description = "대상을 동작 시점과 함께 저장한다")) {
                        result.shouldBeSuccess(1)
                        stateCase.verifySaved(context)
                    }

                    Then(testName(tcIdList = listOf("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c") + stateCase.syncTcIdList, description = "기기에 저장한 뒤 동기화를 한 번 요청한다")) {
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                    }
                }
            }

            Given("${stateCase.label}: 로그인하지 않아 게스트 계정이 확인된다") {
                val account = fixtureMonkey.giveMeOne<Account.Guest>()
                val context = StateContext(account = account)
                context.stubResult(updatedCount = 1)

                When("${stateCase.label}을 실행한다") {
                    val result = stateCase.invoke(context)

                    Then(testName(tcIdList = stateCase.guestTcIdList, description = "게스트 계정 기준으로 기기에 저장한다")) {
                        result.shouldBeSuccess(1)
                        stateCase.verifySaved(context)
                    }
                }
            }

            Given("${stateCase.label}의 기기 저장이 실패하도록 준비되어 있다") {
                val context = StateContext(account = fixtureMonkey.giveMeOne<Account.User>())
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                context.stubFailure(throwable = throwable)

                When("${stateCase.label}을 실행한다") {
                    val result = stateCase.invoke(context)

                    Then(testName(tcIdList = stateCase.failureTcIdList, description = "실패를 그대로 전달하고 동기화를 요청하지 않는다")) {
                        result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                        coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }

            Given("${stateCase.label}: 현재 계정 조회가 실패하도록 준비되어 있다") {
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                val context = StateContext(account = null, accountFailure = throwable)
                context.stubResult(updatedCount = 1)

                When("${stateCase.label}을 실행한다") {
                    val result = stateCase.invoke(context)

                    Then(testName(tcIdList = stateCase.accountFailureTcIdList, description = "아무것도 저장하지 않고 계정 조회 실패를 전달한다")) {
                        result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                        context.verifyNothingSaved()
                    }
                }
            }
        }

        Given("현재 계정과 연결된 대상이 기기에 없다") {
            val context = StateContext(account = fixtureMonkey.giveMeOne<Account.User>())
            context.stubResult(updatedCount = 0)

            stateCaseList.forEach { stateCase ->
                When("${stateCase.label}을 실행한다") {
                    Then("TC-fd1241c1-0667-4741-a1de-10602a660b42 바뀐 대상이 없음을 전달한다") {
                        stateCase.invoke(context).shouldBeSuccess(0)
                    }
                }
            }
        }
    })

private fun testName(
    tcIdList: List<String>,
    description: String,
): String = (tcIdList + description).joinToString(separator = " ")

private class StateContext(
    val account: Account?,
    accountFailure: Throwable? = null,
) {
    val id: Uuid = fixtureMonkey.giveMeOne()
    val now: Instant = fixtureMonkey.giveMeOne()
    val clock: Clock = clock(now = now)
    val requestSyncUseCase: RequestSyncUseCase = requestSyncUseCase()
    val getAccountUseCase: GetAccountUseCase =
        if (account != null) getAccountUseCase(account = account) else failingGetAccountUseCase(throwable = checkNotNull(accountFailure))
    val checklistRepository: AccountChecklistRepository = mockk()
    val itemRepository: AccountChecklistItemRepository = mockk()

    fun stubResult(updatedCount: Int) {
        coEvery { checklistRepository.updateDeleted(account = any(), checklistId = any(), isDeleted = any(), updatedAt = any()) } returns updatedCount
        coEvery { itemRepository.updateChecked(account = any(), itemId = any(), isChecked = any(), updatedAt = any()) } returns updatedCount
        coEvery { itemRepository.updateDeleted(account = any(), itemId = any(), isDeleted = any(), updatedAt = any()) } returns updatedCount
        coEvery { itemRepository.updateAllChecked(account = any(), checklistId = any(), isChecked = any(), updatedAt = any()) } returns updatedCount
    }

    fun stubFailure(throwable: Throwable) {
        coEvery { checklistRepository.updateDeleted(account = any(), checklistId = any(), isDeleted = any(), updatedAt = any()) } throws throwable
        coEvery { itemRepository.updateChecked(account = any(), itemId = any(), isChecked = any(), updatedAt = any()) } throws throwable
        coEvery { itemRepository.updateDeleted(account = any(), itemId = any(), isDeleted = any(), updatedAt = any()) } throws throwable
        coEvery { itemRepository.updateAllChecked(account = any(), checklistId = any(), isChecked = any(), updatedAt = any()) } throws throwable
    }

    fun verifyNothingSaved() {
        coVerify(exactly = 0) { checklistRepository.updateDeleted(account = any(), checklistId = any(), isDeleted = any(), updatedAt = any()) }
        coVerify(exactly = 0) { itemRepository.updateChecked(account = any(), itemId = any(), isChecked = any(), updatedAt = any()) }
        coVerify(exactly = 0) { itemRepository.updateDeleted(account = any(), itemId = any(), isDeleted = any(), updatedAt = any()) }
        coVerify(exactly = 0) { itemRepository.updateAllChecked(account = any(), checklistId = any(), isChecked = any(), updatedAt = any()) }
    }
}

private class StateCase(
    val label: String,
    val savedTcIdList: List<String>,
    val syncTcIdList: List<String>,
    val guestTcIdList: List<String>,
    val failureTcIdList: List<String>,
    val accountFailureTcIdList: List<String>,
    val invoke: suspend (StateContext) -> Result<Int>,
    val verifySaved: (StateContext) -> Unit,
)

private val stateCaseList: List<StateCase> =
    listOf(
        StateCase(
            label = "체크리스트 삭제",
            savedTcIdList = listOf("TC-d9950fa8-8de2-4ed5-9eb9-93883cd6e478", "TC-63ed56a5-0718-4c57-a018-36a1952fca0e"),
            syncTcIdList = listOf("TC-bc9ac9d1-bc92-4acc-a8ef-eee1dbedb8cc", "TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367"),
            guestTcIdList = listOf("TC-5766f1b0-3084-42a3-a460-5a387498f7d8", "TC-20be2c49-4b48-41e2-861c-50dbec844576"),
            failureTcIdList = listOf("TC-8e505659-9424-4d8b-abe1-6e8bc8d72794"),
            accountFailureTcIdList = emptyList(),
            invoke = { context ->
                DeleteChecklistUseCase(context.getAccountUseCase, context.requestSyncUseCase, context.checklistRepository, context.clock)(
                    parameter = context.id,
                )
            },
            verifySaved = { context ->
                coVerify(exactly = 1) {
                    context.checklistRepository.updateDeleted(
                        account = checkNotNull(context.account),
                        checklistId = context.id,
                        isDeleted = true,
                        updatedAt = context.now,
                    )
                }
            },
        ),
        StateCase(
            label = "체크리스트 삭제의 실행 취소",
            savedTcIdList = listOf("TC-96e49a34-c00b-4352-ab42-a8ec06bffe03"),
            syncTcIdList = listOf("TC-bc9ac9d1-bc92-4acc-a8ef-eee1dbedb8cc"),
            guestTcIdList = listOf("TC-5766f1b0-3084-42a3-a460-5a387498f7d8"),
            failureTcIdList = emptyList(),
            accountFailureTcIdList = listOf("TC-17730eb2-1d22-4810-99d2-aca75dfe6c84"),
            invoke = { context ->
                RestoreChecklistUseCase(context.getAccountUseCase, context.requestSyncUseCase, context.checklistRepository, context.clock)(
                    parameter = context.id,
                )
            },
            verifySaved = { context ->
                coVerify(exactly = 1) {
                    context.checklistRepository.updateDeleted(
                        account = checkNotNull(context.account),
                        checklistId = context.id,
                        isDeleted = false,
                        updatedAt = context.now,
                    )
                }
            },
        ),
        StateCase(
            label = "체크",
            savedTcIdList = listOf("TC-7becaae5-50d1-46ca-874a-97280e482e04"),
            syncTcIdList = listOf("TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367"),
            guestTcIdList = listOf("TC-20be2c49-4b48-41e2-861c-50dbec844576"),
            failureTcIdList = listOf("TC-8e505659-9424-4d8b-abe1-6e8bc8d72794"),
            accountFailureTcIdList = emptyList(),
            invoke = { context ->
                CheckChecklistItemUseCase(context.getAccountUseCase, context.requestSyncUseCase, context.itemRepository, context.clock)(
                    parameter = context.id,
                )
            },
            verifySaved = { context ->
                coVerify(exactly = 1) {
                    context.itemRepository.updateChecked(
                        account = checkNotNull(context.account),
                        itemId = context.id,
                        isChecked = true,
                        updatedAt = context.now,
                    )
                }
            },
        ),
        StateCase(
            label = "체크 해제",
            savedTcIdList = listOf("TC-7becaae5-50d1-46ca-874a-97280e482e04"),
            syncTcIdList = listOf("TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367"),
            guestTcIdList = listOf("TC-20be2c49-4b48-41e2-861c-50dbec844576"),
            failureTcIdList = listOf("TC-8e505659-9424-4d8b-abe1-6e8bc8d72794"),
            accountFailureTcIdList = emptyList(),
            invoke = { context ->
                UncheckChecklistItemUseCase(context.getAccountUseCase, context.requestSyncUseCase, context.itemRepository, context.clock)(
                    parameter = context.id,
                )
            },
            verifySaved = { context ->
                coVerify(exactly = 1) {
                    context.itemRepository.updateChecked(
                        account = checkNotNull(context.account),
                        itemId = context.id,
                        isChecked = false,
                        updatedAt = context.now,
                    )
                }
            },
        ),
        StateCase(
            label = "항목 빼기",
            savedTcIdList = listOf("TC-d7bffc1e-30a6-4f1a-8dae-588da8f7d911"),
            syncTcIdList = listOf("TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367"),
            guestTcIdList = listOf("TC-20be2c49-4b48-41e2-861c-50dbec844576"),
            failureTcIdList = listOf("TC-8e505659-9424-4d8b-abe1-6e8bc8d72794"),
            accountFailureTcIdList = emptyList(),
            invoke = { context ->
                DeleteChecklistItemUseCase(context.getAccountUseCase, context.requestSyncUseCase, context.itemRepository, context.clock)(
                    parameter = context.id,
                )
            },
            verifySaved = { context ->
                coVerify(exactly = 1) {
                    context.itemRepository.updateDeleted(
                        account = checkNotNull(context.account),
                        itemId = context.id,
                        isDeleted = true,
                        updatedAt = context.now,
                    )
                }
            },
        ),
        StateCase(
            label = "리셋",
            savedTcIdList = listOf("TC-c81f2cd3-659c-4d52-b6b4-571337e12f0b"),
            syncTcIdList = listOf("TC-e066d56e-5d9e-4299-a49b-e4b1cfb94367"),
            guestTcIdList = listOf("TC-20be2c49-4b48-41e2-861c-50dbec844576"),
            failureTcIdList = listOf("TC-8e505659-9424-4d8b-abe1-6e8bc8d72794"),
            accountFailureTcIdList = emptyList(),
            invoke = { context ->
                ResetChecklistUseCase(context.getAccountUseCase, context.requestSyncUseCase, context.itemRepository, context.clock)(
                    parameter = context.id,
                )
            },
            verifySaved = { context ->
                coVerify(exactly = 1) {
                    context.itemRepository.updateAllChecked(
                        account = checkNotNull(context.account),
                        checklistId = context.id,
                        isChecked = false,
                        updatedAt = context.now,
                    )
                }
            },
        ),
    )
