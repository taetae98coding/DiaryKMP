package io.github.taetae98coding.diary.data.playlist.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MusicDetailMapperTest :
    FunSpec({
        test("TC-9e545317-6c86-45ac-9268-965e60c49464 domain to local") {
            val domain = fixtureMonkey.giveMeOne<MusicDetail>()

            domain.toLocal() shouldBe
                MusicDetailLocalEntity(
                    link = domain.link,
                    title = domain.title,
                    artist = domain.artist,
                    thumbnail = "",
                )
        }

        test("TC-771d2727-6089-43b8-a3c4-2b6862717fbc local to domain 은 저장소에 남은 썸네일 주소를 곡의 내용에 넣지 않는다") {
            val local = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>().copy(thumbnail = "https://i.ytimg.com/vi/${fixtureMonkey.giveMeOne<String>()}/hqdefault.jpg")

            local.toDomain() shouldBe
                MusicDetail(
                    title = local.title,
                    artist = local.artist,
                    link = local.link,
                )
        }

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<MusicDetail>()

            domain.toLocal().toDomain() shouldBe domain
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
