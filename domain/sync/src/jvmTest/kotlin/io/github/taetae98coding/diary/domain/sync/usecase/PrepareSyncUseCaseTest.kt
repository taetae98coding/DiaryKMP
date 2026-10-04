package io.github.taetae98coding.diary.domain.sync.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncTimeRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class PrepareSyncUseCaseTest :
    BehaviorSpec({
        listOf(
            Triple("기록이 없다", null, false),
            Triple("29일 전이다", 29.days, false),
            Triple("30일 전이다", 30.days, true),
            Triple("31일 전이다", 31.days, true),
        ).forEach { (label, elapsed, isReset) ->
            Given("인증된 사용자 계정의 마지막 동기화 시각이 $label") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val now = fixtureMonkey.giveMeOne<Instant>()
                val clock = mockk<Clock>()
                every { clock.now() } returns now

                When("동기화를 시작하기 전에 판단한다") {
                    Then("TC-DATA-SYNC-DOMAIN-076 $label 강제 전체 재동기화 여부가 $isReset 이다") {
                        val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
                        coEvery { accountSyncTimeRepository.read(account = account) } returns elapsed?.let { now - it }
                        val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                        val useCase =
                            PrepareSyncUseCase(
                                accountSyncTimeRepository = accountSyncTimeRepository,
                                accountSyncDataRepository = accountSyncDataRepository,
                                clock = clock,
                            )

                        useCase(parameter = account).shouldBeSuccess(Unit)

                        coVerify(exactly = if (isReset) 1 else 0) { accountSyncDataRepository.delete(account = account) }
                        coVerify(exactly = if (isReset) 1 else 0) {
                            accountSyncTimeRepository.upsert(account = account, syncedAt = now)
                        }
                    }
                }
            }
        }

        Given("한 기기에 마지막 동기화 시각이 29일 전인 계정과 31일 전인 계정이 기록되어 있다") {
            val recentAccount = fixtureMonkey.giveMeOne<Account.User>()
            val staleAccount = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
            coEvery { accountSyncTimeRepository.read(account = recentAccount) } returns now - 29.days
            coEvery { accountSyncTimeRepository.read(account = staleAccount) } returns now - 31.days

            When("두 계정의 동기화를 차례로 시작한다") {
                Then("TC-DATA-SYNC-DOMAIN-077 31일 전인 계정만 기기 데이터를 지운다") {
                    val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                    val useCase =
                        PrepareSyncUseCase(
                            accountSyncTimeRepository = accountSyncTimeRepository,
                            accountSyncDataRepository = accountSyncDataRepository,
                            clock = clock,
                        )

                    useCase(parameter = recentAccount).shouldBeSuccess(Unit)
                    useCase(parameter = staleAccount).shouldBeSuccess(Unit)

                    coVerify(exactly = 0) { accountSyncDataRepository.delete(account = recentAccount) }
                    coVerify(exactly = 1) { accountSyncDataRepository.delete(account = staleAccount) }
                }
            }
        }

        Given("마지막 동기화 시각이 31일 전인 계정이 강제 전체 재동기화로 기기 데이터를 한 번 지웠다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val firstTriggeredAt = fixtureMonkey.giveMeOne<Instant>()

            When("30일이 지나기 전에 같은 계정의 동기화를 다시 시작한다") {
                Then("TC-DATA-SYNC-DATA-038 기기 데이터를 다시 지우지 않는다") {
                    val storedSyncedAt = mutableMapOf<Account, Instant>(account to firstTriggeredAt - 31.days)
                    val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>()
                    coEvery { accountSyncTimeRepository.read(account = any()) } answers { storedSyncedAt[firstArg<Account>()] }
                    coEvery { accountSyncTimeRepository.upsert(account = any(), syncedAt = any()) } answers {
                        storedSyncedAt[firstArg<Account>()] = secondArg<Instant>()
                    }
                    val accountSyncDataRepository = mockk<AccountSyncDataRepository>(relaxed = true)
                    val clock = mockk<Clock>()
                    every { clock.now() } returnsMany listOf(firstTriggeredAt, firstTriggeredAt + 29.days)
                    val useCase =
                        PrepareSyncUseCase(
                            accountSyncTimeRepository = accountSyncTimeRepository,
                            accountSyncDataRepository = accountSyncDataRepository,
                            clock = clock,
                        )

                    useCase(parameter = account).shouldBeSuccess(Unit)
                    useCase(parameter = account).shouldBeSuccess(Unit)

                    coVerify(exactly = 1) { accountSyncDataRepository.delete(account = account) }
                    coVerify(exactly = 1) { accountSyncTimeRepository.upsert(account = account, syncedAt = firstTriggeredAt) }
                }
            }
        }

        Given("마지막 동기화 시각이 31일 전인 계정의 기기 데이터 삭제가 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())

            When("동기화를 시작하기 전에 판단한다") {
                Then("실패를 전달하고 마지막 동기화 시각을 갱신하지 않는다") {
                    val clock = mockk<Clock>()
                    every { clock.now() } returns now
                    val accountSyncTimeRepository = mockk<AccountSyncTimeRepository>(relaxed = true)
                    coEvery { accountSyncTimeRepository.read(account = account) } returns now - 31.days
                    val accountSyncDataRepository = mockk<AccountSyncDataRepository>()
                    coEvery { accountSyncDataRepository.delete(account = account) } throws throwable
                    val useCase =
                        PrepareSyncUseCase(
                            accountSyncTimeRepository = accountSyncTimeRepository,
                            accountSyncDataRepository = accountSyncDataRepository,
                            clock = clock,
                        )

                    useCase(parameter = account).shouldBeFailure() shouldBeSameInstanceAs throwable

                    coVerify(exactly = 0) { accountSyncTimeRepository.upsert(account = any(), syncedAt = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
