package io.github.taetae98coding.diary.data.web.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.core.model.browser.BrowserCookieSameSite
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class BrowserCookieMapperTest :
    FunSpec({
        test("local to domain to local") {
            repeat(times = 5) {
                val local = fixtureMonkey.giveMeOne<BrowserCookieLocalEntity>()

                local.toDomain().toLocal() shouldBe local
            }
        }

        test("domain to local to domain") {
            repeat(times = 5) {
                val domain = fixtureMonkey.giveMeOne<BrowserCookie>()

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("same site local to domain") {
            val expected =
                mapOf(
                    BrowserCookieSameSiteLocalEntity.UNSPECIFIED to BrowserCookieSameSite.UNSPECIFIED,
                    BrowserCookieSameSiteLocalEntity.NONE to BrowserCookieSameSite.NONE,
                    BrowserCookieSameSiteLocalEntity.LAX to BrowserCookieSameSite.LAX,
                    BrowserCookieSameSiteLocalEntity.STRICT to BrowserCookieSameSite.STRICT,
                )

            BrowserCookieSameSiteLocalEntity.entries.forEach { local ->
                local.toDomain() shouldBe expected.getValue(local)
            }
        }

        test("same site domain to local to domain") {
            BrowserCookieSameSite.entries.forEach { domain ->
                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
