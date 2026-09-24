package io.github.taetae98coding.diary.data.holiday.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class HolidayMapperTest :
    FunSpec({
        test("remote to local") {
            listOf(true, false).forEach { isHoliday ->
                val year = fixtureMonkey.giveMeOne<Int>()
                val remote = remoteHoliday(isHoliday = isHoliday)

                remote.toLocal(country = HolidayCountry.UNITED_STATES, year = year) shouldBe
                    HolidayLocalEntity(
                        country = HolidayCountryLocalEntity.UNITED_STATES,
                        year = year,
                        name = remote.name,
                        isHoliday = isHoliday,
                        start = remote.start,
                        endInclusive = remote.endInclusive,
                    )
            }
        }

        test("country to local and remote") {
            mapOf(
                HolidayCountry.KOREA to (HolidayCountryLocalEntity.KOREA to HolidayCountryRemoteEntity.KOREA),
                HolidayCountry.UNITED_STATES to (HolidayCountryLocalEntity.UNITED_STATES to HolidayCountryRemoteEntity.UNITED_STATES),
            ).forEach { (country, expected) ->
                country.toLocal() shouldBe expected.first
                country.toRemote() shouldBe expected.second
            }
        }

        test("local to domain") {
            listOf(true, false).forEach { isHoliday ->
                val local = localHoliday(isHoliday = isHoliday)

                local.toDomain() shouldBe
                    Holiday(
                        name = local.name,
                        isHoliday = isHoliday,
                        dateRange = local.start..local.endInclusive,
                    )
            }
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun remoteHoliday(isHoliday: Boolean): HolidayRemoteEntity {
    val start = date()

    return fixtureMonkey
        .giveMeKotlinBuilder<HolidayRemoteEntity>()
        .setExp(HolidayRemoteEntity::isHoliday, isHoliday)
        .setExp(HolidayRemoteEntity::start, start)
        .setExp(HolidayRemoteEntity::endInclusive, start.plus(1 + randomInt(bound = 28), DateTimeUnit.DAY))
        .sample()
}

private fun localHoliday(isHoliday: Boolean): HolidayLocalEntity {
    val start = date()

    return fixtureMonkey
        .giveMeKotlinBuilder<HolidayLocalEntity>()
        .setExp(HolidayLocalEntity::isHoliday, isHoliday)
        .setExp(HolidayLocalEntity::start, start)
        .setExp(HolidayLocalEntity::endInclusive, start.plus(1 + randomInt(bound = 28), DateTimeUnit.DAY))
        .sample()
}

private fun date(): LocalDate =
    LocalDate(
        year = 2000 + randomInt(bound = 100),
        month = 1 + randomInt(bound = 12),
        day = 1 + randomInt(bound = 28),
    )

private fun randomInt(bound: Int): Int = (fixtureMonkey.giveMeOne<Int>().toUInt() % bound.toUInt()).toInt()
