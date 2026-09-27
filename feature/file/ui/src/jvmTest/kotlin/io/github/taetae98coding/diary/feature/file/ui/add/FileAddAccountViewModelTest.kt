@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileAddAccountViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-FILE-ADD-FEATURE-015 게스트가 되면 화면을 닫도록 알리고 다른 사용자나 확정하지 않은 상태는 알리지 않는다") {
            runTest(mainDispatcher) {
                val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns accountFlow
                val viewModel = FileAddAccountViewModel(getAccountUseCase = getAccountUseCase)

                viewModel.effect.test {
                    accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>())
                    accountFlow.value = Result.failure(IllegalStateException("account"))
                    expectNoEvents()

                    accountFlow.value = Result.success(Account.Guest)
                    awaitItem() shouldBe FileAddAccountEffect.BecameGuest
                    expectNoEvents()
                }
            }
        }
    }
}
