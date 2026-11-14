package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.routine.repository.AccountTagRoutineRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class RoutineHomePageRoutineUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 루틴 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val today = Arb.kotlinxLocalDate().next()
            val pagingData = PagingData.from(listOf(Arb.routineSummary().next()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountRoutineRepository>()
            every { repository.page(account = account, currentCycle = routineCurrentCycle(today = today)) } returns flowOf(pagingData)
            val useCase = PageRoutineUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = repository)

            When("오늘을 넘겨 루틴 목록을 조회한다") {
                Then("오늘이 속한 이번 주기로 조회한 현재 계정의 루틴 페이지를 반환한다") {
                    useCase(parameter = today).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountRoutineRepository>(relaxed = true)
            val useCase = PageRoutineUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = repository)

            When("루틴 목록을 조회한다") {
                Then("TC-d37eb7e7-5a57-467f-97e5-16f02aed1518 노출하는 루틴 없이 계정 조회의 실패를 그대로 전달한다") {
                    useCase(parameter = Arb.kotlinxLocalDate().next()).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.page(account = any(), currentCycle = any()) }
                }
            }
        }

        Given("태그와 연결된 현재 계정의 루틴 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val today = Arb.kotlinxLocalDate().next()
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val pagingData = PagingData.from(listOf(Arb.routineSummary().next()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountTagRoutineRepository>()
            every { repository.page(account = account, tagId = tagId, scope = scope, currentCycle = routineCurrentCycle(today = today)) } returns flowOf(pagingData)
            val useCase = PageTagRoutineUseCase(getAccountUseCase = getAccountUseCase, accountTagRoutineRepository = repository)

            When("태그와 표시 범위, 오늘을 넘겨 태그의 루틴을 조회한다") {
                Then("오늘이 속한 이번 주기로 조회한 그 태그의 루틴 페이지를 반환한다") {
                    useCase(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = scope, today = today)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정의 완료된 루틴 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val today = Arb.kotlinxLocalDate().next()
            val pagingData = PagingData.from(listOf(Arb.routineSummary().next()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountRoutineRepository>()
            every { repository.pageFinished(account = account, currentCycle = routineCurrentCycle(today = today)) } returns flowOf(pagingData)
            val useCase = PageFinishedRoutineUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = repository)

            When("오늘을 넘겨 완료된 루틴 목록을 조회한다") {
                Then("TC-c07f8ee0-eefb-4858-91af-01ccb759f3e2 오늘이 속한 이번 주기로 조회한 현재 계정의 완료된 루틴 페이지를 반환한다") {
                    useCase(parameter = today).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.page(account = any(), currentCycle = any()) }
                }
            }
        }

        Given("완료된 루틴 목록을 조회할 때 현재 계정 조회가 실패하도록 설정되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountRoutineRepository>(relaxed = true)
            val tagRepository = mockk<AccountTagRoutineRepository>(relaxed = true)
            val useCase = PageFinishedRoutineUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = repository)
            val tagUseCase = PageFinishedTagRoutineUseCase(getAccountUseCase = getAccountUseCase, accountTagRoutineRepository = tagRepository)

            When("완료된 루틴 목록과 태그의 완료된 루틴 목록을 조회한다") {
                Then("노출하는 루틴 없이 계정 조회의 실패를 그대로 전달한다") {
                    useCase(parameter = Arb.kotlinxLocalDate().next()).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }
                    tagUseCase(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = Arb.kotlinUuid().next(), today = Arb.kotlinxLocalDate().next())).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.pageFinished(account = any(), currentCycle = any()) }
                    verify(exactly = 0) { tagRepository.pageFinished(account = any(), tagId = any(), currentCycle = any()) }
                }
            }
        }

        Given("태그와 연결된 현재 계정의 완료된 루틴 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val today = Arb.kotlinxLocalDate().next()
            val tagId = Arb.kotlinUuid().next()
            val pagingData = PagingData.from(listOf(Arb.routineSummary().next()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountTagRoutineRepository>()
            every { repository.pageFinished(account = account, tagId = tagId, currentCycle = routineCurrentCycle(today = today)) } returns flowOf(pagingData)
            val useCase = PageFinishedTagRoutineUseCase(getAccountUseCase = getAccountUseCase, accountTagRoutineRepository = repository)

            When("태그와 오늘을 넘겨 태그의 완료된 루틴을 조회한다") {
                Then("TC-a790ed25-431f-4048-8ab6-c7c2c5245389 표시 범위 없이 그 태그의 완료된 루틴 페이지를 반환한다") {
                    useCase(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = today)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.page(account = any(), tagId = any(), scope = any(), currentCycle = any()) }
                }
            }
        }
    })
