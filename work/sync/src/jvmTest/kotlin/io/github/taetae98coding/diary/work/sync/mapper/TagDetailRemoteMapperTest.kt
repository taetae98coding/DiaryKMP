package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagDetailRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TagDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            val local = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()

            local.toRemote() shouldBe
                TagDetailRemoteEntity(
                    emoji = local.emoji,
                    title = local.title,
                    description = local.description,
                    color = local.color,
                )
        }

        test("remote to local") {
            val remote = fixtureMonkey.giveMeOne<TagDetailRemoteEntity>()

            remote.toLocal() shouldBe
                TagDetailLocalEntity(
                    emoji = remote.emoji,
                    title = remote.title,
                    description = remote.description,
                    color = remote.color,
                )
        }

        test("local to remote to local") {
            val local = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()

            local.toRemote().toLocal() shouldBe local
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
