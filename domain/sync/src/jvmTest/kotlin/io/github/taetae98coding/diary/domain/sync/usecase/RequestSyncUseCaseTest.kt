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

private fun requestSyncUseCase(
    getAccountUseCase: GetAccountUseCase,
    syncManager: SyncManager,
): RequestSyncUseCase =
    RequestSyncUseCase(
        getAccountUseCase = getAccountUseCase,
        syncManager = syncManager,
    )

class RequestSyncUseCaseTest :
    BehaviorSpec({
        Given("로그인 세션이 인증된 사용자 계정이 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("메모나 태그에 변경이 발생해 동기화를 요청한다") {
                Then("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c 데이터 변경 계기면 확인된 사용자 계정으로 진행을 보고하지 않는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = false) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("TC-221754fa-d814-4c80-9c41-c0bdce86574f 확인된 사용자 계정으로 진행을 보고하는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.USER_REQUESTED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                }
            }

            When("앱이 인증된 사용자 계정을 확인해 동기화를 요청한다") {
                Then("TC-f7816008-73c9-4079-a740-e4e695b0de94 확인된 사용자 계정으로 진행을 보고하는 동기화가 한 번 요청된다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = true) }
                }
            }

            When("진행 표시 중에 진행을 표시하지 않는 계기로 동기화를 요청한다") {
                Then("TC-904c3de1-9a96-4f74-9e76-74ebeaedfe00 메모나 태그 변경 계기는 진행 보고를 새로 요청하지 않고 동기화만 한 번 요청한다") {
                    val syncManager = mockk<SyncManager>(relaxed = true)
                    val useCase =
                        requestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 1) { syncManager.requestSync(reportsProgress = false) }
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = true) }
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
                Then("TC-34531fcc-91a8-4944-894e-d7d092fbaa0c 동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("TC-3b111d97-65d8-466f-9f19-c16776baaf75 로그인 정보가 유효하지 않으면 계정 데이터 서버 동기화가 요청되지 않아 진행 표시 대상도 없다") {
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

            When("메모, 태그, 장소, 웹 항목, 연락처, 곡, QR 또는 일곱 연결 종류에 변경이 발생해 동기화를 요청한다") {
                Then(
                    listOf(
                        "TC-aac365bb-b751-48aa-8d35-f88b442e15b1",
                        "TC-f942eea0-e00f-4e8f-a7a2-bcffc48794c7",
                        "TC-2d672161-9914-4650-ae19-2f77010ba903",
                        "TC-4a5232dc-0a4d-4973-a88c-bf54c2ec94dd",
                        "TC-2364d348-12f9-476a-8cf4-dc7f00ecdd29",
                        "TC-ca23453a-d461-4d5e-a800-98d0920e3fc3",
                        "TC-77972a44-8e5c-470b-8790-10bd7edbd458",
                        "TC-c6fc5a0c-6728-4100-9a24-f626b054d55b",
                        "TC-fbf49960-d768-4bea-bc33-740469a525b7",
                        "TC-94764581-fbce-4dbc-9cc0-a28ccf406f27",
                        "TC-448de895-ec55-4a6f-8a8a-e4ca87b5d5f0",
                    ).joinToString(separator = " ", postfix = " 동기화가 요청되지 않는다"),
                ) {
                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }

            When("체크리스트나 체크 항목에 변경이 발생해 동기화를 요청한다") {
                Then("TC-5766f1b0-3084-42a3-a460-5a387498f7d8 TC-3f8e323a-1b3f-4bc5-85a9-d54b08e6179a TC-20be2c49-4b48-41e2-861c-50dbec844576 동기화가 요청되지 않는다") {
                    val result = useCase(parameter = SyncTrigger.DATA_CHANGED)

                    result.shouldBeSuccess(Unit)
                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }

            When("사용자가 화면을 당겨 새로고침을 요청한다") {
                Then("TC-3b111d97-65d8-466f-9f19-c16776baaf75 TC-3133b1a9-6ea0-47a7-871f-623da1d0c0cf 계정 데이터 서버 동기화가 요청되지 않아 진행 표시 대상도 없다") {
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
                Then("TC-7cbb0698-dbb1-4da3-bbfb-f86c62111698 동기화가 요청되지 않는다") {
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
