package io.github.taetae98coding.diary.domain.playlist.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.repository.YoutubeVideoRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchYoutubeVideoUseCaseTest :
    BehaviorSpec({
        Given("영상 정보 조회가 성공하도록 준비되어 있다") {
            When("YouTube 영상 링크로 불러온다") {
                Then("TC-MUSIC-ADD-DATA-008 입력한 링크로 조회하고 영상 제목과 채널 이름을 전달한다") {
                    val video = fixtureMonkey.giveMeOne<YoutubeVideo>()
                    val repository = successRepository(video = video)
                    val useCase = FetchYoutubeVideoUseCase(youtubeVideoRepository = repository)

                    useCase(parameter = YOUTUBE_LINK).shouldBeSuccess() shouldBe video

                    coVerify(exactly = 1) { repository.fetch(link = YOUTUBE_LINK) }
                }

                Then("TC-MUSIC-ADD-DOMAIN-010 링크의 앞뒤 공백을 없앤 값으로 조회한다") {
                    val repository = successRepository(video = fixtureMonkey.giveMeOne<YoutubeVideo>())
                    val useCase = FetchYoutubeVideoUseCase(youtubeVideoRepository = repository)

                    useCase(parameter = "  $YOUTUBE_LINK  ").shouldBeSuccess()

                    coVerify(exactly = 1) { repository.fetch(link = YOUTUBE_LINK) }
                }
            }
        }

        Given("영상 정보 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException("fetch-${fixtureMonkey.giveMeOne<String>()}")
            val repository = mockk<YoutubeVideoRepository>()
            coEvery { repository.fetch(link = any()) } throws throwable
            val useCase = FetchYoutubeVideoUseCase(youtubeVideoRepository = repository)

            When("YouTube 영상 링크로 불러온다") {
                Then("TC-MUSIC-ADD-DATA-009 조회 실패를 그대로 전달한다") {
                    useCase(parameter = YOUTUBE_LINK).shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }

        Given("불러오기에 쓸 수 없는 링크가 입력되어 있다") {
            val repository = mockk<YoutubeVideoRepository>(relaxed = true)
            val useCase = FetchYoutubeVideoUseCase(youtubeVideoRepository = repository)

            When("불러오기를 실행한다") {
                listOf("", "   ").forEach { blankLink ->
                    Then("TC-MUSIC-ADD-FEATURE-019 링크가 비어 있으면 조회하지 않고 링크 공백을 알린다: '$blankLink'") {
                        val result = useCase(parameter = blankLink)

                        result.shouldBeFailure().shouldBeInstanceOf<MusicLinkBlankException>()
                        coVerify(exactly = 0) { repository.fetch(link = any()) }
                    }
                }

                listOf(
                    "https://vimeo.com/76979871",
                    "https://youtube.com.attacker.example/watch?v=dQw4w9WgXcQ",
                    "www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "곡 링크",
                ).forEach { link ->
                    Then("TC-MUSIC-ADD-FEATURE-019 YouTube 주소가 아니면 조회하지 않고 링크 형식을 알린다: $link") {
                        val result = useCase(parameter = link)

                        result.shouldBeFailure().shouldBeInstanceOf<MusicLinkNotYoutubeException>()
                        coVerify(exactly = 0) { repository.fetch(link = any()) }
                    }
                }
            }
        }

        Given("여러 형태의 YouTube 주소가 입력되어 있다") {
            When("불러오기를 실행한다") {
                listOf(
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://youtu.be/dQw4w9WgXcQ",
                    "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://music.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://WWW.YouTube.COM/watch?v=dQw4w9WgXcQ",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLabc&t=42s",
                    "https://www.youtube.com/shorts/dQw4w9WgXcQ",
                    "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
                ).forEach { link ->
                    Then("TC-MUSIC-ADD-DOMAIN-011 YouTube 주소로 영상 정보를 조회한다: $link") {
                        val video = fixtureMonkey.giveMeOne<YoutubeVideo>()
                        val repository = successRepository(video = video)
                        val useCase = FetchYoutubeVideoUseCase(youtubeVideoRepository = repository)

                        useCase(parameter = link).shouldBeSuccess() shouldBe video

                        coVerify(exactly = 1) { repository.fetch(link = link) }
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun successRepository(video: YoutubeVideo): YoutubeVideoRepository {
            val repository = mockk<YoutubeVideoRepository>()
            coEvery { repository.fetch(link = any()) } returns video
            return repository
        }
    }
}
