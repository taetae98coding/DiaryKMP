package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MusicDetailRemoteMapperTest :
    FunSpec({
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

        test("local to remote to local") {
            val local = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>()

            local.toRemote().toLocal() shouldBe local
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
