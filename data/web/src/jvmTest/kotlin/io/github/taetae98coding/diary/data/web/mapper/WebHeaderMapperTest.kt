package io.github.taetae98coding.diary.data.web.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WebHeaderMapperTest :
    FunSpec({
        test("domain to remote") {
            val domain = fixtureMonkey.giveMeOne<WebHeader>()

            domain.toRemote() shouldBe
                WebPageHeaderRemoteEntity(
                    name = domain.name,
                    value = domain.value,
                )
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
