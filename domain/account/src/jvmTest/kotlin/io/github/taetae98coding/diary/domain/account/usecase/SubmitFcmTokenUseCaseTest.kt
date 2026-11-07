package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.repository.FcmTokenRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

class SubmitFcmTokenUseCaseTest :
    BehaviorSpec({
        Given("현재 계정이 게스트다") {
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.delete() } returns Unit
            val useCase = useCase(account = Account.Guest, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-90250441-85e8-4644-a4fd-8e1675552f74 해제를 한 번 요청하고 등록은 요청하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.delete() }
                    coVerify(exactly = 0) { repository.upsert() }
                }
            }
        }

        Given("현재 계정이 사용자이지만 세션이 갱신되지 않았다") {
            val account = Arb.accountUser().next().copy(isSessionValid = false)
            val repository = mockk<FcmTokenRepository>()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-fe8a1e97-131a-454b-84fe-3ab5598f19f5 등록도 해제도 요청하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }

        Given("현재 계정이 세션이 갱신된 사용자다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } returns Unit
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-df26f005-cca3-43b9-9b5a-ce35848bf660 등록을 한 번 요청하고 해제는 요청하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 사용자의 등록이 특정 원인으로 실패한다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val failure = IllegalStateException(Arb.string().next())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-4d24fc43-3f72-4b39-9d43-ea654eb37958 실패를 전달하고 오류 보고는 남기지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 게스트의 해제가 특정 원인으로 실패한다") {
            val failure = IllegalStateException(Arb.string().next())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.delete() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = Account.Guest, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-4d24fc43-3f72-4b39-9d43-ea654eb37958 실패를 전달하고 오류 보고는 남기지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 제출이 진행 중에 중단된다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws CancellationException("cancelled")
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                shouldThrowExactly<CancellationException> { useCase(parameter = Unit) }

                Then("TC-969f648b-64d1-4167-b5a1-e95732282aa5 오류 보고가 남지 않는다") {
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("저장된 사용자 정보는 있지만 세션이 인증되지 않았다가 인증된 상태로 바뀐다") {
            val account = Arb.accountUser().next().copy(isSessionValid = false)
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

                Then("TC-fe8a1e97-131a-454b-84fe-3ab5598f19f5 인증된 뒤에만 등록을 한 번 요청한다") {
                    coVerify(exactly = 1) { repository.upsert() }
                    coVerify(exactly = 0) { repository.delete() }
                }
            }
        }

        Given("로그아웃되어 현재 계정이 게스트로 바뀌었고 서버에 도달할 수 없다") {
            val failure = IOException(Arb.string().next())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.delete() } throws failure
            val useCase = useCase(account = Account.Guest, repository = repository)

            When("게스트로 바뀐 계기에 토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-68ae3e2d-db68-4521-841f-5fa83acf7e6b 해제를 한 번 요청하고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    coVerify(exactly = 1) { repository.delete() }
                    coVerify(exactly = 0) { repository.upsert() }
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 사용자의 등록이 통신 오류로 실패한다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val failure = IOException(Arb.string().next())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-4d24fc43-3f72-4b39-9d43-ea654eb37958 통신 오류로 실패해도 오류 보고는 남기지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 기기에 토큰이 없어 등록이 요청 없이 끝난다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } returns Unit
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-ed327973-684c-4131-af2e-3c9a8d007034 성공으로 끝나고 오류 보고가 남지 않는다") {
                    result.shouldBeSuccess()
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 토큰을 받아 오지 못해 등록이 실패한다") {
            val account = Arb.accountUser().next().copy(isSessionValid = true)
            val failure = IllegalStateException(Arb.string().next())
            val repository = mockk<FcmTokenRepository>()
            coEvery { repository.upsert() } throws failure
            val reportList = recordCrashlyticsLog()
            val useCase = useCase(account = account, repository = repository)

            When("토큰을 제출한다") {
                val result = useCase(parameter = Unit)

                Then("TC-a5d2b40e-e204-4dc5-aa2f-9cb56a893b1a 실패를 전달하고 오류 보고는 남기지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("계정 확인이 실패한다") {
            val failure = IllegalStateException(Arb.string().next())
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
