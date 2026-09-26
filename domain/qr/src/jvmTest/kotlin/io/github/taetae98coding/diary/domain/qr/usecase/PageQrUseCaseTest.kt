package io.github.taetae98coding.diary.domain.qr.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class PageQrUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 계정의 QR이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val qrList = List(2) { fixtureMonkey.qr(isDeleted = false) }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountQrRepository = mockk<AccountQrRepository>()
            every { accountQrRepository.page(account = account) } returns flowOf(PagingData.from(qrList))
            val useCase =
                PageQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountQrRepository = accountQrRepository,
                )

            When("QR 목록을 페이지로 조회한다") {
                Then("TC-QR-HOME-DOMAIN-005 TC-QR-HOME-DATA-001 현재 계정의 QR을 페이지로 전달한다") {
                    val pagingData = useCase(parameter = Unit).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe qrList
                }
            }
        }

        Given("현재 계정이 바뀌도록 준비되어 있다") {
            val firstAccount = fixtureMonkey.giveMeOne<Account.Guest>()
            val secondAccount = fixtureMonkey.giveMeOne<Account.User>()
            val firstQrList = List(2) { fixtureMonkey.qr(isDeleted = false) }
            val secondQrList = List(2) { fixtureMonkey.qr(isDeleted = false) }
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(firstAccount))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val accountQrRepository = mockk<AccountQrRepository>()
            every { accountQrRepository.page(account = firstAccount) } returns flowOf(PagingData.from(firstQrList))
            every { accountQrRepository.page(account = secondAccount) } returns flowOf(PagingData.from(secondQrList))
            val useCase =
                PageQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountQrRepository = accountQrRepository,
                )

            When("QR 목록을 조회하는 동안 현재 계정이 바뀐다") {
                Then("바뀐 계정의 QR을 이어서 전달한다") {
                    useCase(parameter = Unit).test {
                        flowOf(awaitItem().shouldBeSuccess()).asSnapshot() shouldBe firstQrList

                        accountFlow.value = Result.success(secondAccount)

                        flowOf(awaitItem().shouldBeSuccess()).asSnapshot() shouldBe secondQrList
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountQrRepository = mockk<AccountQrRepository>(relaxed = true)
            val useCase =
                PageQrUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountQrRepository = accountQrRepository,
                )

            When("QR 목록을 페이지로 조회한다") {
                Then("TC-QR-HOME-DOMAIN-006 노출할 QR을 전달하지 않고 계정 조회 실패를 그대로 전달한다") {
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountQrRepository.page(account = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
