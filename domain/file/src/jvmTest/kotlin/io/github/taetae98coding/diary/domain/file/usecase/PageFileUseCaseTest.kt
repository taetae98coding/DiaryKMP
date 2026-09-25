package io.github.taetae98coding.diary.domain.file.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take

class PageFileUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정의 파일이 서버에 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val fileList = List(2) { fixtureMonkey.giveMeOne<DiaryFile>() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val fileRepository = mockk<FileRepository>()
            every { fileRepository.page() } returns flowOf(PagingData.from(fileList))
            val useCase = PageFileUseCase(getAccountUseCase = getAccountUseCase, fileRepository = fileRepository)

            When("파일 목록을 페이지로 조회한다") {
                Then("불러오는 중인 빈 목록 다음에 서버에서 불러온 파일을 페이지로 전달한다") {
                    val pagingDataList = useCase(parameter = Unit).take(2)

                    flowOf(pagingDataList.first().shouldBeSuccess()).asSnapshot().shouldBeEmpty()
                    flowOf(pagingDataList.last().shouldBeSuccess()).asSnapshot() shouldBe fileList
                }
            }
        }

        Given("게스트 상태다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val fileRepository = mockk<FileRepository>()
            val useCase = PageFileUseCase(getAccountUseCase = getAccountUseCase, fileRepository = fileRepository)

            When("파일 목록을 페이지로 조회한다") {
                Then("TC-FILE-HOME-FEATURE-005 서버에 요청하지 않고 빈 목록을 전달한다") {
                    val pagingData = useCase(parameter = Unit).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot().shouldBeEmpty()
                    verify(exactly = 0) { fileRepository.page() }
                }
            }
        }

        Given("계정 A로 파일 목록을 조회하고 있다") {
            val accountA = fixtureMonkey.giveMeOne<Account.User>()
            val accountB = fixtureMonkey.giveMeOne<Account.User>()
            val fileListA = List(2) { fixtureMonkey.giveMeOne<DiaryFile>() }
            val fileListB = List(2) { fixtureMonkey.giveMeOne<DiaryFile>() }
            val accountFlow = MutableStateFlow(Result.success<Account>(accountA))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val fileRepository = mockk<FileRepository>()
            every { fileRepository.page() } returns flowOf(PagingData.from(fileListA)) andThen flowOf(PagingData.from(fileListB))
            val useCase = PageFileUseCase(getAccountUseCase = getAccountUseCase, fileRepository = fileRepository)

            When("계정이 계정 B로 바뀐다") {
                Then("TC-FILE-HOME-DOMAIN-001 계정 A의 파일 대신 불러오는 중인 빈 목록을 보낸 뒤 계정 B의 파일을 처음부터 다시 불러온다") {
                    useCase(parameter = Unit).test {
                        awaitItem()
                        flowOf(awaitItem().shouldBeSuccess()).asSnapshot() shouldBe fileListA

                        accountFlow.value = Result.success(accountB)

                        flowOf(awaitItem().shouldBeSuccess()).asSnapshot().shouldBeEmpty()
                        flowOf(awaitItem().shouldBeSuccess()).asSnapshot() shouldBe fileListB
                        verify(exactly = 2) { fileRepository.page() }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("세션 갱신 여부가 서로 다른 사용자 상태다") {
            When("파일 목록을 페이지로 조회한다") {
                Then("TC-FILE-HOME-DOMAIN-003 세션 갱신 여부와 관계없이 서버에 목록을 요청한다") {
                    listOf(true to false, false to true, false to false).forEach { (isSessionValid, isSessionPending) ->
                        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = isSessionValid, isSessionPending = isSessionPending)
                        val getAccountUseCase = mockk<GetAccountUseCase>()
                        every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                        val fileRepository = mockk<FileRepository>()
                        every { fileRepository.page() } returns flowOf(PagingData.empty())
                        val useCase = PageFileUseCase(getAccountUseCase = getAccountUseCase, fileRepository = fileRepository)

                        useCase(parameter = Unit).take(2).last().shouldBeSuccess()

                        verify(exactly = 1) { fileRepository.page() }
                    }
                }
            }
        }

        Given("로그인한 계정으로 파일 목록을 조회하고 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val accountFlow = MutableStateFlow(Result.success<Account>(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val fileRepository = mockk<FileRepository>()
            every { fileRepository.page() } answers { flowOf(PagingData.empty()) }
            val useCase = PageFileUseCase(getAccountUseCase = getAccountUseCase, fileRepository = fileRepository)

            When("같은 계정의 세션 갱신 여부만 바뀐다") {
                Then("TC-FILE-HOME-DOMAIN-007 목록을 다시 불러오지 않는다") {
                    useCase(parameter = Unit).test {
                        awaitItem()
                        awaitItem()
                        accountFlow.value = Result.success(account.copy(isSessionValid = false, isSessionPending = true))

                        expectNoEvents()
                        verify(exactly = 1) { fileRepository.page() }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("계정 조회가 실패한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val fileRepository = mockk<FileRepository>()
            val useCase = PageFileUseCase(getAccountUseCase = getAccountUseCase, fileRepository = fileRepository)

            When("파일 목록을 페이지로 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
