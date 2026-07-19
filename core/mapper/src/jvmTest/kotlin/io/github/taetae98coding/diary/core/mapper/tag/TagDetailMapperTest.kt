package io.github.taetae98coding.diary.core.mapper.tag

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagDetailRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TagDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<TagDetail>()

            domain.toLocal() shouldBe
                TagDetailLocalEntity(
                    emoji = domain.emoji,
                    title = domain.title,
                    description = domain.description,
                    color = domain.color,
                )
        }

        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()

            local.toDomain() shouldBe
                TagDetail(
                    emoji = local.emoji,
                    title = local.title,
                    description = local.description,
                    color = local.color,
                )
        }

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

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<TagDetail>()

            domain.toLocal().toDomain() shouldBe domain
        }

        test("local to remote to local") {
            val local = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()

            local.toRemote().toLocal() shouldBe local
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
