package io.github.taetae98coding.diary.domain.playlist.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class FindMusicDownloadTargetUseCaseTest :
    BehaviorSpec({
        Given("영상 ID를 얻을 수 있는 곡과 얻을 수 없는 곡이 함께 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val withVideo = music(link = "https://youtu.be/dQw4w9WgXcQ")
            val withoutLink = music(link = "")
            val withoutVideo = music(link = "https://www.youtube.com/feed/subscriptions")
            val useCase =
                useCase(
                    account = account,
                    musicList = listOf(withVideo, withoutLink, withoutVideo),
                )

            When("다운로드 대상을 조회한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-001 TC-MUSIC-DOWNLOAD-FEATURE-007 영상 ID를 얻을 수 있는 곡만 대상이 된다") {
                    useCase(parameter = ListSort.TITLE).shouldBeSuccess() shouldBe
                        listOf(MusicDownloadTarget(id = withVideo.id, videoId = "dQw4w9WgXcQ"))
                }
            }
        }

        Given("영상 ID를 얻을 수 있는 곡이 목록 순서대로 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val first = music(link = "https://youtu.be/aaaaaaaaaaa")
            val second = music(link = "https://www.youtube.com/watch?v=bbbbbbbbbbb")
            val useCase = useCase(account = account, musicList = listOf(first, second))

            When("다운로드 대상을 조회한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-002 목록 순서를 그대로 유지한다") {
                    useCase(parameter = ListSort.TITLE).shouldBeSuccess() shouldBe
                        listOf(
                            MusicDownloadTarget(id = first.id, videoId = "aaaaaaaaaaa"),
                            MusicDownloadTarget(id = second.id, videoId = "bbbbbbbbbbb"),
                        )
                }
            }
        }

        Given("노출되는 곡이 하나도 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val useCase = useCase(account = account, musicList = emptyList())

            When("다운로드 대상을 조회한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-007 대상이 하나도 없다") {
                    useCase(parameter = ListSort.TITLE).shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val useCase =
                FindMusicDownloadTargetUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMusicRepository = mockk(relaxed = true),
                )

            When("다운로드 대상을 조회한다") {
                Then("계정 조회 실패를 그대로 전달한다") {
                    useCase(parameter = ListSort.TITLE)
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun useCase(
            account: Account,
            musicList: List<Music>,
        ): FindMusicDownloadTargetUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery { accountMusicRepository.findList(account = account, sort = ListSort.TITLE) } returns musicList

            return FindMusicDownloadTargetUseCase(
                getAccountUseCase = getAccountUseCase,
                accountMusicRepository = accountMusicRepository,
            )
        }

        private fun music(link: String): Music =
            fixtureMonkey
                .giveMeKotlinBuilder<Music>()
                .setExp(Music::detail, MusicDetail(title = "제목", artist = "가수", link = link))
                .setExp(Music::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Music::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
