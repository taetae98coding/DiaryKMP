@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.more.ui.home.account

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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class MoreHomeAccountViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-307a01f7-d920-4a94-b0b4-cae3a7982b65 로그인한 사용자 정보가 없으면 게스트 상태로 표시한다") {
            runTest(mainDispatcher) {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(Unit) } returns flowOf(Result.success<Account>(Account.Guest))
                val viewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeAccountUiState.Loading
                    awaitItem() shouldBe MoreHomeAccountUiState.Guest
                }
            }
        }

        test("TC-11459454-f5e9-472e-91b9-f8438bdae9dd 로그인한 사용자 정보가 있으면 사용자 상태로 표시한다") {
            runTest(mainDispatcher) {
                val email = fixtureMonkey.giveMeOne<String>()
                val profileImage = fixtureMonkey.giveMeOne<String>()
                val account =
                    Account.User(
                        id = fixtureMonkey.giveMeOne<Uuid>(),
                        profileImage = profileImage,
                        email = email,
                        isSessionValid = fixtureMonkey.giveMeOne<Boolean>(),
                    )
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(Unit) } returns flowOf(Result.success<Account>(account))
                val viewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeAccountUiState.Loading
                    awaitItem() shouldBe MoreHomeAccountUiState.User(profileImage = profileImage, email = email)
                }
            }
        }

        test("TC-eaa44711-0140-4979-b183-7e0a83206623 계정 정보를 확인하기 전이나 확인에 실패하면 확인 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val accountFlows =
                    listOf(
                        emptyFlow(),
                        flowOf(Result.failure<Account>(IllegalStateException("account error"))),
                    )

                accountFlows.forEach { accountFlow ->
                    val getAccountUseCase = mockk<GetAccountUseCase>()
                    every { getAccountUseCase(Unit) } returns accountFlow
                    val viewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe MoreHomeAccountUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-bbe25547-96a8-45ad-8cf2-ea4b3523a159 계정 상태나 표시 정보가 바뀌면 별도 조작 없이 바뀐 내용을 표시한다") {
            runTest(mainDispatcher) {
                val user = user()
                val changedUser = user.copy(email = "changed-" + fixtureMonkey.giveMeOne<String>(), profileImage = "changed-" + fixtureMonkey.giveMeOne<String>())
                val userUiState = MoreHomeAccountUiState.User(profileImage = user.profileImage, email = user.email)
                val changedUserUiState = MoreHomeAccountUiState.User(profileImage = changedUser.profileImage, email = changedUser.email)
                val caseList =
                    listOf(
                        AccountChangeCase(before = Account.Guest, beforeUiState = MoreHomeAccountUiState.Guest, after = user, afterUiState = userUiState),
                        AccountChangeCase(before = user, beforeUiState = userUiState, after = Account.Guest, afterUiState = MoreHomeAccountUiState.Guest),
                        AccountChangeCase(before = user, beforeUiState = userUiState, after = changedUser, afterUiState = changedUserUiState),
                    )

                caseList.forEach { case ->
                    val accountFlow = MutableStateFlow(Result.success(case.before))
                    val getAccountUseCase = mockk<GetAccountUseCase>()
                    every { getAccountUseCase(Unit) } returns accountFlow
                    val viewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe MoreHomeAccountUiState.Loading
                        awaitItem() shouldBe case.beforeUiState

                        accountFlow.value = Result.success(case.after)

                        awaitItem() shouldBe case.afterUiState
                    }
                }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private data class AccountChangeCase(
            val before: Account,
            val beforeUiState: MoreHomeAccountUiState,
            val after: Account,
            val afterUiState: MoreHomeAccountUiState,
        )

        private fun user(): Account.User =
            Account.User(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                profileImage = fixtureMonkey.giveMeOne<String>(),
                email = fixtureMonkey.giveMeOne<String>(),
                isSessionValid = fixtureMonkey.giveMeOne<Boolean>(),
            )
    }
}
