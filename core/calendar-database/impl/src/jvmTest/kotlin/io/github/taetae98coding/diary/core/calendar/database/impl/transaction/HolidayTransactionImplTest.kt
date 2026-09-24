package io.github.taetae98coding.diary.core.calendar.database.impl.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.datasource.HolidayLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayTransactionImplTest :
    FunSpec({
        lateinit var database: CalendarDatabase
        lateinit var dataSource: HolidayLocalDataSource
        lateinit var transaction: HolidayTransaction

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<CalendarDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = HolidayLocalDataSourceImpl(database = database)
            transaction = HolidayTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-HOLIDAY-DATABASE-DATA-005 저장 시 해당 연도의 기존 목록 전체를 교체한다") {
            val previousHolidayList = listOf(holiday(), holiday())
            val replacementHolidayList = listOf(holiday())
            transaction.upsert(country = KOREA, year = YEAR, holidayList = previousHolidayList)

            transaction.upsert(country = KOREA, year = YEAR, holidayList = replacementHolidayList)

            dataSource.get(countrySet = setOf(KOREA), year = YEAR).first() shouldBe replacementHolidayList
        }

        test("TC-HOLIDAY-DATABASE-DATA-006 빈 목록 저장 시 해당 연도의 기존 목록을 제거한다") {
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday()))

            transaction.upsert(country = KOREA, year = YEAR, holidayList = emptyList())

            dataSource.get(countrySet = setOf(KOREA), year = YEAR).first().shouldBeEmpty()
        }

        test("TC-HOLIDAY-DATABASE-DATA-007 한 연도를 교체해도 다른 연도의 목록은 유지한다") {
            val otherHolidayList = listOf(holiday(year = OTHER_YEAR))
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday()))
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = otherHolidayList)

            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday()))

            dataSource.get(countrySet = setOf(KOREA), year = OTHER_YEAR).first() shouldBe otherHolidayList
        }
        test("TC-HOLIDAY-DATABASE-DATA-015 한 국가·연도를 교체해도 같은 연도의 다른 국가 목록은 유지한다") {
            val unitedStatesHolidayList = listOf(holiday(country = UNITED_STATES))
            val replacementHolidayList = listOf(holiday(country = KOREA))
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday(country = KOREA)))
            transaction.upsert(country = UNITED_STATES, year = YEAR, holidayList = unitedStatesHolidayList)

            transaction.upsert(country = KOREA, year = YEAR, holidayList = replacementHolidayList)

            dataSource.get(countrySet = setOf(KOREA), year = YEAR).first() shouldBe replacementHolidayList
            dataSource.get(countrySet = setOf(UNITED_STATES), year = YEAR).first() shouldBe unitedStatesHolidayList
        }

        test("한 국가에 빈 목록을 저장해도 같은 연도의 다른 국가 목록은 유지한다") {
            val unitedStatesHolidayList = listOf(holiday(country = UNITED_STATES))
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday(country = KOREA)))
            transaction.upsert(country = UNITED_STATES, year = YEAR, holidayList = unitedStatesHolidayList)

            transaction.upsert(country = KOREA, year = YEAR, holidayList = emptyList())

            dataSource.get(countrySet = setOf(KOREA, UNITED_STATES), year = YEAR).first() shouldBe unitedStatesHolidayList
        }
    }) {
    public companion object {
        private const val YEAR = 2026
        private const val OTHER_YEAR = 2027

        private val KOREA = HolidayCountryLocalEntity.KOREA
        private val UNITED_STATES = HolidayCountryLocalEntity.UNITED_STATES

        private fun holiday(
            country: HolidayCountryLocalEntity = KOREA,
            year: Int = YEAR,
            name: String = fixtureMonkey.giveMeOne(),
            isHoliday: Boolean = fixtureMonkey.giveMeOne(),
            start: LocalDate = LocalDate(2026, 1, 1),
            endInclusive: LocalDate = start,
        ): HolidayLocalEntity =
            HolidayLocalEntity(
                country = country,
                year = year,
                name = name,
                isHoliday = isHoliday,
                start = start,
                endInclusive = endInclusive,
            )
    }
}
