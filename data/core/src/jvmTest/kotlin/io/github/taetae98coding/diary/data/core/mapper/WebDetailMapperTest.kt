package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.testing.web.localWebDetail
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.core.testing.web.webHeaderNameCaseList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebDetailMapperTest :
    FunSpec({
        test("domain to local") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val domain = Arb.webDetail(headerNameList = headerNameList).next()

                domain.toLocal() shouldBe
                    WebDetailLocalEntity(
                        title = domain.title,
                        description = domain.description,
                        url = domain.url,
                        headerList = domain.headerList.map { header -> header.toLocal() },
                    )
            }
        }

        test("local to domain") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val local = Arb.localWebDetail(headerNameList = headerNameList).next()

                local.toDomain() shouldBe
                    WebDetail(
                        title = local.title,
                        description = local.description,
                        url = local.url,
                        headerList = local.headerList.map { header -> header.toDomain() },
                    )
            }
        }

        test("domain to local to domain") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val domain = Arb.webDetail(headerNameList = headerNameList).next()

                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
