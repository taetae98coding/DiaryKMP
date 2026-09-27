package io.github.taetae98coding.diary.domain.playlist.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class PageMusicUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 계정의 곡이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val musicList = List(2) { music() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            every { accountMusicRepository.page(account = account, sort = ListSort.TITLE) } returns flowOf(PagingData.from(musicList))
            val useCase =
                PageMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMusicRepository = accountMusicRepository,
                )

            When("곡 목록을 페이지로 조회한다") {
                Then("TC-PLAYLIST-HOME-DOMAIN-001 TC-PLAYLIST-HOME-DATA-001 현재 계정의 곡을 페이지로 전달한다") {
                    val pagingData = useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe musicList
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                PageMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMusicRepository = accountMusicRepository,
                )

            When("곡 목록을 페이지로 조회한다") {
                Then("TC-PLAYLIST-HOME-DOMAIN-002 노출할 곡을 전달하지 않고 곡 목록을 조회하지 않는다") {
                    useCase(parameter = ListSort.TITLE)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    verify(exactly = 0) { accountMusicRepository.page(account = any(), sort = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun music(): Music =
            fixtureMonkey
                .giveMeKotlinBuilder<Music>()
                .setExp(Music::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Music::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
