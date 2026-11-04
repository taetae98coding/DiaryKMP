package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.testing.web.webHeader
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageHeaderRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebHeaderMapperTest :
    FunSpec({
        test("domain to remote") {
            val domain = Arb.webHeader().next()

            domain.toRemote() shouldBe
                WebPageHeaderRemoteEntity(
                    name = domain.name,
                    value = domain.value,
                )
        }
    })
