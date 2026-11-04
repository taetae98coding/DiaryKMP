package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.testing.web.localWebDetail
import io.github.taetae98coding.diary.core.testing.web.remoteWebDetail
import io.github.taetae98coding.diary.core.testing.web.webHeaderNameCaseList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val local = Arb.localWebDetail(headerNameList = headerNameList).next()

                local.toRemote() shouldBe
                    WebDetailRemoteEntity(
                        title = local.title,
                        description = local.description,
                        url = local.url,
                        headerList = local.headerList.map { header -> header.toRemote() },
                    )
            }
        }

        test("remote to local") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val remote = Arb.remoteWebDetail(headerNameList = headerNameList).next()

                remote.toLocal() shouldBe
                    WebDetailLocalEntity(
                        title = remote.title,
                        description = remote.description,
                        url = remote.url,
                        headerList = remote.headerList.map { header -> header.toLocal() },
                    )
            }
        }

        test("local to remote to local") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val local = Arb.localWebDetail(headerNameList = headerNameList).next()

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
