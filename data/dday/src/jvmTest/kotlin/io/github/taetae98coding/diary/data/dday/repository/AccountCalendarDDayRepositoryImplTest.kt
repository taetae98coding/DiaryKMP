package io.github.taetae98coding.diary.data.dday.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountCalendarDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.dday.calendarDDayLocalEntity
import io.github.taetae98coding.diary.data.dday.mapper.toDomain
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

class AccountCalendarDDayRepositoryImplTest :
    FunSpec({
        test("캘린더 디데이 조회는 표시 대상 기간으로 기기를 조회하고 순서를 유지한 채 후속 변경도 전달한다") {
            val account = Arb.accountUser().next()
            val dateRange = LocalDateRange(LocalDate(2026, 6, 28), LocalDate(2026, 8, 8))
            val firstList = List(3) { Arb.calendarDDayLocalEntity().next() }
            val secondList = List(2) { Arb.calendarDDayLocalEntity().next() }
            val localFlow = MutableStateFlow<List<CalendarDDayLocalEntity>>(firstList)
            val localDataSource = mockk<AccountCalendarDDayLocalDataSource>()
            every { localDataSource.get(accountId = account.id, dateRange = dateRange) } returns localFlow
            val repository = AccountCalendarDDayRepositoryImpl(accountCalendarDDayLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).test {
                awaitItem() shouldBe firstList.map { local -> local.toDomain() }

                localFlow.value = secondList

                awaitItem() shouldBe secondList.map { local -> local.toDomain() }
                cancelAndIgnoreRemainingEvents()
            }
        }
    })
