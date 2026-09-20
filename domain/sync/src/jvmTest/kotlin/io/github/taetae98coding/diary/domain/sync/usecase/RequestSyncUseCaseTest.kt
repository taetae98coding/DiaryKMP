package io.github.taetae98coding.diary.domain.sync.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

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
                        RequestSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = false) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("TC-SYNC-REFRESH-FEATURE-001 확인된 사용자 계정으로 진행을 보고하는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        RequestSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

                    val result = useCase(parameter = SyncTrigger.USER_REQUESTED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                }
            }

            When("앱이 인증된 사용자 계정을 확인해 동기화를 요청한다") {
                Then("확인된 사용자 계정으로 진행을 보고하는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        RequestSyncUseCase(
                            getAccountUseCase = getAccountUseCase,
                            syncManager = syncManager,
                        )

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
                RequestSyncUseCase(
                    getAccountUseCase = getAccountUseCase,
                    syncManager = syncManager,
                )

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
                RequestSyncUseCase(
                    getAccountUseCase = getAccountUseCase,
                    syncManager = syncManager,
                )

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
                RequestSyncUseCase(
                    getAccountUseCase = getAccountUseCase,
                    syncManager = syncManager,
                )

            When("앱이 계정 변화를 확인해 동기화를 요청한다") {
                Then("TC-DATA-SYNC-DOMAIN-036 동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase =
                RequestSyncUseCase(
                    getAccountUseCase = getAccountUseCase,
                    syncManager = syncManager,
                )

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
