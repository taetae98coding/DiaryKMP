package io.github.taetae98coding.diary.data.playlist.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtubenetwork.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.core.youtubenetwork.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class YoutubeVideoRepositoryImplTest :
    FunSpec({
        test("TC-MUSIC-ADD-DATA-008 입력한 링크로 영상 정보를 조회해 제목과 채널 이름과 썸네일을 전달한다") {
            val remote = fixtureMonkey.giveMeOne<YoutubeVideoRemoteEntity>()
            val remoteDataSource = mockk<YoutubeVideoRemoteDataSource>()
            coEvery { remoteDataSource.fetch(link = YOUTUBE_LINK) } returns remote
            val repository = YoutubeVideoRepositoryImpl(youtubeVideoRemoteDataSource = remoteDataSource)

            val actual = repository.fetch(link = YOUTUBE_LINK)

            actual shouldBe
                YoutubeVideo(
                    title = remote.title,
                    channelName = remote.authorName,
                    thumbnail = remote.thumbnailUrl,
                )
            coVerify(exactly = 1) { remoteDataSource.fetch(link = YOUTUBE_LINK) }
        }

        test("TC-MUSIC-ADD-DATA-009 조회 실패를 그대로 전파한다") {
            val throwable = IllegalStateException("fetch-${fixtureMonkey.giveMeOne<String>()}")
            val remoteDataSource = mockk<YoutubeVideoRemoteDataSource>()
            coEvery { remoteDataSource.fetch(link = any()) } throws throwable
            val repository = YoutubeVideoRepositoryImpl(youtubeVideoRemoteDataSource = remoteDataSource)

            val actual = shouldThrow<IllegalStateException> { repository.fetch(link = YOUTUBE_LINK) }

            actual shouldBeSameInstanceAs throwable
        }
    })
