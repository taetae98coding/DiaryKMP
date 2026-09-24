package io.github.taetae98coding.diary.data.setting.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.browsercookie.api.entity.ChromeProfileLocalEntity
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ChromeProfileMapperTest :
    FunSpec({
        test("local to domain") {
            repeat(times = 5) {
                val local = fixtureMonkey.giveMeOne<ChromeProfileLocalEntity>()

                local.toDomain() shouldBe ChromeProfile(directory = local.directory, name = local.name)
            }
        }
    })
