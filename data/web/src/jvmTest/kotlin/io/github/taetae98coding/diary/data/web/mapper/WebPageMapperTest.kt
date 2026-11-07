package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageRemoteEntity
import io.github.taetae98coding.diary.data.web.webPageRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebPageMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = Arb.webPageRemoteEntity().next()

            remote.toDomain() shouldBe
                WebPage(
                    baseUrl = remote.url,
                    body = remote.body,
                )
        }
    })
