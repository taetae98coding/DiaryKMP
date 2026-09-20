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

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<MusicDetail>()

            domain.toLocal().toDomain() shouldBe domain
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
