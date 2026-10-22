package io.github.taetae98coding.diary.core.calendar.database.impl.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import io.github.taetae98coding.diary.core.calendar.database.impl.datasource.LunarLocalDataSourceImpl
import io.github.taetae98coding.diary.core.calendar.database.impl.datasource.lunarDateList
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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

        test("TC-3e9afe01-23d6-4f46-8445-bf9a9a75408e 저장 시 해당 연도의 기존 목록 전체를 교체하고 다른 연도는 유지한다") {
            val previousList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 3))
            val replacementList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 2), endInclusive = LocalDate(2026, 1, 2))
            val otherYearList = lunarDateList(solarYear = 2027, start = LocalDate(2027, 1, 1), endInclusive = LocalDate(2027, 1, 2))
            transaction.upsert(solarYear = 2026, lunarDateList = previousList)
            transaction.upsert(solarYear = 2027, lunarDateList = otherYearList)

            transaction.upsert(solarYear = 2026, lunarDateList = replacementList)

            dataSource.get(dateRange = LocalDate(2026, 1, 1)..LocalDate(2027, 12, 31)).first() shouldBe replacementList + otherYearList
        }

        test("TC-61b8aa37-c3e9-44ec-937a-02664d252225 교체하는 동안 일부만 바뀐 음력 자료가 보이지 않는다") {
            val dateRange = LocalDate(2026, 1, 1)..LocalDate(2026, 12, 31)
            val previousList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 3))
            val replacementList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 2), endInclusive = LocalDate(2026, 1, 5))
            transaction.upsert(solarYear = 2026, lunarDateList = previousList)

            dataSource.get(dateRange = dateRange).test {
                awaitItem() shouldBe previousList

                transaction.upsert(solarYear = 2026, lunarDateList = replacementList)

                awaitItem() shouldBe replacementList
                expectNoEvents()
            }
        }

        test("TC-77a8f0ae-7aa7-4553-a40a-d70cca1e0f3d 빈 목록 저장 시 해당 연도의 기존 목록을 제거한다") {
            transaction.upsert(solarYear = 2026, lunarDateList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 3)))

            transaction.upsert(solarYear = 2026, lunarDateList = emptyList())

            dataSource.get(dateRange = LocalDate(2026, 1, 1)..LocalDate(2026, 12, 31)).first().shouldBeEmpty()
        }

        test("TC-d42d9dd5-3937-4d4e-b617-04c56aa5c6e2 교체가 끝까지 완료되지 못하면 그 연도의 기존 음력 자료를 유지한다") {
            val dateRange = LocalDate(2026, 1, 1)..LocalDate(2026, 12, 31)
            val previousList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 1), endInclusive = LocalDate(2026, 1, 3))
            val replacementList = lunarDateList(solarYear = 2026, start = LocalDate(2026, 1, 2), endInclusive = LocalDate(2026, 1, 5))
            transaction.upsert(solarYear = 2026, lunarDateList = previousList)
            val throwable = IllegalStateException(fixtureMonkey.nonBlankString())
            val failingDatabase = spyk(database)
            // 기존 자료를 지운 뒤 새 자료를 저장하려는 순간 쓰기가 실패하도록 한다.
            every { failingDatabase.lunarDateDao() } returns database.lunarDateDao() andThenThrows throwable
            val failingTransaction = LunarTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(solarYear = 2026, lunarDateList = replacementList)
            }.message shouldBe throwable.message

            dataSource.get(dateRange = dateRange).first() shouldBe previousList
        }
    })
