package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountPlaceMemoRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class PageFinishedPlaceMemoUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 대상 장소의 완료된 메모 페이지가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val pagingData = PagingData.from(listOf(fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountPlaceMemoRepository = mockk<AccountPlaceMemoRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountPlaceMemoRepository.pageFinished(account = account, placeId = placeId, sort = ListSort.DEFAULT) } returns flowOf(pagingData)
            val useCase =
                PageFinishedPlaceMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceMemoRepository = accountPlaceMemoRepository,
                )

            When("대상 장소의 완료된 메모 페이지를 조회한다") {
                Then("TC-ENTITY-MEMO-FINISHED-LIST-DATA-001 현재 계정과 대상 장소의 완료된 메모 페이지를 반환한다") {
                    useCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        accountPlaceMemoRepository.pageFinished(
                            account = account,
                            placeId = placeId,
                            sort = ListSort.DEFAULT,
                        )
                    }
                }

                Then("TC-ENTITY-MEMO-FINISHED-LIST-DATA-001 미완료 메모 목록 조회를 요청하지 않는다") {
                    useCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)).test {
                        awaitItem()
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountPlaceMemoRepository.page(
                            account = any(),
                            placeId = any(),
                            sort = any(),
                        )
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountPlaceMemoRepository = mockk<AccountPlaceMemoRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase =
                PageFinishedPlaceMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceMemoRepository = accountPlaceMemoRepository,
                )

            When("대상 장소의 완료된 메모 페이지를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 페이지를 요청하지 않는다") {
                    useCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)).test {
                        awaitItem().exceptionOrNull() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountPlaceMemoRepository.pageFinished(
                            account = any(),
                            placeId = any(),
                            sort = any(),
                        )
                    }
                }
            }
        }

        Given("대상 장소의 완료된 메모 페이지 조회가 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountPlaceMemoRepository = mockk<AccountPlaceMemoRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountPlaceMemoRepository.pageFinished(account = account, placeId = placeId, sort = ListSort.DEFAULT) } returns flow { throw failure }
            val useCase =
                PageFinishedPlaceMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceMemoRepository = accountPlaceMemoRepository,
                )

            When("대상 장소의 완료된 메모 페이지를 조회한다") {
                Then("저장소의 실패를 실패 결과로 전달한다") {
                    useCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)).test {
                        awaitItem()
                            .shouldBeFailure()
                            .shouldBeInstanceOf<IllegalStateException>()
                            .message shouldBe failure.message
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정이 바뀌고 이전 계정의 페이지도 이후에 변경되는 상황이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val accountPagingData = PagingData.from(listOf(fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())))
            val staleAccountPagingData = PagingData.from(List(2) { fixtureMonkey.memo(title = fixtureMonkey.nonBlankString()) })
            val otherAccountPagingData = PagingData.from(listOf(fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())))
            val changedOtherAccountPagingData = PagingData.from(List(3) { fixtureMonkey.memo(title = fixtureMonkey.nonBlankString()) })
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val accountPagingFlow = MutableStateFlow(accountPagingData)
            val otherAccountPagingFlow = MutableStateFlow(otherAccountPagingData)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountPlaceMemoRepository = mockk<AccountPlaceMemoRepository>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            every { accountPlaceMemoRepository.pageFinished(account = account, placeId = placeId, sort = ListSort.DEFAULT) } returns accountPagingFlow
            every { accountPlaceMemoRepository.pageFinished(account = otherAccount, placeId = placeId, sort = ListSort.DEFAULT) } returns otherAccountPagingFlow
            val useCase =
                PageFinishedPlaceMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountPlaceMemoRepository = accountPlaceMemoRepository,
                )

            When("계정이 바뀐 뒤 이전 계정과 현재 계정의 페이지가 차례로 바뀐다") {
                Then("이전 계정 관찰을 중단하고 현재 계정의 페이지 변경만 전달한다") {
                    useCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs accountPagingData

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().getOrThrow() shouldBeSameInstanceAs otherAccountPagingData

                        accountPagingFlow.value = staleAccountPagingData
                        expectNoEvents()

                        otherAccountPagingFlow.value = changedOtherAccountPagingData

                        awaitItem().getOrThrow() shouldBeSameInstanceAs changedOtherAccountPagingData
                        cancelAndIgnoreRemainingEvents()
                    }

                    verify(exactly = 1) {
                        accountPlaceMemoRepository.pageFinished(
                            account = account,
                            placeId = placeId,
                            sort = ListSort.DEFAULT,
                        )
                    }
                    verify(exactly = 1) {
                        accountPlaceMemoRepository.pageFinished(
                            account = otherAccount,
                            placeId = placeId,
                            sort = ListSort.DEFAULT,
                        )
                    }
                }
            }
        }
    })
