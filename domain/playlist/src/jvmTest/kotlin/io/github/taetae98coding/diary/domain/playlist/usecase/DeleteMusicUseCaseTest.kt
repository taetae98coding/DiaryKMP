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

class DeleteMusicUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 곡이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val musicId = Uuid.random()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDeleted(
                    account = account,
                    musicId = musicId,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = instant()
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    now = now,
                )

            When("곡을 삭제한다") {
                Then("TC-MUSIC-DETAIL-DOMAIN-007 삭제 여부와 삭제 시점만 반영한다") {
                    useCase(parameter = musicId).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                    coVerify(exactly = 0) {
                        accountMusicRepository.updateDetail(account = any(), musicId = any(), detail = any(), updatedAt = any())
                    }
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-MUSIC-DETAIL-DATA-007 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = musicId).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("대상 곡이 없도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDeleted(account = account, musicId = any(), isDeleted = any(), updatedAt = any())
            } returns 0
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                )

            When("곡을 삭제한다") {
                Then("TC-MUSIC-DETAIL-DATA-004 아무것도 바꾸지 않는다") {
                    useCase(parameter = Uuid.random()).shouldBeSuccess(0)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                )

            When("곡을 삭제한다") {
                Then("계정 조회 실패를 전달하고 삭제를 저장하지 않는다") {
                    useCase(parameter = Uuid.random())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountMusicRepository.updateDeleted(account = any(), musicId = any(), isDeleted = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("삭제 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDeleted(account = account, musicId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                )

            When("곡을 삭제한다") {
                Then("TC-MUSIC-DETAIL-DATA-006 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = Uuid.random())
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

        private fun useCase(
            getAccountUseCase: GetAccountUseCase,
            requestSyncUseCase: RequestSyncUseCase,
            accountMusicRepository: AccountMusicRepository,
            now: Instant? = null,
        ): DeleteMusicUseCase {
            val clock =
                if (now == null) {
                    Clock.System
                } else {
                    mockk<Clock>().also { clock -> every { clock.now() } returns now }
                }

            return DeleteMusicUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                accountMusicRepository = accountMusicRepository,
                clock = clock,
            )
        }

        private fun getAccountUseCase(account: Account): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(Result.success(account))

            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
