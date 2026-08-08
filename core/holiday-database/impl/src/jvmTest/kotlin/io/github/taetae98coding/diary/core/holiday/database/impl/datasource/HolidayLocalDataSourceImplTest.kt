package io.github.taetae98coding.diary.core.holiday.database.impl.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.holiday.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.database.impl.HolidayDatabase
import io.github.taetae98coding.diary.core.holiday.database.impl.transaction.HolidayTransactionImpl
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
        lateinit var database: HolidayDatabase
        lateinit var dataSource: HolidayLocalDataSource
        lateinit var transaction: HolidayTransaction

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<HolidayDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = HolidayLocalDataSourceImpl(database = database)
            transaction = HolidayTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-HOLIDAY-DATABASE-DATA-001 저장된 공휴일이 없으면 빈 목록을 반환한다") {
            dataSource.get(year = YEAR).first().shouldBeEmpty()
        }

        test("TC-HOLIDAY-DATABASE-DATA-002 요청한 연도의 공휴일만 반환한다") {
            val holiday = holiday()
            val otherHoliday = holiday(year = OTHER_YEAR)
            transaction.upsert(year = YEAR, holidayList = listOf(holiday))
            transaction.upsert(year = OTHER_YEAR, holidayList = listOf(otherHoliday))

            dataSource.get(year = YEAR).first() shouldBe listOf(holiday)
        }

        test("TC-HOLIDAY-DATABASE-DATA-003 조회 중인 연도의 캐시가 변경되면 새 목록을 제공한다") {
            val holiday = holiday()

            dataSource.get(year = YEAR).test {
                awaitItem().shouldBeEmpty()

                transaction.upsert(year = YEAR, holidayList = listOf(holiday))

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
                year = YEAR,
                holidayList = listOf(laterStart, laterEnd, laterName, first),
            )

            dataSource.get(year = YEAR).first() shouldBe listOf(first, laterName, laterEnd, laterStart)
        }

        test("TC-HOLIDAY-DATABASE-DATA-008 전체 조회는 모든 연도의 공휴일을 반환한다") {
            val holiday = holiday(name = "가")
            val otherHoliday = holiday(year = OTHER_YEAR, name = "나")
            transaction.upsert(year = YEAR, holidayList = listOf(holiday))
            transaction.upsert(year = OTHER_YEAR, holidayList = listOf(otherHoliday))

            dataSource.get().first() shouldBe listOf(holiday, otherHoliday)
        }

        test("TC-HOLIDAY-DATABASE-DATA-009 전체 조회에서 저장된 공휴일이 없으면 빈 목록을 반환한다") {
            dataSource.get().first().shouldBeEmpty()
        }

        test("TC-HOLIDAY-DATABASE-DATA-010 어느 연도의 캐시든 바뀌면 전체 조회 결과를 갱신한다") {
            val holiday = holiday(name = "가")
            val otherHoliday = holiday(year = OTHER_YEAR, name = "나")
            val replacementHoliday = holiday(name = "가")
            val otherReplacementHoliday = holiday(year = OTHER_YEAR, name = "나")
            transaction.upsert(year = YEAR, holidayList = listOf(holiday))
            transaction.upsert(year = OTHER_YEAR, holidayList = listOf(otherHoliday))

            dataSource.get().test {
                awaitItem() shouldBe listOf(holiday, otherHoliday)

                transaction.upsert(year = YEAR, holidayList = listOf(replacementHoliday))

                awaitItem() shouldBe listOf(replacementHoliday, otherHoliday)

                transaction.upsert(year = OTHER_YEAR, holidayList = listOf(otherReplacementHoliday))

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
                year = YEAR,
                holidayList = listOf(lastByName, thirdByName, secondByName),
            )
            transaction.upsert(year = OTHER_YEAR, holidayList = listOf(firstByName))

            dataSource.get().first() shouldBe listOf(firstByName, secondByName, thirdByName, lastByName)
        }
    }) {
    public companion object {
        private const val YEAR = 2026
        private const val OTHER_YEAR = 2027

        private fun holiday(
            year: Int = YEAR,
            name: String = fixtureMonkey.giveMeOne(),
            isHoliday: Boolean = fixtureMonkey.giveMeOne(),
            start: LocalDate = LocalDate(2026, 1, 1),
            endInclusive: LocalDate = start,
        ): HolidayLocalEntity =
            HolidayLocalEntity(
                year = year,
                name = name,
                isHoliday = isHoliday,
                start = start,
                endInclusive = endInclusive,
            )
    }
}
