package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

class RoutineHomeViewScopeUseCaseTest :
    BehaviorSpec({
        Given("보기 범위가 오늘 할 루틴이고 오늘이 요일마다 다르다") {
            listOf(
                LocalDate(2026, 7, 10) to setOf(RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY),
                LocalDate(2026, 7, 11) to setOf(RoutineCycle.DAILY, RoutineCycle.WEEKENDS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY),
                LocalDate(2026, 7, 12) to setOf(RoutineCycle.DAILY, RoutineCycle.WEEKENDS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY),
                LocalDate(2026, 7, 13) to setOf(RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY),
            ).forEach { (today, cycleSet) ->
                When("오늘 ${today}로 루틴 목록을 조회한다") {
                    val context = PageContext(viewScope = RoutineViewScope.TODAY)

                    Then("TC-484e3e37-a791-40e0-a578-b47f5b5a4816 시작한 루틴 가운데 오늘의 요일에 하는 주기만 조회한다") {
                        context.useCase(parameter = today).test {
                            awaitItem().getOrThrow() shouldBeSameInstanceAs context.startedPagingData
                            awaitComplete()
                        }

                        verify(exactly = 1) { context.repository.pageStarted(account = context.account, currentCycle = routineCurrentCycle(today = today), cycleSet = cycleSet) }
                        verify(exactly = 0) { context.repository.page(account = any(), currentCycle = any()) }
                    }
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴이고 오늘이 공휴일인 금요일 2026-07-17이다") {
            val context = PageContext(viewScope = RoutineViewScope.TODAY)
            val today = LocalDate(2026, 7, 17)

            When("루틴 목록을 조회한다") {
                Then("TC-bb93d3d7-ef7b-40bf-9571-c9dae3bc536e 요일로만 판정해 평일만을 넣고 주말만을 빼고 조회한다") {
                    context.useCase(parameter = today).test {
                        awaitItem().shouldBeSuccess()
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        context.repository.pageStarted(
                            account = context.account,
                            currentCycle = routineCurrentCycle(today = today),
                            cycleSet = setOf(RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY),
                        )
                    }
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴이고 평일만 루틴이 조회 대상인지 확인한다") {
            listOf(
                LocalDate(2026, 11, 9) to true,
                LocalDate(2026, 11, 10) to true,
                LocalDate(2026, 11, 11) to true,
                LocalDate(2026, 11, 12) to true,
                LocalDate(2026, 11, 13) to true,
                LocalDate(2026, 11, 14) to false,
                LocalDate(2026, 11, 15) to false,
                LocalDate(2026, 11, 16) to true,
            ).forEach { (today, expected) ->
                When("오늘 ${today}로 루틴 목록을 조회한다") {
                    val cycleSet = PageContext(viewScope = RoutineViewScope.TODAY).queriedCycleSet(today = today)

                    Then("TC-aa1f73aa-2d48-4a8e-a0f5-502d86cbc445 월요일부터 금요일 사이일 때만 평일만 주기를 조회에 넣는다") {
                        (RoutineCycle.WEEKDAYS in cycleSet) shouldBe expected
                    }
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴이고 주말만 루틴이 조회 대상인지 확인한다") {
            listOf(
                LocalDate(2026, 11, 13) to false,
                LocalDate(2026, 11, 14) to true,
                LocalDate(2026, 11, 15) to true,
                LocalDate(2026, 11, 16) to false,
                LocalDate(2026, 11, 11) to false,
            ).forEach { (today, expected) ->
                When("오늘 ${today}로 루틴 목록을 조회한다") {
                    val cycleSet = PageContext(viewScope = RoutineViewScope.TODAY).queriedCycleSet(today = today)

                    Then("TC-04aa51df-0ce8-4ee5-a128-5c1e1cd657c6 토요일이나 일요일일 때만 주말만 주기를 조회에 넣는다") {
                        (RoutineCycle.WEEKENDS in cycleSet) shouldBe expected
                    }
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴이고 오늘이 일요일부터 토요일까지 바뀐다") {
            (8..14).map { day -> LocalDate(2026, 11, day) }.forEach { today ->
                When("오늘 ${today}로 루틴 목록을 조회한다") {
                    val cycleSet = PageContext(viewScope = RoutineViewScope.TODAY).queriedCycleSet(today = today)

                    Then("TC-ec997da8-0a75-4681-b7df-14198aacf409 매일, 매주, 매월 주기는 어느 요일에나 조회에 넣는다") {
                        cycleSet shouldContainAll setOf(RoutineCycle.DAILY, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY)
                    }
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴이고 오늘이 공휴일인 금요일 2026-12-25다") {
            When("루틴 목록을 조회한다") {
                // 조회는 공휴일 정보를 입력으로 받지 않고 날짜의 요일로만 주기를 고른다.
                val cycleSet = PageContext(viewScope = RoutineViewScope.TODAY).queriedCycleSet(today = LocalDate(2026, 12, 25))

                Then("TC-78d686ee-08a5-4885-9e15-967d2128d2f6 평일만과 매일은 넣고 주말만은 넣지 않는다") {
                    (RoutineCycle.WEEKDAYS in cycleSet) shouldBe true
                    (RoutineCycle.WEEKENDS in cycleSet) shouldBe false
                    (RoutineCycle.DAILY in cycleSet) shouldBe true
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴이고 오늘이 어느 날짜든") {
            When("루틴 목록을 조회한다") {
                val context = PageContext(viewScope = RoutineViewScope.TODAY)
                val today = LocalDate(2026, 11, 13)
                context.queriedCycleSet(today = today)

                Then("TC-63e99f28-7f79-45d0-8c89-ffa5f3e1ef10 TC-1e4eafd2-87db-4b16-8363-6dc31e5901e4 완료되지 않았고 시작한 루틴만 돌려주는 조회에 오늘을 넘긴다") {
                    verify(exactly = 1) { context.repository.pageStarted(account = context.account, currentCycle = routineCurrentCycle(today = today), cycleSet = any()) }
                    verify(exactly = 0) { context.repository.page(account = any(), currentCycle = any()) }
                }
            }
        }

        Given("보기 범위가 모든 루틴이다") {
            val context = PageContext(viewScope = RoutineViewScope.ALL)
            val today = LocalDate(2026, 7, 15)

            When("루틴 목록을 조회한다") {
                Then("TC-ddf146fd-79f8-40e3-ae8e-70d353a32cbf 시작 날짜와 요일로 좁히지 않고 완료되지 않은 루틴을 모두 조회한다") {
                    context.useCase(parameter = today).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs context.allPagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) { context.repository.page(account = context.account, currentCycle = routineCurrentCycle(today = today)) }
                    verify(exactly = 0) { context.repository.pageStarted(account = any(), currentCycle = any(), cycleSet = any()) }
                }
            }
        }

        Given("보기 범위가 오늘 할 루틴인 채 루틴 목록을 관찰하고 있다") {
            val viewScopeFlow = MutableStateFlow(RoutineViewScope.TODAY)
            val context = PageContext(viewScopeFlow = viewScopeFlow)

            When("저장된 보기 범위가 모든 루틴으로 바뀌었다가 오늘 할 루틴으로 되돌아온다") {
                Then("TC-07fef83b-4cac-4e3a-8c90-ed97fcd1cf84 다시 조회를 요청하지 않아도 바뀐 범위의 목록이 이어서 전달된다") {
                    context.useCase(parameter = LocalDate(2026, 7, 15)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs context.startedPagingData

                        viewScopeFlow.value = RoutineViewScope.ALL
                        awaitItem().getOrThrow() shouldBeSameInstanceAs context.allPagingData

                        viewScopeFlow.value = RoutineViewScope.TODAY
                        awaitItem().getOrThrow() shouldBeSameInstanceAs context.startedPagingData

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("보기 범위 조회가 같은 값을 거듭 전달한다") {
            val context = PageContext(viewScopeFlow = flowOf(RoutineViewScope.ALL, RoutineViewScope.ALL, RoutineViewScope.ALL))

            When("루틴 목록을 조회한다") {
                Then("TC-8389da95-7521-4222-9754-2e7007fb3d13 같은 범위로는 목록을 다시 조회하지 않는다") {
                    context.useCase(parameter = LocalDate(2026, 7, 15)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs context.allPagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) { context.repository.page(account = any(), currentCycle = any()) }
                }
            }
        }

        Given("현재 계정 조회가 실패한다") {
            val failure = IllegalStateException(Arb.string().next())
            val context = PageContext(accountResult = Result.failure(failure))

            When("루틴 목록, 보기 범위와 완료되지 않은 루틴의 유무를 조회한다") {
                Then("저장소를 조회하지 않고 계정 조회의 실패를 그대로 전달한다") {
                    context.useCase(parameter = LocalDate(2026, 7, 15)).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }
                    GetRoutineViewScopeUseCase(getAccountUseCase = context.getAccountUseCase, accountRoutineFilterRepository = context.filterRepository)().test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }
                    GetUnfinishedRoutineExistsUseCase(getAccountUseCase = context.getAccountUseCase, accountRoutineRepository = context.repository)().test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) { context.filterRepository.getViewScope(account = any()) }
                    verify(exactly = 0) { context.repository.getUnfinishedExists(account = any()) }
                }
            }
        }

        Given("현재 계정의 보기 범위 선택이 저장되어 있다") {
            val viewScope = Arb.enum<RoutineViewScope>().next()
            val context = PageContext(viewScope = viewScope)
            val useCase = GetRoutineViewScopeUseCase(getAccountUseCase = context.getAccountUseCase, accountRoutineFilterRepository = context.filterRepository)

            When("보기 범위를 조회한다") {
                Then("현재 계정의 보기 범위를 전달한다") {
                    useCase().test {
                        awaitItem().shouldBeSuccess() shouldBe viewScope
                        awaitComplete()
                    }

                    verify(exactly = 1) { context.filterRepository.getViewScope(account = context.account) }
                }
            }
        }

        Given("현재 계정에 완료되지 않은 루틴이 있는지가 정해져 있다") {
            val exists = Arb.boolean().next()
            val context = PageContext()
            every { context.repository.getUnfinishedExists(account = context.account) } returns flowOf(exists)
            val useCase = GetUnfinishedRoutineExistsUseCase(getAccountUseCase = context.getAccountUseCase, accountRoutineRepository = context.repository)

            When("완료되지 않은 루틴의 유무를 조회한다") {
                Then("현재 계정 기준의 유무를 전달한다") {
                    useCase().test {
                        awaitItem().shouldBeSuccess() shouldBe exists
                        awaitComplete()
                    }
                }
            }
        }

        Given("로그인한 계정의 보기 범위를 고른다") {
            RoutineViewScope.entries.forEach { viewScope ->
                val context = PageContext()
                coEvery { context.filterRepository.upsertViewScope(account = any(), viewScope = any()) } returns Unit
                val useCase = SetRoutineViewScopeUseCase(getAccountUseCase = context.getAccountUseCase, accountRoutineFilterRepository = context.filterRepository)

                When("${viewScope}로 보기 범위 저장을 요청한다") {
                    val result = useCase(parameter = viewScope)

                    Then("TC-80220c94-0abb-4324-ba26-4297993a422d 현재 계정의 보기 범위 선택 하나로 저장한다") {
                        result.shouldBeSuccess()
                        coVerify(exactly = 1) { context.filterRepository.upsertViewScope(account = context.account, viewScope = viewScope) }
                    }
                }
            }
        }

        Given("보기 범위 선택의 저장이 실패한다") {
            val failure = IllegalStateException(Arb.string().next())
            val context = PageContext()
            coEvery { context.filterRepository.upsertViewScope(account = any(), viewScope = any()) } throws failure
            val useCase = SetRoutineViewScopeUseCase(getAccountUseCase = context.getAccountUseCase, accountRoutineFilterRepository = context.filterRepository)

            When("보기 범위 저장을 요청한다") {
                val result = useCase(parameter = RoutineViewScope.ALL)

                Then("TC-ab4d2410-0156-42b1-b2f5-e77016b9d15e 저장 실패를 그대로 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("새 계정의 처음 보기 범위를 확인한다") {
            Then("TC-829e32df-b32a-4101-812c-58ee288ec037 처음 보기 범위는 오늘 할 루틴이다") {
                ROUTINE_DEFAULT_VIEW_SCOPE shouldBe RoutineViewScope.TODAY
            }
        }
    })

private class PageContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
    viewScope: RoutineViewScope = RoutineViewScope.TODAY,
    viewScopeFlow: Flow<RoutineViewScope> = flowOf(viewScope),
) {
    val account: Account = accountResult.getOrNull() ?: Account.Guest
    val startedPagingData: PagingData<RoutineSummary> = PagingData.from(listOf(Arb.routineSummary().next()))
    val allPagingData: PagingData<RoutineSummary> = PagingData.from(listOf(Arb.routineSummary().next()))

    val getAccountUseCase: GetAccountUseCase =
        mockk<GetAccountUseCase>().also { useCase ->
            every { useCase() } returns flowOf(accountResult)
        }

    val repository: AccountRoutineRepository =
        mockk<AccountRoutineRepository>().also { repository ->
            every { repository.pageStarted(account = any(), currentCycle = any(), cycleSet = any()) } returns flowOf(startedPagingData)
            every { repository.page(account = any(), currentCycle = any()) } returns flowOf(allPagingData)
        }

    val filterRepository: AccountRoutineFilterRepository =
        mockk<AccountRoutineFilterRepository>().also { repository ->
            every { repository.getViewScope(account = any()) } returns viewScopeFlow
        }

    val useCase: PageViewScopeRoutineUseCase =
        PageViewScopeRoutineUseCase(
            getAccountUseCase = getAccountUseCase,
            accountRoutineRepository = repository,
            accountRoutineFilterRepository = filterRepository,
        )

    suspend fun queriedCycleSet(today: LocalDate): Set<RoutineCycle> {
        val cycleSet = slot<Set<RoutineCycle>>()
        every { repository.pageStarted(account = any(), currentCycle = any(), cycleSet = capture(cycleSet)) } returns flowOf(startedPagingData)

        useCase(parameter = today).first()

        return cycleSet.captured
    }
}
