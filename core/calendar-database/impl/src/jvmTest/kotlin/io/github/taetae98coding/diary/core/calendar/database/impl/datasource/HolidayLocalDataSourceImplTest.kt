package io.github.taetae98coding.diary.core.calendar.database.impl.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.transaction.HolidayTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayLocalDataSourceImplTest :
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

        test("TC-HOLIDAY-DATABASE-DATA-001 저장된 공휴일이 없으면 빈 목록을 반환한다") {
            dataSource.get(countrySet = setOf(KOREA), year = YEAR).first().shouldBeEmpty()
        }

        test("TC-HOLIDAY-DATABASE-DATA-002 요청한 연도의 공휴일만 반환한다") {
            val holiday = holiday()
            val otherHoliday = holiday(year = OTHER_YEAR)
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday))
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(otherHoliday))

            dataSource.get(countrySet = setOf(KOREA), year = YEAR).first() shouldBe listOf(holiday)
        }

        test("TC-HOLIDAY-DATABASE-DATA-003 조회 중인 연도의 캐시가 변경되면 새 목록을 제공한다") {
            val holiday = holiday()

            dataSource.get(countrySet = setOf(KOREA), year = YEAR).test {
                awaitItem().shouldBeEmpty()

                transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday))

                awaitItem() shouldBe listOf(holiday)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-HOLIDAY-DATABASE-DATA-004 공휴일 목록을 정해진 순서로 반환한다") {
            val laterStart = holiday(name = "다", start = LocalDate(2026, 2, 1), endInclusive = LocalDate(2026, 2, 1))
            val laterEnd = holiday(name = "라", start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 2))
            val laterName = holiday(name = "나", start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 1))
            val first = holiday(name = "가", start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 1))
            transaction.upsert(
                country = KOREA,
                year = YEAR,
                holidayList = listOf(laterStart, laterEnd, laterName, first),
            )

            dataSource.get(countrySet = setOf(KOREA), year = YEAR).first() shouldBe listOf(first, laterName, laterEnd, laterStart)
        }

        test("TC-HOLIDAY-DATABASE-DATA-008 전체 조회는 모든 연도의 공휴일을 반환한다") {
            val holiday = holiday(name = "가")
            val otherHoliday = holiday(year = OTHER_YEAR, name = "나")
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday))
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(otherHoliday))

            dataSource.get(countrySet = setOf(KOREA)).first() shouldBe listOf(holiday, otherHoliday)
        }

        test("TC-HOLIDAY-DATABASE-DATA-009 전체 조회에서 저장된 공휴일이 없으면 빈 목록을 반환한다") {
            dataSource.get(countrySet = setOf(KOREA)).first().shouldBeEmpty()
        }

        test("TC-HOLIDAY-DATABASE-DATA-010 어느 연도의 캐시든 바뀌면 전체 조회 결과를 갱신한다") {
            val holiday = holiday(name = "가")
            val otherHoliday = holiday(year = OTHER_YEAR, name = "나")
            val replacementHoliday = holiday(name = "가")
            val otherReplacementHoliday = holiday(year = OTHER_YEAR, name = "나")
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday))
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(otherHoliday))

            dataSource.get(countrySet = setOf(KOREA)).test {
                awaitItem() shouldBe listOf(holiday, otherHoliday)

                transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(replacementHoliday))

                awaitItem() shouldBe listOf(replacementHoliday, otherHoliday)

                transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(otherReplacementHoliday))

                awaitItem() shouldBe listOf(replacementHoliday, otherReplacementHoliday)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-HOLIDAY-DATABASE-DATA-012 전체 조회는 이름 오름차순으로 공휴일을 반환한다") {
            val firstByName =
                holiday(
                    year = OTHER_YEAR,
                    name = "가",
                    start = LocalDate(2027, 12, 31),
                    endInclusive = LocalDate(2028, 1, 2),
                )
            val secondByName =
                holiday(
                    name = "나",
                    start = LocalDate(2026, 12, 31),
                    endInclusive = LocalDate(2027, 1, 2),
                )
            val thirdByName =
                holiday(
                    name = "다",
                    start = LocalDate(2026, 1, 2),
                    endInclusive = LocalDate(2026, 1, 3),
                )
            val lastByName =
                holiday(
                    name = "라",
                    start = LocalDate(2026, 1, 2),
                    endInclusive = LocalDate(2026, 1, 2),
                )
            transaction.upsert(
                country = KOREA,
                year = YEAR,
                holidayList = listOf(lastByName, thirdByName, secondByName),
            )
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(firstByName))

            dataSource.get(countrySet = setOf(KOREA)).first() shouldBe listOf(firstByName, secondByName, thirdByName, lastByName)
        }

        test("TC-HOLIDAY-DATABASE-DATA-013 연도별 조회는 요청한 국가 집합의 공휴일만 합쳐 반환한다") {
            val korea = holiday(country = KOREA, name = "가")
            val unitedStates = holiday(country = UNITED_STATES, name = "나")
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(korea))
            transaction.upsert(country = UNITED_STATES, year = YEAR, holidayList = listOf(unitedStates))

            val caseMap =
                mapOf(
                    setOf(KOREA) to listOf(korea),
                    setOf(UNITED_STATES) to listOf(unitedStates),
                    setOf(KOREA, UNITED_STATES) to listOf(korea, unitedStates),
                    emptySet<HolidayCountryLocalEntity>() to emptyList(),
                )

            caseMap.forEach { (countrySet, expected) ->
                dataSource.get(countrySet = countrySet, year = YEAR).first() shouldBe expected
            }
        }

        test("TC-HOLIDAY-DATABASE-DATA-014 전체 조회는 요청한 국가 집합의 공휴일만 반환한다") {
            val korea = holiday(country = KOREA, name = "가")
            val otherYearKorea = holiday(country = KOREA, year = OTHER_YEAR, name = "나")
            val unitedStates = holiday(country = UNITED_STATES, name = "다")
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(korea))
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(otherYearKorea))
            transaction.upsert(country = UNITED_STATES, year = YEAR, holidayList = listOf(unitedStates))

            dataSource.get(countrySet = setOf(KOREA)).first() shouldBe listOf(korea, otherYearKorea)
        }

        test("TC-HOLIDAY-DATABASE-DATA-016 시작일·종료일·이름이 같으면 국가 순으로 반환한다") {
            val name: String = fixtureMonkey.giveMeOne()
            val korea = holiday(country = KOREA, name = name)
            val unitedStates = holiday(country = UNITED_STATES, name = name)
            transaction.upsert(country = UNITED_STATES, year = YEAR, holidayList = listOf(unitedStates))
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(korea))

            dataSource.get(countrySet = setOf(KOREA, UNITED_STATES), year = YEAR).first() shouldBe listOf(korea, unitedStates)
        }

        test("TC-HOLIDAY-DATABASE-DATA-017 전체 조회에서 이름이 같으면 한국, 미국 순으로 반환한다") {
            val name: String = fixtureMonkey.giveMeOne()
            val unitedStates = holiday(country = UNITED_STATES, year = YEAR, name = name, start = LocalDate(2026, 1, 1))
            val korea = holiday(country = KOREA, year = OTHER_YEAR, name = name, start = LocalDate(2027, 12, 31))
            transaction.upsert(country = UNITED_STATES, year = YEAR, holidayList = listOf(unitedStates))
            transaction.upsert(country = KOREA, year = OTHER_YEAR, holidayList = listOf(korea))

            dataSource.get(countrySet = setOf(KOREA, UNITED_STATES)).first() shouldBe listOf(korea, unitedStates)
        }

        test("TC-HOLIDAY-DATABASE-DATA-018 전체 조회에서 국가 집합이 비어 있으면 빈 목록을 반환한다") {
            transaction.upsert(country = KOREA, year = YEAR, holidayList = listOf(holiday()))

            dataSource.get(countrySet = emptySet()).first().shouldBeEmpty()
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
