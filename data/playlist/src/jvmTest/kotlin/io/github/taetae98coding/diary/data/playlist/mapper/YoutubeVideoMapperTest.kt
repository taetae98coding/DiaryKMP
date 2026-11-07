package io.github.taetae98coding.diary.data.playlist.mapper

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtube.network.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.data.playlist.youtubeVideoRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class YoutubeVideoMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = Arb.youtubeVideoRemoteEntity().next()

            remote.toDomain() shouldBe
                YoutubeVideo(
                    title = remote.title,
                    channelName = remote.authorName,
                )
        }
    })
