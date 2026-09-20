package io.github.taetae98coding.diary.data.tag.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagDetail
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

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<TagDetail>()

            domain.toLocal().toDomain() shouldBe domain
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
