package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.authentication.appleAuthorization
import io.github.taetae98coding.diary.domain.account.repository.AccountRepository
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncTimeRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class DeleteAccountUseCaseTest :
    BehaviorSpec({
        Given("세션이 갱신된 사용자이고 서버 탈퇴가 성공하도록 준비되어 있다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val authorization = Arb.appleAuthorization().next()
            val fixture = Fixture(account = account)

            When("Apple 인가 코드와 함께 탈퇴한다") {
                val result = fixture.useCase(parameter = authorization)

                Then("TC-13a4dded-2539-40aa-8637-ea6f4de0517b 서버에 탈퇴를 요청한 뒤 로그아웃한다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fixture.accountRepository.delete(appleAuthorization = authorization) }
                    coVerify(exactly = 1) { fixture.signOutUseCase(parameter = Unit) }
                }

                Then("TC-e64cb655-d2fc-4b69-942a-20e3ad112f67 그 계정의 기기 데이터와 마지막 동기화 시각을 지운다") {
                    coVerify(exactly = 1) { fixture.accountSyncDataRepository.delete(account = account) }
                    coVerify(exactly = 1) { fixture.accountSyncTimeRepository.delete(account = account) }
                }
            }
        }

        listOf<Pair<String, () -> Account>>(
            "게스트" to { Account.Guest },
            "세션이 갱신되지 않은 사용자" to { Arb.accountUser().next().copy(isSessionValid = false) },
        ).forEach { (label, createAccount) ->
            Given("계정이 $label 다") {
                val fixture = Fixture(account = createAccount())

                When("탈퇴한다") {
                    val result = fixture.useCase(parameter = null)

                    Then("TC-4b46963d-1ea3-40d8-ae4b-66d54f4a4084 $label 서버에 요청하지 않고 실패한다") {
                        result.shouldBeFailure()
                        coVerify(exactly = 0) { fixture.accountRepository.delete(appleAuthorization = any()) }
                        coVerify(exactly = 0) { fixture.signOutUseCase(parameter = Unit) }
                    }
                }
            }
        }

        Given("서버 탈퇴가 실패하도록 준비되어 있고 오류 보고 기록 수단이 등록되어 있다") {
            val failure = IllegalStateException("delete failed")
            val fixture = Fixture(account = Arb.accountUser().next().copy(isSessionValid = true))
            coEvery { fixture.accountRepository.delete(appleAuthorization = any()) } throws failure
            val reportList = recordCrashlyticsLog()

            When("탈퇴한다") {
                val result = fixture.useCase(parameter = null)

                Then("TC-ed9f47a1-176a-4d60-a181-06e62faa6583 기기 데이터를 지우지 않고 로그아웃하지 않으며 실패한다") {
                    result.shouldBeFailure()
                    coVerify(exactly = 0) { fixture.accountSyncDataRepository.delete(account = any()) }
                    coVerify(exactly = 0) { fixture.signOutUseCase(parameter = Unit) }
                }

                Then("TC-364abafa-b866-4902-b128-c8a3799b563f 서버 처리 실패는 오류 보고를 남기지 않는다") {
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("서버 탈퇴는 성공하고 기기 데이터 삭제가 실패하도록 준비되어 있고 오류 보고 기록 수단이 등록되어 있다") {
            val failure = IllegalStateException("local delete failed")
            val fixture = Fixture(account = Arb.accountUser().next().copy(isSessionValid = true))
            coEvery { fixture.accountSyncDataRepository.delete(account = any()) } throws failure
            val reportList = recordCrashlyticsLog()

            When("탈퇴한다") {
                val result = fixture.useCase(parameter = null)

                Then("TC-892a1660-772e-408f-ad0e-c99ca15aefba 로그아웃하고 성공으로 끝나며 오류 보고는 남기지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fixture.signOutUseCase(parameter = Unit) }
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("서버 탈퇴 요청이 끝나지 않은 채 진행 중이다") {
            val fixture = Fixture(account = Arb.accountUser().next().copy(isSessionValid = true))
            val started = CompletableDeferred<Unit>()
            val response = CompletableDeferred<Unit>()
            coEvery { fixture.accountRepository.delete(appleAuthorization = any()) } coAnswers {
                started.complete(Unit)
                response.await()
            }

            When("요청한 쪽이 취소된 뒤 서버 탈퇴가 성공한다") {
                coroutineScope {
                    val job = launch { fixture.useCase(parameter = null) }
                    started.await()
                    job.cancelAndJoin()
                    response.complete(Unit)
                }

                Then("TC-ac89843d-73fb-4ece-9f0b-6f8c58a5228c 기기 데이터를 지우지 않고 로그아웃하지 않는다") {
                    coVerify(exactly = 0) { fixture.accountSyncDataRepository.delete(account = any()) }
                    coVerify(exactly = 0) { fixture.signOutUseCase(parameter = Unit) }
                }
            }
        }

        Given("서버 탈퇴는 성공하고 기기 항목 데이터 삭제만 실패하도록 준비되어 있다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val fixture = Fixture(account = account)
            coEvery { fixture.accountSyncDataRepository.delete(account = any()) } throws IllegalStateException("local delete failed")

            When("탈퇴한다") {
                fixture.useCase(parameter = null)

                Then("TC-e64cb655-d2fc-4b69-942a-20e3ad112f67 마지막 동기화 시각 기록은 지운다") {
                    coVerify(exactly = 1) { fixture.accountSyncTimeRepository.delete(account = account) }
                }
            }
        }
    })

private class Fixture(
    account: Account,
) {
    val accountRepository: AccountRepository =
        mockk {
            coEvery { delete(appleAuthorization = any()) } just Runs
        }
    val accountSyncDataRepository: AccountSyncDataRepository =
        mockk {
            coEvery { delete(account = any()) } just Runs
        }
    val accountSyncTimeRepository: AccountSyncTimeRepository =
        mockk {
            coEvery { delete(account = any()) } just Runs
        }
    val signOutUseCase: SignOutUseCase = mockk()
    private val getAccountUseCase: GetAccountUseCase = mockk()

    init {
        coEvery { signOutUseCase(parameter = Unit) } returns Result.success(Unit)
        every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
    }

    val useCase: DeleteAccountUseCase =
        DeleteAccountUseCase(
            getAccountUseCase = getAccountUseCase,
            accountRepository = accountRepository,
            accountSyncDataRepository = accountSyncDataRepository,
            accountSyncTimeRepository = accountSyncTimeRepository,
            signOutUseCase = signOutUseCase,
        )
}
