package io.github.taetae98coding.diary.data.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.testing.web.localWebDetail
import io.github.taetae98coding.diary.core.testing.web.remoteWebDetail
import io.github.taetae98coding.diary.core.testing.web.webHeaderNameCaseList
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WebDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            webHeaderNameCaseList.forEach { headerNameList ->
                val local = fixtureMonkey.localWebDetail(headerNameList = headerNameList)

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
                val remote = fixtureMonkey.remoteWebDetail(headerNameList = headerNameList)

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
                val local = fixtureMonkey.localWebDetail(headerNameList = headerNameList)

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
