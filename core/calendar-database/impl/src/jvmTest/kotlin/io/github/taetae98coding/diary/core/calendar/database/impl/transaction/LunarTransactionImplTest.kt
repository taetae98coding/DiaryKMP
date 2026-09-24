package io.github.taetae98coding.diary.core.calendar.database.impl.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.datasource.LunarLocalDataSourceImpl
import io.github.taetae98coding.diary.core.calendar.database.impl.datasource.lunarDateList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class LunarTransactionImplTest :
    FunSpec({
        lateinit var database: CalendarDatabase
        lateinit var dataSource: LunarLocalDataSource
        lateinit var transaction: LunarTransaction

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<CalendarDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = LunarLocalDataSourceImpl(database = database)
            transaction = LunarTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-LUNAR-FETCH-DATA-001 저장 시 해당 연도의 기존 목록 전체를 교체하고 다른 연도는 유지한다") {
            val previousList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 3))
            val replacementList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 2), endInclusive = LocalDate(2026, 1, 2))
            val otherYearList = lunarDateList(solarYear = 2027, start = LocalDate(2027, 1, 1), endInclusive = LocalDate(2027, 1, 2))
            transaction.upsert(solarYear = 2026, lunarDateList = previousList)
            transaction.upsert(solarYear = 2027, lunarDateList = otherYearList)

            transaction.upsert(solarYear = 2026, lunarDateList = replacementList)

            dataSource.get(dateRange = LocalDate(2026, 1, 1)..LocalDate(2027, 12, 31)).first() shouldBe replacementList + otherYearList
        }

        test("TC-LUNAR-FETCH-DATA-002 빈 목록 저장 시 해당 연도의 기존 목록을 제거한다") {
            transaction.upsert(solarYear = 2026, lunarDateList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 3)))

            transaction.upsert(solarYear = 2026, lunarDateList = emptyList())

            dataSource.get(dateRange = LocalDate(2026, 1, 1)..LocalDate(2026, 12, 31)).first().shouldBeEmpty()
        }
    })
