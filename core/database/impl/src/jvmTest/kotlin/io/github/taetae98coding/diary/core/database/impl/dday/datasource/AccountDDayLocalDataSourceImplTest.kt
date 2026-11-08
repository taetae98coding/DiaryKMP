package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import java.util.TimeZone
import kotlin.uuid.Uuid

class AccountDDayLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountDDayLocalDataSourceImpl
        lateinit var transaction: AccountDDayTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountDDayLocalDataSourceImpl(database = database)
            transaction = AccountDDayTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insert(
            accountId: Uuid,
            vararg ddayList: DDayLocalEntity,
        ) {
            ddayList.forEach { dday -> transaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList()) }
        }

        suspend fun pagedTitleList(accountId: Uuid): List<String> =
            dataSource
                .page(accountId = accountId)
                .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
                .data
                .map { dday -> dday.detail.title }

        // 카드 문구가 `D-Day`, `D-400(1년 1개월)`, `25일`이 되는 값을 오늘 2026년 1월 1일 기준으로 고른다.
        fun cardVariedDDayList(): List<DDayLocalEntity> =
            listOf(
                Arb.localDDay(baseDate = LocalDate(2026, 1, 1), title = "다").next(),
                Arb.localDDay(baseDate = LocalDate(2027, 2, 5), repeats = setOf(DateTimeUnit.MonthBased(months = 12)), title = "가").next(),
                Arb.localDDay(baseDate = LocalDate(2025, 12, 8), countsBaseDay = true, repeats = setOf(DateTimeUnit.DayBased(days = 100)), title = "나").next(),
            )

        test("TC-1de4da40-9a26-4cc5-86aa-344144a4ec60 목록은 제목 오름차순이고 카드 문구, 기준일과 기념일은 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            insert(accountId, *cardVariedDDayList().toTypedArray())

            pagedTitleList(accountId = accountId) shouldBe listOf("가", "나", "다")
        }

        test("TC-717b035d-b955-4e16-81af-df80e79e05b1 오늘이 바뀌어도 목록 순서는 그대로다") {
            // 목록 조회는 오늘을 입력으로 받지 않으므로 오늘 2026년 1월 1일과 2027년 6월 30일의 조회는 같은 조회를 두 번 한 것과 같다.
            val accountId = Arb.kotlinUuid().next()
            insert(accountId, *cardVariedDDayList().toTypedArray())

            pagedTitleList(accountId = accountId) shouldBe listOf("가", "나", "다")
            pagedTitleList(accountId = accountId) shouldBe listOf("가", "나", "다")
        }

        test("TC-fde858aa-0164-4fd4-8fec-c09ed3f490dd 기준일과 종료일은 기기의 시간대와 관계없이 같은 연·월·일이다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = Arb.localDDay(baseDate = LocalDate(2026, 3, 1), endDate = LocalDate(2026, 12, 31)).next()
            val defaultTimeZone = TimeZone.getDefault()
            try {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
                insert(accountId, dday)

                // Etc/GMT-14는 협정 세계시보다 14시간 빠르고 Etc/GMT+12는 12시간 느리다.
                listOf("Etc/GMT-14", "UTC", "Etc/GMT+12").forEach { timeZoneId ->
                    TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))

                    val found = dataSource.find(accountId = accountId, ddayId = dday.id).first().shouldNotBeNull()

                    found.detail.baseDate shouldBe LocalDate(2026, 3, 1)
                    found.detail.endDate shouldBe LocalDate(2026, 12, 31)
                }
            } finally {
                TimeZone.setDefault(defaultTimeZone)
            }
        }
    })
