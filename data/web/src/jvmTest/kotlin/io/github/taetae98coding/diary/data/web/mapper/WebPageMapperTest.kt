package io.github.taetae98coding.diary.data.web.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WebPageMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = fixtureMonkey.giveMeOne<WebPageRemoteEntity>()

            remote.toDomain() shouldBe
                WebPage(
                    baseUrl = remote.url,
                    body = remote.body,
                )
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
