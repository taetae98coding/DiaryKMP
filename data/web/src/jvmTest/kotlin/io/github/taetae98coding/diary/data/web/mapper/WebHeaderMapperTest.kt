package io.github.taetae98coding.diary.data.web.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WebHeaderMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<WebHeader>()

            domain.toLocal() shouldBe
                WebHeaderLocalEntity(
                    name = domain.name,
                    value = domain.value,
                )
        }

        test("domain to remote") {
            val domain = fixtureMonkey.giveMeOne<WebHeader>()

            domain.toRemote() shouldBe
                WebPageHeaderRemoteEntity(
                    name = domain.name,
                    value = domain.value,
                )
        }

        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<WebHeaderLocalEntity>()

            local.toDomain() shouldBe
                WebHeader(
                    name = local.name,
                    value = local.value,
                )
        }

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<WebHeader>()

            domain.toLocal().toDomain() shouldBe domain
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
