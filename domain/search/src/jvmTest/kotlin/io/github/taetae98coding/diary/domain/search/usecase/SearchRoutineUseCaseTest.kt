package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchRoutineRepository
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class SearchRoutineUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 질의를 만족하는 루틴이 준비되어 있다") {
            val query = Arb.alphanumericString(prefix = "Query").next()
            val today = Arb.kotlinxLocalDate().next()
            val account = Arb.accountUser().next()
            val routineList = List(2) { Arb.routineSummary().next() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountSearchRoutineRepository = mockk<AccountSearchRoutineRepository>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            every {
                accountSearchRoutineRepository.page(account = account, query = query, currentCycle = routineCurrentCycle(today = today))
            } returns flowOf(PagingData.from(routineList))
            val useCase =
                SearchRoutineUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSearchRoutineRepository = accountSearchRoutineRepository,
                )

            When("질의로 루틴을 검색한다") {
                Then("TC-cd483a5b-ad04-4c6c-bedb-ea734aa93d42 현재 계정의 검색 결과를 오늘이 속한 이번 주기로 조회해 페이지로 전달한다") {
                    val pagingData = useCase(parameter = SearchRoutineUseCase.Parameter(query = query, today = today)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe routineList
                }

                Then("TC-f5cf35ea-6c4b-44ce-98c9-7e74630d7a70 앞뒤 공백을 뺀 질의로 조회한다") {
                    val pagingData = useCase(parameter = SearchRoutineUseCase.Parameter(query = "  $query  ", today = today)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe routineList
                }
            }
        }

        Given("로그인하지 않은 게스트 상태이고 질의를 만족하는 루틴이 준비되어 있다") {
            val query = Arb.alphanumericString(prefix = "Query").next()
            val today = Arb.kotlinxLocalDate().next()
            val routineList = List(2) { Arb.routineSummary().next() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountSearchRoutineRepository = mockk<AccountSearchRoutineRepository>()
            every { getAccountUseCase() } returns flowOf(Result.success(Account.Guest))
            every {
                accountSearchRoutineRepository.page(account = Account.Guest, query = query, currentCycle = routineCurrentCycle(today = today))
            } returns flowOf(PagingData.from(routineList))
            val useCase =
                SearchRoutineUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSearchRoutineRepository = accountSearchRoutineRepository,
                )

            When("질의로 루틴을 검색한다") {
                Then("TC-9340ee09-dab8-45c0-9f68-8fc24d788378 게스트 계정의 검색 결과를 같은 기준으로 전달한다") {
                    val pagingData = useCase(parameter = SearchRoutineUseCase.Parameter(query = query, today = today)).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe routineList
                }
            }
        }

        Given("빈 질의가 준비되어 있다") {
            val today = Arb.kotlinxLocalDate().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountSearchRoutineRepository = mockk<AccountSearchRoutineRepository>()
            val useCase =
                SearchRoutineUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSearchRoutineRepository = accountSearchRoutineRepository,
                )

            When("빈 질의로 루틴을 검색한다") {
                Then("TC-414698a7-cd5f-402b-a542-d9369f88dce2 결과가 비어 있고 검색을 요청하지 않는다") {
                    listOf("", " ", "   ").forEach { query ->
                        val pagingData = useCase(parameter = SearchRoutineUseCase.Parameter(query = query, today = today)).first().shouldBeSuccess()

                        flowOf(pagingData).asSnapshot().shouldBeEmpty()
                    }

                    verify(exactly = 0) { accountSearchRoutineRepository.page(account = any(), query = any(), currentCycle = any()) }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val query = Arb.alphanumericString(prefix = "Query").next()
            val today = Arb.kotlinxLocalDate().next()
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountSearchRoutineRepository = mockk<AccountSearchRoutineRepository>(relaxed = true)
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
            val useCase =
                SearchRoutineUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSearchRoutineRepository = accountSearchRoutineRepository,
                )

            When("질의로 루틴을 검색한다") {
                Then("TC-c0df0eeb-3fdb-49a4-bdac-6302eff2f7a3 계정 조회 실패를 전달하고 검색을 요청하지 않는다") {
                    useCase(parameter = SearchRoutineUseCase.Parameter(query = query, today = today))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountSearchRoutineRepository.page(account = any(), query = any(), currentCycle = any()) }
                }
            }
        }
    })
