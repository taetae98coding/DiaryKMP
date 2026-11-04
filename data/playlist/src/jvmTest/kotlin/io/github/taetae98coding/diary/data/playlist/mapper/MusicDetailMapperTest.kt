package io.github.taetae98coding.diary.data.playlist.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.testing.music.musicDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.playlist.musicDetail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class MusicDetailMapperTest :
    FunSpec({
        test("TC-9e545317-6c86-45ac-9268-965e60c49464 domain to local") {
            val domain = Arb.musicDetail().next()

            domain.toLocal() shouldBe
                MusicDetailLocalEntity(
                    link = domain.link,
                    title = domain.title,
                    artist = domain.artist,
                    thumbnail = "",
                )
        }

        test("TC-771d2727-6089-43b8-a3c4-2b6862717fbc local to domain 은 저장소에 남은 썸네일 주소를 곡의 내용에 넣지 않는다") {
            val local = Arb.musicDetailLocalEntity().next().copy(thumbnail = "https://i.ytimg.com/vi/${Arb.string().next()}/hqdefault.jpg")

            local.toDomain() shouldBe
                MusicDetail(
                    title = local.title,
                    artist = local.artist,
                    link = local.link,
                )
        }

        test("domain to local to domain") {
            val domain = Arb.musicDetail().next()

            domain.toLocal().toDomain() shouldBe domain
        }
    })
