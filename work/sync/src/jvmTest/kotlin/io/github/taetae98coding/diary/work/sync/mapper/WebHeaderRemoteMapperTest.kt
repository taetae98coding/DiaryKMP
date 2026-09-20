package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WebHeaderRemoteMapperTest :
    FunSpec({
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

        test("local to remote to local") {
            val local = fixtureMonkey.giveMeOne<WebHeaderLocalEntity>()

            local.toRemote().toLocal() shouldBe local
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
