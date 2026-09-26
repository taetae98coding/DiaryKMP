package io.github.taetae98coding.diary.domain.playlist.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RestoreMusicUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 삭제된 곡이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDeleted(
                    account = account,
                    musicId = id,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                RestoreMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    clock = clock,
                )

            When("곡의 삭제를 실행 취소한다") {
                Then("TC-PLAYLIST-HOME-DOMAIN-006 삭제 여부를 미삭제로 되돌리고 수정 시각을 갱신한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe false
                    updatedAtSlot.captured shouldBe now
                    coVerify(exactly = 0) {
                        accountMusicRepository.updateDetail(account = any(), musicId = any(), detail = any(), updatedAt = any())
                    }
                }

                Then("TC-PLAYLIST-HOME-DATA-004 TC-SYNC-REFRESH-FEATURE-004 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.Guest>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDeleted(account = account, musicId = id, isDeleted = any(), updatedAt = any())
            } returns 1
            val useCase =
                RestoreMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("곡의 삭제를 실행 취소한다") {
                Then("TC-PLAYLIST-HOME-DATA-005 게스트 계정 기준으로 기기에만 실행 취소를 반영한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(exactly = 1) {
                        accountMusicRepository.updateDeleted(account = account, musicId = id, isDeleted = false, updatedAt = any())
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                RestoreMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("곡의 삭제를 실행 취소한다") {
                Then("TC-PLAYLIST-HOME-DOMAIN-007 실행 취소가 성공으로 다뤄지지 않고 곡의 삭제 여부를 바꾸지 않는다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountMusicRepository.updateDeleted(account = any(), musicId = any(), isDeleted = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("실행 취소 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDeleted(account = account, musicId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                RestoreMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("곡의 삭제를 실행 취소한다") {
                Then("TC-PLAYLIST-HOME-DATA-006 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }
    }
}
