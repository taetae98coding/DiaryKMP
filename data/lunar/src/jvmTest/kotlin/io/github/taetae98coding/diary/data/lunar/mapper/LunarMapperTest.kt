package io.github.taetae98coding.diary.data.lunar.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LunarMapperTest :
    FunSpec({
        test("remote to local") {
            val solarYear = fixtureMonkey.giveMeOne<Int>()
            val remote =
                LunarDateRemoteEntity(
                    solar = LocalDate(2026, 2, 17),
                    year = fixtureMonkey.giveMeOne(),
                    month = fixtureMonkey.giveMeOne(),
                    day = fixtureMonkey.giveMeOne(),
                    isLeapMonth = fixtureMonkey.giveMeOne(),
                )

            remote.toLocal(solarYear = solarYear) shouldBe
                LunarDateLocalEntity(
                    solar = remote.solar,
                    solarYear = solarYear,
                    lunarYear = remote.year,
                    lunarMonth = remote.month,
                    lunarDay = remote.day,
                    isLeapMonth = remote.isLeapMonth,
                )
        }

        test("local to domain") {
            val local =
                LunarDateLocalEntity(
                    solar = LocalDate(2026, 2, 17),
                    solarYear = fixtureMonkey.giveMeOne(),
                    lunarYear = fixtureMonkey.giveMeOne(),
                    lunarMonth = fixtureMonkey.giveMeOne(),
                    lunarDay = fixtureMonkey.giveMeOne(),
                    isLeapMonth = fixtureMonkey.giveMeOne(),
                )

            local.toDomain() shouldBe
                LunarDate(
                    solar = local.solar,
                    year = local.lunarYear,
                    month = local.lunarMonth,
                    day = local.lunarDay,
                    isLeapMonth = local.isLeapMonth,
                )
        }
    })
