package io.github.taetae98coding.diary.core.calendar.database.impl.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.transaction.LunarTransactionImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class LunarLocalDataSourceImplTest :
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

        test("저장된 음력 날짜가 없으면 빈 목록을 반환한다") {
            dataSource.get(dateRange = LocalDate(2026, 12, 30)..LocalDate(2027, 1, 2)).first().shouldBeEmpty()
        }

        test("TC-LUNAR-FETCH-DATA-011 기간에 드는 양력 날짜의 음력 날짜만 양력 날짜 순으로 제공한다") {
            val yearEndList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 12, 29), endInclusive = LocalDate(2026, 12, 31))
            val yearStartList = lunarDateList(solarYear = 2027, start = LocalDate(2027, 1, 1), endInclusive = LocalDate(2027, 1, 3))
            transaction.upsert(solarYear = 2027, lunarDateList = yearStartList.reversed())
            transaction.upsert(solarYear = 2026, lunarDateList = yearEndList.reversed())

            dataSource.get(dateRange = LocalDate(2026, 12, 30)..LocalDate(2027, 1, 2)).first() shouldBe
                (yearEndList + yearStartList).filter { lunarDate -> lunarDate.solar in LocalDate(2026, 12, 30)..LocalDate(2027, 1, 2) }
        }

        test("TC-LUNAR-FETCH-DATA-012 캐시되지 않은 연도의 날짜는 결과에 들지 않는다") {
            val yearEndList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 12, 30), endInclusive = LocalDate(2026, 12, 31))
            transaction.upsert(solarYear = 2026, lunarDateList = yearEndList)

            dataSource.get(dateRange = LocalDate(2026, 12, 30)..LocalDate(2027, 1, 2)).first() shouldBe yearEndList
        }

        test("TC-LUNAR-FETCH-DATA-013 조회 중인 기간의 캐시가 교체되면 새 목록을 제공한다") {
            val lunarDateList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 8, 17), endInclusive = LocalDate(2026, 8, 23))

            dataSource.get(dateRange = LocalDate(2026, 8, 17)..LocalDate(2026, 8, 23)).test {
                awaitItem().shouldBeEmpty()

                transaction.upsert(solarYear = 2026, lunarDateList = lunarDateList)

                awaitItem() shouldBe lunarDateList
                cancelAndIgnoreRemainingEvents()
            }
        }
    })

internal fun lunarDateList(
    solarYear: Int,
    start: LocalDate,
    endInclusive: LocalDate,
): List<LunarDateLocalEntity> =
    (start..endInclusive).mapIndexed { index, solar ->
        LunarDateLocalEntity(
            solar = solar,
            solarYear = solarYear,
            lunarYear = solarYear - 1,
            lunarMonth = 11,
            lunarDay = 1 + index,
            isLeapMonth = false,
        )
    }
