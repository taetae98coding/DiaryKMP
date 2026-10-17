package io.github.taetae98coding.diary.domain.qr.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

class FindQrUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 연결된 삭제 상태의 QR이 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val qr = fixtureMonkey.qr(isDeleted = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            every { accountQrRepository.find(account = account, qrId = qr.id) } returns flowOf(qr)
            val useCase = FindQrUseCase(getAccountUseCase = getAccountUseCase, accountQrRepository = accountQrRepository)

            When("그 QR을 찾는다") {
                Then("TC-QR-DETAIL-DOMAIN-001 삭제 상태여도 현재 계정 기준으로 찾은 QR을 전달한다") {
                    useCase(parameter = qr.id).test {
                        awaitItem().shouldBeSuccess(qr)
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정과 연결된 그 QR이 기기에 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            every { accountQrRepository.find(account = account, qrId = id) } returns flowOf(null)
            val useCase = FindQrUseCase(getAccountUseCase = getAccountUseCase, accountQrRepository = accountQrRepository)

            When("그 QR을 찾는다") {
                Then("찾은 QR이 없음을 전달한다") {
                    useCase(parameter = id).test {
                        awaitItem().shouldBeSuccess(null)
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase = FindQrUseCase(getAccountUseCase = getAccountUseCase, accountQrRepository = mockk())

            When("QR을 찾는다") {
                Then("조회 실패를 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>()).test {
                        awaitItem().shouldBeFailure().shouldBeSameInstanceAs(throwable)
                        awaitComplete()
                    }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
