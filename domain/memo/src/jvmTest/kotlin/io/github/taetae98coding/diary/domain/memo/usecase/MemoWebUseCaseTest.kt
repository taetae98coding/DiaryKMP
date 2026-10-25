package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoWebRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoWebUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val getAccountUseCase = accountUseCase(account = account)
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("웹 항목를 추가한다") {
                Then("TC-858a06f1-a316-4252-9a7f-831cc0ea6505 메모와 그 웹 항목의 연결을 해제되지 않은 상태와 선택 시점으로 저장한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoWebRepository = accountMemoWebRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoWebUseCase.Parameter(memoId = memoId, webId = webId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoWebRepository.upsert(
                            account = account,
                            memoId = memoId,
                            webId = webId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-f77630ce-437e-4210-9c94-155dfcab66a0 TC-16582126-e239-4cef-91b8-c8ac38486eda 연결 변경을 서버와 맞추기 위한 동기화를 요청한다") {
                    val useCase =
                        AddMemoWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoWebRepository = accountMemoWebRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                AddMemoWebUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    webId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeSuccess()
                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("웹 항목를 해제한다") {
                Then("TC-20e23ee0-6926-4285-9161-5d9bf265aff7 메모와 그 웹 항목의 연결을 해제 상태와 해제 시점으로 저장한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoWebRepository = accountMemoWebRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memoId, webId = webId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoWebRepository.upsert(
                            account = account,
                            memoId = memoId,
                            webId = webId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                    }
                }
            }
        }

        Given("동기화 요청이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = accountUseCase(account = account)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.failure(throwable)
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("웹 항목를 추가한다") {
                Then("TC-ea37a9e5-9ce0-467b-b523-ac575b70000d 저장한 웹 연결을 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoWebRepository = accountMemoWebRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoWebUseCase.Parameter(memoId = memoId, webId = webId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoWebRepository.upsert(account = account, memoId = memoId, webId = webId, isDeleted = false, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoWebRepository.upsert(account = account, memoId = memoId, webId = webId, isDeleted = true, updatedAt = any())
                    }
                }
            }

            When("웹 항목를 해제한다") {
                Then("TC-ea37a9e5-9ce0-467b-b523-ac575b70000d 저장한 웹 연결 해제를 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoWebUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoWebRepository = accountMemoWebRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memoId, webId = webId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoWebRepository.upsert(account = account, memoId = memoId, webId = webId, isDeleted = true, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoWebRepository.upsert(account = account, memoId = memoId, webId = webId, isDeleted = false, updatedAt = any())
                    }
                }
            }
        }

        Given("저장이 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            coEvery {
                accountMemoWebRepository.upsert(
                    account = any(),
                    memoId = any(),
                    webId = any(),
                    isDeleted = any(),
                    updatedAt = any(),
                )
            } throws throwable
            val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                AddMemoWebUseCase(
                    getAccountUseCase = accountUseCase(),
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoWebRepository = accountMemoWebRepository,
                    clock = clock,
                )

            When("웹 항목를 추가한다") {
                Then("실패를 그대로 전달한다") {
                    val result =
                        useCase(
                            parameter =
                                AddMemoWebUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    webId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("로그인한 계정과 메모에 연결된 웹 항목가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val webList = List(2) { web() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            every { accountMemoWebRepository.getWebList(account = account, memoId = memoId) } returns flowOf(webList)
            val useCase =
                GetMemoWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoWebRepository = accountMemoWebRepository,
                )

            When("메모의 웹 항목를 조회한다") {
                Then("현재 계정의 메모에 연결된 웹 항목를 전달한다") {
                    useCase(parameter = memoId).first().shouldBeSuccess(webList)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>(relaxed = true)
            val useCase =
                GetMemoWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoWebRepository = accountMemoWebRepository,
                )

            When("메모의 웹 항목를 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
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

        private fun web(): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Web::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun accountUseCase(account: Account = fixtureMonkey.giveMeOne<Account.User>()): GetAccountUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            return getAccountUseCase
        }
    }
}
