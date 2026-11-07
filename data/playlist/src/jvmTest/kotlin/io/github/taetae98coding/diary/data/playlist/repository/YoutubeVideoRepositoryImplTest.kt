package io.github.taetae98coding.diary.data.playlist.repository

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtube.network.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.core.youtube.network.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.data.playlist.youtubeVideoRemoteEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

class YoutubeVideoRepositoryImplTest :
    FunSpec({
        test("TC-a4483895-1bff-43b1-98af-3787cc0ca408 입력한 링크로 영상 정보를 조회해 제목과 채널 이름을 전달한다") {
            val remote = Arb.youtubeVideoRemoteEntity().next()
            val remoteDataSource = mockk<YoutubeVideoRemoteDataSource>()
            coEvery { remoteDataSource.read(link = YOUTUBE_LINK) } returns remote
            val repository = YoutubeVideoRepositoryImpl(youtubeVideoRemoteDataSource = remoteDataSource)

            val actual = repository.fetch(link = YOUTUBE_LINK)

            actual shouldBe
                YoutubeVideo(
                    title = remote.title,
                    channelName = remote.authorName,
                )
            coVerify(exactly = 1) { remoteDataSource.read(link = YOUTUBE_LINK) }
        }

        test("TC-c5c2a1a7-9368-4c94-90a5-8f64decd79c8 조회 실패를 그대로 전파한다") {
            val throwable = IllegalStateException("fetch-${Arb.string().next()}")
            val remoteDataSource = mockk<YoutubeVideoRemoteDataSource>()
            coEvery { remoteDataSource.read(link = any()) } throws throwable
            val repository = YoutubeVideoRepositoryImpl(youtubeVideoRemoteDataSource = remoteDataSource)

            val actual = shouldThrow<IllegalStateException> { repository.fetch(link = YOUTUBE_LINK) }

            actual shouldBeSameInstanceAs throwable
        }
    })
