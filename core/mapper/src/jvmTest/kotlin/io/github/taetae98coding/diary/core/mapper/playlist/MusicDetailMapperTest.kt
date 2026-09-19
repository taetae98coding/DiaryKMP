package io.github.taetae98coding.diary.core.mapper.playlist

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MusicDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<MusicDetail>()

            domain.toLocal() shouldBe
                MusicDetailLocalEntity(
                    link = domain.link,
                    title = domain.title,
                    artist = domain.artist,
                    thumbnail = domain.thumbnail,
                )
        }

        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>()

            local.toDomain() shouldBe
                MusicDetail(
                    link = local.link,
                    title = local.title,
                    artist = local.artist,
                    thumbnail = local.thumbnail,
                )
        }

        test("local to remote") {
            val local = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>()

            local.toRemote() shouldBe
                MusicDetailRemoteEntity(
                    link = local.link,
                    title = local.title,
                    artist = local.artist,
                    thumbnail = local.thumbnail,
                )
        }

        test("remote to local") {
            val remote = fixtureMonkey.giveMeOne<MusicDetailRemoteEntity>()

            remote.toLocal() shouldBe
                MusicDetailLocalEntity(
                    link = remote.link,
                    title = remote.title,
                    artist = remote.artist,
                    thumbnail = remote.thumbnail,
                )
        }

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<MusicDetail>()

            domain.toLocal().toDomain() shouldBe domain
        }

        test("local to remote to local") {
            val local = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>()

            local.toRemote().toLocal() shouldBe local
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
