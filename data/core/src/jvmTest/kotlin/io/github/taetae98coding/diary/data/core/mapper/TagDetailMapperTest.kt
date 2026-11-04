package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.core.testing.tag.tagDetailLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class TagDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = Arb.tagDetail().next()

            domain.toLocal() shouldBe
                TagDetailLocalEntity(
                    emoji = domain.emoji,
                    title = domain.title,
                    description = domain.description,
                    color = domain.color,
                )
        }

        test("local to domain") {
            val local = Arb.tagDetailLocalEntity().next()

            local.toDomain() shouldBe
                TagDetail(
                    emoji = local.emoji,
                    title = local.title,
                    description = local.description,
                    color = local.color,
                )
        }

        test("domain to local to domain") {
            val domain = Arb.tagDetail().next()

            domain.toLocal().toDomain() shouldBe domain
        }
    })
