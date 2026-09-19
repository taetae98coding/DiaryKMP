package io.github.taetae98coding.diary.core.mapper.playlist

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtubenetwork.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class YoutubeVideoMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = fixtureMonkey.giveMeOne<YoutubeVideoRemoteEntity>()

            remote.toDomain() shouldBe
                YoutubeVideo(
                    title = remote.title,
                    channelName = remote.authorName,
                    thumbnail = remote.thumbnailUrl,
                )
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
