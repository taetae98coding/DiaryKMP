package io.github.taetae98coding.diary.domain.sync.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.AccountSyncTimeRepository
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.Uuid

private fun requestSyncUseCase(
    getAccountUseCase: GetAccountUseCase,
    syncManager: SyncManager,
    accountSyncTimeRepository: AccountSyncTimeRepository = mockk(relaxed = true),
    accountSyncDataRepository: AccountSyncDataRepository = mockk(relaxed = true),
    clock: Clock = Clock.System,
): RequestSyncUseCase =
    RequestSyncUseCase(
        getAccountUseCase = getAccountUseCase,
        accountSyncTimeRepository = accountSyncTimeRepository,
        accountSyncDataRepository = accountSyncDataRepository,
        syncManager = syncManager,
        clock = clock,
    )

class RequestSyncUseCaseTest :
    BehaviorSpec({
        Given("로그인 세션이 인증된 사용자 계정이 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("메모나 태그에 변경이 발생해 동기화를 요청한다") {
                Then("TC-DATA-SYNC-DOMAIN-024 확인된 사용자 계정으로 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = false) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("TC-SYNC-REFRESH-FEATURE-001 확인된 사용자 계정으로 진행을 보고하는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.USER_REQUESTED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                }
            }

            When("앱이 인증된 사용자 계정을 확인해 동기화를 요청한다") {
                Then("확인된 사용자 계정으로 진행을 보고하는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                }
            }
        }

        Given("로그인 세션이 인증되지 않은 사용자 계정이 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase =
                requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

            When("동기화 계기가 발생한다") {
                Then("TC-DATA-SYNC-DOMAIN-014 동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.USER_REQUESTED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }

        Given("계정이 게스트 상태다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase =
                requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

            When("메모나 태그에 변경이 발생해 동기화를 요청한다") {
                Then("TC-DATA-SYNC-DOMAIN-002 TC-PLACE-ADD-DATA-008 TC-WEB-ADD-DATA-007 동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("TC-SYNC-REFRESH-FEATURE-008 TC-PLACE-HOME-DOMAIN-017 계정 데이터 서버 동기화가 요청되지 않아 진행 표시 대상도 없다") {
                    val result = useCase(parameter = SyncTrigger.USER_REQUESTED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }

        Given("로그아웃되어 인증된 계정이 없어졌다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase =
                requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

            When("앱이 계정 변화를 확인해 동기화를 요청한다") {
                Then("TC-DATA-SYNC-DOMAIN-036 동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }

        listOf(
            Triple("기록이 없다", null, false),
            Triple("29일 전이다", 29.days, false),
            Triple("30일 전이다", 30.days, true),
            Triple("31일 전이다", 31.days, true),
        ).forEach { (label, elapsed, isReset) ->
            Given("로그인 세션이 인증된 사용자 계정이 있고 그 계정의 마지막 동기화 시각이 $label") {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val now = fixtureMonkey.giveMeOne<Instant>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                val clock = mockk<Clock>()
                every { clock.now() } returns now

                When("동기화 계기가 발생한다") {
                    Then("TC-DATA-SYNC-DOMAIN-070 $label 강제 전체 재동기화 여부가 $isReset 이고 동기화는 한 번 요청된다") {
                        val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
                        coEvery { accountSyncTimeRepository.find(accountId = account.id) } returns elapsed?.let { now - it }
                        val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                        val syncManager = mockk<SyncManager>(relaxed = true)
                        val useCase =
                            requestSyncUseCase(
                                getAccountUseCase = getAccountUseCase,
                                syncManager = syncManager,
                                accountSyncTimeRepository = accountSyncTimeRepository,
                                accountSyncDataRepository = accountSyncDataRepository,
                                clock = clock,
                            )

                        val result = useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)

                        result.shouldBeSuccess(Unit)
                        coVerify(exactly = if (isReset) 1 else 0) { accountSyncDataRepository.delete(accountId = account.id) }
                        verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                    }
                }
            }
        }

        Given("한 기기에 마지막 동기화 시각이 29일 전인 계정과 31일 전인 계정이 기록되어 있다") {
            val recentAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val staleAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
            coEvery { accountSyncTimeRepository.find(accountId = recentAccount.id) } returns now - 29.days
            coEvery { accountSyncTimeRepository.find(accountId = staleAccount.id) } returns now - 31.days

            When("두 계정에 차례로 동기화 계기가 발생한다") {
                Then("TC-DATA-SYNC-DOMAIN-071 31일 전인 계정만 기기 데이터를 지운다") {
                    val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                    val syncManager = mockk<SyncManager>(relaxed = true)

                    listOf(recentAccount, staleAccount).forEach { account ->
                        val getAccountUseCase = mockk<GetAccountUseCase>()
                        every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                        requestSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                            accountSyncTimeRepository = accountSyncTimeRepository,
                            accountSyncDataRepository = accountSyncDataRepository,
                            clock = clock,
                        )(parameter = SyncTrigger.ACCOUNT_CONFIRMED).shouldBeSuccess(Unit)
                    }

                    coVerify(exactly = 0) { accountSyncDataRepository.delete(accountId = recentAccount.id) }
                    coVerify(exactly = 1) { accountSyncDataRepository.delete(accountId = staleAccount.id) }
                }
            }
        }

        Given("로그인 세션이 인증된 사용자 계정이 있고 그 계정의 마지막 동기화 시각이 31일 전으로 기록되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val now = fixtureMonkey.giveMeOne<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("동기화 계기가 발생해 강제 전체 재동기화가 진행된다") {
                Then("TC-DATA-SYNC-DOMAIN-074 같은 사용자 계정으로 동기화가 이어서 요청된다") {
                    val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
                    coEvery { accountSyncTimeRepository.find(accountId = account.id) } returns now - 31.days
                    val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                            accountSyncTimeRepository = accountSyncTimeRepository,
                            accountSyncDataRepository = accountSyncDataRepository,
                            clock = clock,
                        )

                    useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED).shouldBeSuccess(Unit)

                    coVerify(exactly = 1) { accountSyncDataRepository.delete(accountId = account.id) }
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                }
            }
        }

        listOf(SyncTrigger.ACCOUNT_CONFIRMED, SyncTrigger.DATA_CHANGED, SyncTrigger.USER_REQUESTED).forEach { trigger ->
            Given("마지막 동기화 시각이 31일 전인 계정이 있고 계기가 $trigger 다") {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val now = fixtureMonkey.giveMeOne<Instant>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                val clock = mockk<Clock>()
                every { clock.now() } returns now

                When("그 계기로 동기화를 요청한다") {
                    Then("TC-DATA-SYNC-DOMAIN-075 $trigger 계기에서도 강제 전체 재동기화가 시작된다") {
                        val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
                        coEvery { accountSyncTimeRepository.find(accountId = account.id) } returns now - 31.days
                        val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                        val syncManager = mockk<SyncManager>(relaxed = true)
                        val useCase =
                            requestSyncUseCase(
                                getAccountUseCase = getAccountUseCase,
                                syncManager = syncManager,
                                accountSyncTimeRepository = accountSyncTimeRepository,
                                accountSyncDataRepository = accountSyncDataRepository,
                                clock = clock,
                            )

                        useCase(parameter = trigger).shouldBeSuccess(Unit)

                        coVerify(exactly = 1) { accountSyncDataRepository.delete(accountId = account.id) }
                        verify(exactly = 1) { syncManager.requestSync(reportsProgress = any()) }
                    }
                }
            }
        }

        Given("마지막 동기화 시각이 31일 전인 계정이 강제 전체 재동기화로 기기 데이터를 한 번 지웠다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val firstTriggeredAt = fixtureMonkey.giveMeOne<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("30일이 지나기 전에 같은 계정에 동기화 계기가 다시 발생한다") {
                Then("TC-DATA-SYNC-DATA-038 기기 데이터를 다시 지우지 않는다") {
                    val storedSyncedAt = mutableMapOf(account.id to firstTriggeredAt - 31.days)
                    val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>()
                    coEvery { accountSyncTimeRepository.find(accountId = any()) } answers { storedSyncedAt[firstArg<Uuid>()] }
                    coEvery { accountSyncTimeRepository.upsert(accountId = any(), syncedAt = any()) } answers {
                        storedSyncedAt[firstArg<Uuid>()] = secondArg<Instant>()
                    }
                    val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                    val clock = mockk<Clock>()
                    every { clock.now() } returnsMany listOf(firstTriggeredAt, firstTriggeredAt + 29.days)
                    val useCase =
                        requestSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = mockk(relaxed = true),
                            accountSyncTimeRepository = accountSyncTimeRepository,
                            accountSyncDataRepository = accountSyncDataRepository,
                            clock = clock,
                        )

                    useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED).shouldBeSuccess(Unit)
                    useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED).shouldBeSuccess(Unit)

                    coVerify(exactly = 1) { accountSyncDataRepository.delete(accountId = account.id) }
                    coVerify(exactly = 1) { accountSyncTimeRepository.upsert(accountId = account.id, syncedAt = firstTriggeredAt) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase =
                requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

            When("동기화를 요청한다") {
                Then("계정 조회 실패를 전달하고 동기화를 요청하지 않는다") {
                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
