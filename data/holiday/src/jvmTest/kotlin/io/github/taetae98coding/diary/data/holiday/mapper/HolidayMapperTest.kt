package io.github.taetae98coding.diary.data.holiday.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.model.holiday.Holiday
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

                remote.toLocal(year = year) shouldBe
                    HolidayLocalEntity(
                        year = year,
                        name = remote.name,
                        isHoliday = isHoliday,
                        start = remote.start,
                        endInclusive = remote.endInclusive,
                    )
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
