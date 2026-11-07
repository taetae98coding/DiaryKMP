package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.testing.web.webHeader
import io.github.taetae98coding.diary.core.testing.web.webHeaderLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebHeaderMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = Arb.webHeader().next()

            domain.toLocal() shouldBe
                WebHeaderLocalEntity(
                    name = domain.name,
                    value = domain.value,
                )
        }

        test("local to domain") {
            val local = Arb.webHeaderLocalEntity().next()

            local.toDomain() shouldBe
                WebHeader(
                    name = local.name,
                    value = local.value,
                )
        }

        test("domain to local to domain") {
            val domain = Arb.webHeader().next()

            domain.toLocal().toDomain() shouldBe domain
        }
    })
