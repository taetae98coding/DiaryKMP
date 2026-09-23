package io.github.taetae98coding.diary.domain.playlist.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindMusicUseCaseTest :
    BehaviorSpec({
        listOf(false, true).forEach { isDeleted ->
            Given("로그인한 계정과 삭제 여부가 $isDeleted 인 곡이 저장되어 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val music = music(isDeleted = isDeleted)
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                val accountMusicRepository = mockk<AccountMusicRepository>()
                every { accountMusicRepository.find(account = account, musicId = music.id) } returns flowOf(music)
                val useCase =
                    FindMusicUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountMusicRepository = accountMusicRepository,
                    )

                When("대상 식별자로 곡을 조회한다") {
                    Then("TC-MUSIC-DETAIL-DOMAIN-001 삭제 여부와 관계없이 현재 계정의 곡을 전달한다") {
                        useCase(parameter = music.id).first().shouldBeSuccess(music)
                    }
                }
            }
        }

        Given("현재 계정과 연결되지 않은 식별자가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val musicId = Uuid.random()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            every { accountMusicRepository.find(account = account, musicId = musicId) } returns flowOf(null)
            val useCase =
                FindMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMusicRepository = accountMusicRepository,
                )

            When("대상 식별자로 곡을 조회한다") {
                Then("TC-MUSIC-DETAIL-DATA-001 조회되는 곡이 없다") {
                    useCase(parameter = musicId).first().shouldBeSuccess(null)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                FindMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMusicRepository = accountMusicRepository,
                )

            When("대상 식별자로 곡을 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = Uuid.random())
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

        private fun music(isDeleted: Boolean): Music =
            fixtureMonkey
                .giveMeKotlinBuilder<Music>()
                .setExp(Music::isDeleted, isDeleted)
                .setExp(Music::updatedAt, instant())
                .setExp(Music::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
