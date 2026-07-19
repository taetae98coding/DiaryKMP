package io.github.taetae98coding.diary.core.mapper.web

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
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

        test("local to remote") {
            val local = fixtureMonkey.giveMeOne<WebHeaderLocalEntity>()

            local.toRemote() shouldBe
                WebHeaderRemoteEntity(
                    name = local.name,
                    value = local.value,
                )
        }

        test("remote to local") {
            val remote = fixtureMonkey.giveMeOne<WebHeaderRemoteEntity>()

            remote.toLocal() shouldBe
                WebHeaderLocalEntity(
                    name = remote.name,
                    value = remote.value,
                )
        }

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<WebHeader>()

            domain.toLocal().toDomain() shouldBe domain
        }

        test("local to remote to local") {
            val local = fixtureMonkey.giveMeOne<WebHeaderLocalEntity>()

            local.toRemote().toLocal() shouldBe local
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
