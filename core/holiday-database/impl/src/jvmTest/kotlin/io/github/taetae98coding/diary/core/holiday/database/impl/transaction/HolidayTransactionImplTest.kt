package io.github.taetae98coding.diary.core.holiday.database.impl.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.holiday.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.database.impl.HolidayDatabase
import io.github.taetae98coding.diary.core.holiday.database.impl.datasource.HolidayLocalDataSourceImpl
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

        test("TC-HOLIDAY-DATABASE-DATA-005 저장 시 해당 연도의 기존 목록 전체를 교체한다") {
            val previousHolidayList = listOf(holiday(), holiday())
            val replacementHolidayList = listOf(holiday())
            transaction.upsert(year = YEAR, holidayList = previousHolidayList)

            transaction.upsert(year = YEAR, holidayList = replacementHolidayList)

            dataSource.get(year = YEAR).first() shouldBe replacementHolidayList
        }

        test("TC-HOLIDAY-DATABASE-DATA-006 빈 목록 저장 시 해당 연도의 기존 목록을 제거한다") {
            transaction.upsert(year = YEAR, holidayList = listOf(holiday()))

            transaction.upsert(year = YEAR, holidayList = emptyList())

            dataSource.get(year = YEAR).first().shouldBeEmpty()
        }

        test("TC-HOLIDAY-DATABASE-DATA-007 한 연도를 교체해도 다른 연도의 목록은 유지한다") {
            val otherHolidayList = listOf(holiday(year = OTHER_YEAR))
            transaction.upsert(year = YEAR, holidayList = listOf(holiday()))
            transaction.upsert(year = OTHER_YEAR, holidayList = otherHolidayList)

            transaction.upsert(year = YEAR, holidayList = listOf(holiday()))

            dataSource.get(year = OTHER_YEAR).first() shouldBe otherHolidayList
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
