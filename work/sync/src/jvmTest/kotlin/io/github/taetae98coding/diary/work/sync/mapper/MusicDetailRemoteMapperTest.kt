package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity
import io.github.taetae98coding.diary.core.testing.music.musicDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicDetailRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class MusicDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            val local = Arb.musicDetailLocalEntity().next()

            local.toRemote() shouldBe
                MusicDetailRemoteEntity(
                    link = local.link,
                    title = local.title,
                    artist = local.artist,
                    thumbnail = local.thumbnail,
                )
        }

        test("remote to local") {
            val remote = Arb.musicDetailRemoteEntity().next()

            remote.toLocal() shouldBe
                MusicDetailLocalEntity(
                    link = remote.link,
                    title = remote.title,
                    artist = remote.artist,
                    thumbnail = remote.thumbnail,
                )
        }

        test("local to remote to local") {
            val local = Arb.musicDetailLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }
    })
