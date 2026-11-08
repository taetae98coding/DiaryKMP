package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

class AccountDDaySyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var syncTransaction: AccountDDaySyncTransactionImpl
        lateinit var dataSource: AccountDDayLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            syncTransaction = AccountDDaySyncTransactionImpl(database = database)
            dataSource = AccountDDayLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-1940bbd8-f6e9-4cd9-8845-d0c028854f0e 내려받은 디데이의 반복 간격 목록이 기기의 목록을 대체한다") {
            listOf(
                setOf(DateTimeUnit.MonthBased(months = 12), DateTimeUnit.DayBased(days = 1000)),
                emptySet(),
            ).forEach { pulledRepeats ->
                val accountId = Arb.kotlinUuid().next()
                val stored = Arb.localDDay(baseDate = LocalDate(2026, 1, 1), repeats = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12))).next()
                AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = stored, ddayTagList = emptyList())
                val pulled = stored.copy(detail = stored.detail.copy(repeats = pulledRepeats), updatedAt = stored.updatedAt + 1.seconds)

                syncTransaction.upsert(accountId = accountId, ddayList = listOf(pulled), cursor = 1L)

                dataSource
                    .find(accountId = accountId, ddayId = stored.id)
                    .first()
                    .shouldNotBeNull()
                    .detail.repeats shouldBe pulledRepeats
            }
        }

        test("TC-d3c9e994-62b9-432e-8218-9b2570678f20 종료일이 비어 있는 디데이를 내려받으면 기기의 디데이는 종료일이 없어진다") {
            val accountId = Arb.kotlinUuid().next()
            val stored = Arb.localDDay(baseDate = LocalDate(2026, 3, 1), endDate = LocalDate(2027, 3, 1)).next()
            AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = stored, ddayTagList = emptyList())
            val pulled = stored.copy(detail = stored.detail.copy(endDate = null), updatedAt = stored.updatedAt + 1.seconds)

            syncTransaction.upsert(accountId = accountId, ddayList = listOf(pulled), cursor = 1L)

            dataSource
                .find(accountId = accountId, ddayId = stored.id)
                .first()
                .shouldNotBeNull()
                .detail.endDate
                .shouldBeNull()
        }

        // 추가 테스트: 내려받은 디데이 전체가 저장 형식을 거쳐도 그대로 되돌아온다.
        test("내려받은 디데이는 반복 간격과 날짜를 포함해 그대로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val pulled = Arb.localDDay(isDeleted = false).next()

            syncTransaction.upsert(accountId = accountId, ddayList = listOf(pulled), cursor = 1L)

            dataSource.find(accountId = accountId, ddayId = pulled.id).first() shouldBe pulled
        }
    })
