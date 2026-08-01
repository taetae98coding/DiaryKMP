package io.github.taetae98coding.diary.data.memo.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountCalendarMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.mapper.memo.toDomain
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime

class AccountCalendarMemoRepositoryImplTest :
    FunSpec({
        test("TC-CALENDAR-MEMO-DOMAIN-007 메모에 기록된 기간 형태를 그대로 유지한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val allDayMemo =
                calendarMemo().copy(
                    isAllDay = true,
                    start = LocalDateTime(2026, 7, 6, 0, 0),
                    endInclusive = LocalDateTime(2026, 7, 7, 0, 0),
                )
            val dateTimeMemo =
                calendarMemo().copy(
                    isAllDay = false,
                    start = LocalDateTime(2026, 7, 8, 13, 30),
                    endInclusive = LocalDateTime(2026, 7, 9, 9, 0),
                )
            val localDataSource = mockk<AccountCalendarMemoLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(listOf(allDayMemo, dateTimeMemo))
            val repository = AccountCalendarMemoRepositoryImpl(accountCalendarMemoLocalDataSource = localDataSource)

            val calendarMemoList = repository.get(account = account, dateRange = dateRange).first()

            calendarMemoList[0].dateTime shouldBe
                MemoDateTime.AllDay(dateRange = LocalDate(2026, 7, 6)..LocalDate(2026, 7, 7))
            calendarMemoList[1].dateTime shouldBe
                MemoDateTime.DateTime(
                    start = LocalDateTime(2026, 7, 8, 13, 30),
                    endInclusive = LocalDateTime(2026, 7, 9, 9, 0),
                )
        }

        test("캘린더 메모 조회는 표시 대상 기간으로 로컬 저장소를 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 6, 29), LocalDate(2026, 8, 9))
            val localList = listOf(calendarMemo(), calendarMemo())
            val localDataSource = mockk<AccountCalendarMemoLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(localList)
            val repository = AccountCalendarMemoRepositoryImpl(accountCalendarMemoLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).first() shouldBe localList.map { local -> local.toDomain() }
        }

        test("캘린더 메모 조회는 조회 결과의 순서를 그대로 유지한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val localList = List(size = 5) { calendarMemo() }
            val localDataSource = mockk<AccountCalendarMemoLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(localList)
            val repository = AccountCalendarMemoRepositoryImpl(accountCalendarMemoLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).first().map { calendarMemo -> calendarMemo.id } shouldBe
                localList.map { local -> local.id }
        }

        test("캘린더 메모 조회는 로컬 저장소의 후속 변경도 도메인 모델로 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val localList = listOf(calendarMemo())
            val changedLocalList = listOf(calendarMemo(), calendarMemo())
            val localFlow = MutableStateFlow(localList)
            val localDataSource = mockk<AccountCalendarMemoLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns localFlow
            val repository = AccountCalendarMemoRepositoryImpl(accountCalendarMemoLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).test {
                awaitItem() shouldBe localList.map { local -> local.toDomain() }

                localFlow.value = changedLocalList

                awaitItem() shouldBe changedLocalList.map { local -> local.toDomain() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("표시 대상 기간과 겹치는 메모가 없으면 빈 목록을 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val localDataSource = mockk<AccountCalendarMemoLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(emptyList())
            val repository = AccountCalendarMemoRepositoryImpl(accountCalendarMemoLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).first() shouldBe emptyList()
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun calendarMemo(): CalendarMemoLocalEntity = fixtureMonkey.giveMeOne<CalendarMemoLocalEntity>()
    }
}
