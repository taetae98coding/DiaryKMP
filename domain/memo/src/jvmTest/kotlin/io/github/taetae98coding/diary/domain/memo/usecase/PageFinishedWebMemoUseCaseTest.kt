package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountWebMemoRepository
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

class PageFinishedWebMemoUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 대상 웹 항목의 완료된 메모 페이지가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val pagingData = PagingData.from(listOf(fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountWebMemoRepository = mockk<AccountWebMemoRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountWebMemoRepository.pageFinished(account = account, webId = webId, sort = ListSort.DEFAULT) } returns flowOf(pagingData)
            val useCase =
                PageFinishedWebMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebMemoRepository = accountWebMemoRepository,
                )

            When("대상 웹 항목의 완료된 메모 페이지를 조회한다") {
                Then("TC-e446b0d4-096a-4328-a3fa-1e26b34bd16f 현재 계정과 대상 웹 항목의 완료된 메모 페이지를 반환한다") {
                    useCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = ListSort.DEFAULT)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        accountWebMemoRepository.pageFinished(
                            account = account,
                            webId = webId,
                            sort = ListSort.DEFAULT,
                        )
                    }
                }

                Then("TC-e446b0d4-096a-4328-a3fa-1e26b34bd16f 미완료 메모 목록 조회를 요청하지 않는다") {
                    useCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = ListSort.DEFAULT)).test {
                        awaitItem()
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountWebMemoRepository.page(
                            account = any(),
                            webId = any(),
                            sort = any(),
                        )
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountWebMemoRepository = mockk<AccountWebMemoRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase =
                PageFinishedWebMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebMemoRepository = accountWebMemoRepository,
                )

            When("대상 웹 항목의 완료된 메모 페이지를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 페이지를 요청하지 않는다") {
                    useCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = ListSort.DEFAULT)).test {
                        awaitItem().exceptionOrNull() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountWebMemoRepository.pageFinished(
                            account = any(),
                            webId = any(),
                            sort = any(),
                        )
                    }
                }
            }
        }

        Given("대상 웹 항목의 완료된 메모 페이지 조회가 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountWebMemoRepository = mockk<AccountWebMemoRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountWebMemoRepository.pageFinished(account = account, webId = webId, sort = ListSort.DEFAULT) } returns flow { throw failure }
            val useCase =
                PageFinishedWebMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebMemoRepository = accountWebMemoRepository,
                )

            When("대상 웹 항목의 완료된 메모 페이지를 조회한다") {
                Then("저장소의 실패를 실패 결과로 전달한다") {
                    useCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = ListSort.DEFAULT)).test {
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
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val accountPagingData = PagingData.from(listOf(fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())))
            val staleAccountPagingData = PagingData.from(List(2) { fixtureMonkey.memo(title = fixtureMonkey.nonBlankString()) })
            val otherAccountPagingData = PagingData.from(listOf(fixtureMonkey.memo(title = fixtureMonkey.nonBlankString())))
            val changedOtherAccountPagingData = PagingData.from(List(3) { fixtureMonkey.memo(title = fixtureMonkey.nonBlankString()) })
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val accountPagingFlow = MutableStateFlow(accountPagingData)
            val otherAccountPagingFlow = MutableStateFlow(otherAccountPagingData)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountWebMemoRepository = mockk<AccountWebMemoRepository>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            every { accountWebMemoRepository.pageFinished(account = account, webId = webId, sort = ListSort.DEFAULT) } returns accountPagingFlow
            every { accountWebMemoRepository.pageFinished(account = otherAccount, webId = webId, sort = ListSort.DEFAULT) } returns otherAccountPagingFlow
            val useCase =
                PageFinishedWebMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebMemoRepository = accountWebMemoRepository,
                )

            When("계정이 바뀐 뒤 이전 계정과 현재 계정의 페이지가 차례로 바뀐다") {
                Then("이전 계정 관찰을 중단하고 현재 계정의 페이지 변경만 전달한다") {
                    useCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = ListSort.DEFAULT)).test {
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
                        accountWebMemoRepository.pageFinished(
                            account = account,
                            webId = webId,
                            sort = ListSort.DEFAULT,
                        )
                    }
                    verify(exactly = 1) {
                        accountWebMemoRepository.pageFinished(
                            account = otherAccount,
                            webId = webId,
                            sort = ListSort.DEFAULT,
                        )
                    }
                }
            }
        }
    })
