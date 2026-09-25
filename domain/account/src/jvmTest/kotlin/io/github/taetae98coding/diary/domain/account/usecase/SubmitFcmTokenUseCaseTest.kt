package io.github.taetae98coding.diary.domain.account.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.repository.FcmTokenRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class SubmitFcmTokenUseCaseTest :
    BehaviorSpec({
        Given("현재 계정이 게스트다") {
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.delete() } returns Unit
            val useCase = useCase(account = Account.Guest, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-018 해제를 한 번 요청하고 등록은 요청하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.delete() }
                    coVerify(exactly = 0) { repository.upsert() }
                }
            }
        }

        Given("현재 계정이 사용자이지만 세션이 갱신되지 않았다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false)
            val repository = mockk<FcmTokenRepository>()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-003 등록도 해제도 요청하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }

        Given("현재 계정이 세션이 갱신된 사용자다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } returns Unit
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-005 등록을 한 번 요청하고 해제는 요청하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 사용자의 등록이 특정 원인으로 실패한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-015 TC-USECASE-FAILURE-LOGGING-DOMAIN-009 실패를 전달하고 그 원인을 담은 오류 보고가 한 번 남는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure

                    val report = reportList.single()

                    report.throwable shouldBeSameInstanceAs failure
                    report.message shouldContain "SubmitFcmTokenUseCase"
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 게스트의 해제가 특정 원인으로 실패한다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.delete() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = Account.Guest, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-015 TC-USECASE-FAILURE-LOGGING-DOMAIN-009 실패를 전달하고 그 원인을 담은 오류 보고가 한 번 남는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure

                    val report = reportList.single()

                    report.throwable shouldBeSameInstanceAs failure
                    report.message shouldContain "SubmitFcmTokenUseCase"
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 제출이 진행 중에 중단된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws CancellationException("cancelled")
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                shouldThrowExactly<CancellationException> { useCase(parameter = Unit) }

                Then("TC-FCM-TOKEN-DOMAIN-017 오류 보고가 남지 않는다") {
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("저장된 사용자 정보는 있지만 세션이 인증되지 않았다가 인증된 상태로 바뀐다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } returns Unit
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returnsMany
                listOf(
                    flowOf(Result.success(account)),
                    flowOf(Result.success(account.copy(isSessionValid = true))),
                )
            val useCase = SubmitFcmTokenUseCase(getAccountUseCase = getAccountUseCase, fcmTokenRepository = repository)

            When("세션이 인증되기 전과 뒤의 계기에 토큰을 제출한다") {
                useCase(parameter = Unit).shouldBeSuccess()
                coVerify(exactly = 0) { repository.upsert() }
                coVerify(exactly = 0) { repository.delete() }

                useCase(parameter = Unit).shouldBeSuccess()

                Then("TC-FCM-TOKEN-DOMAIN-003 인증된 뒤에만 등록을 한 번 요청한다") {
                    coVerify(exactly = 1) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }

        Given("로그아웃되어 현재 계정이 게스트로 바뀌었고 서버에 도달할 수 없다") {
            val failure = IOException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.delete() } throws failure
            val useCase = useCase(account = Account.Guest, repository = repository)

            When("게스트로 바뀐 계기에 토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-020 해제를 한 번 요청하고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    coVerify(exactly = 1) { repository.delete() }
                    coVerify(exactly = 0) { repository.upsert() }
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 사용자의 등록이 통신 오류로 실패한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val failure = IOException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-016 통신 오류를 원인으로 담은 오류 보고가 한 번 남는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.single().throwable shouldBeSameInstanceAs failure
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 기기에 토큰이 없어 등록이 요청 없이 끝난다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } returns Unit
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-022 성공으로 끝나고 오류 보고가 남지 않는다") {
                    result.shouldBeSuccess()
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 토큰을 받아 오지 못해 등록이 실패한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-FCM-TOKEN-DOMAIN-024 토큰을 받아 오지 못한 오류를 원인으로 담은 오류 보고가 한 번 남는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.single().throwable shouldBeSameInstanceAs failure
                }
            }
        }

        Given("계정 확인이 실패한다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<FcmTokenRepository>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase = SubmitFcmTokenUseCase(getAccountUseCase = getAccountUseCase, fcmTokenRepository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("제출하지 않고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    coVerify(exactly = 0) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }
    })

private fun useCase(
    account: Account,
    repository: FcmTokenRepository,
): SubmitFcmTokenUseCase {
    val getAccountUseCase = mockk<GetAccountUseCase>()
    every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

    return SubmitFcmTokenUseCase(getAccountUseCase = getAccountUseCase, fcmTokenRepository = repository)
}
