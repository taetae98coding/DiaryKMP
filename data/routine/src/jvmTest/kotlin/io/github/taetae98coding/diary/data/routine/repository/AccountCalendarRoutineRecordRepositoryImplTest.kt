package io.github.taetae98coding.diary.data.routine.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountCalendarRoutineRecordLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.calendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.data.routine.mapper.toDomain
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class AccountCalendarRoutineRecordRepositoryImplTest :
    FunSpec({
        test("TC-8f9fe355-248b-46ce-aee4-3075f115efb3 캘린더 루틴 기록 조회는 표시 대상 기간으로 기기를 조회하고 순서를 유지한 채 후속 변경도 전달한다") {
            val account = Arb.accountUser().next()
            val dateRange = Arb.localDateRange().next()
            val firstList = List(3) { Arb.calendarRoutineRecordLocalEntity().next() }
            val secondList = List(2) { Arb.calendarRoutineRecordLocalEntity().next() }
            val localFlow = MutableStateFlow<List<CalendarRoutineRecordLocalEntity>>(firstList)
            val localDataSource = mockk<AccountCalendarRoutineRecordLocalDataSource>()
            every { localDataSource.get(accountId = account.id, dateRange = dateRange) } returns localFlow
            val repository = AccountCalendarRoutineRecordRepositoryImpl(accountCalendarRoutineRecordLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).test {
                awaitItem() shouldBe firstList.map { local -> local.toDomain() }

                localFlow.value = secondList

                awaitItem() shouldBe secondList.map { local -> local.toDomain() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-dc0abe02-1923-4800-bd58-e16cb4cdf229 기기 조회가 실패하면 빈 결과로 바꾸지 않고 실패를 그대로 전달한다") {
            val account = Arb.accountUser().next()
            val dateRange = Arb.localDateRange().next()
            val failure = IllegalStateException(Arb.string().next())
            val localDataSource = mockk<AccountCalendarRoutineRecordLocalDataSource>()
            every { localDataSource.get(accountId = account.id, dateRange = dateRange) } returns flow { throw failure }
            val repository = AccountCalendarRoutineRecordRepositoryImpl(accountCalendarRoutineRecordLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).test {
                awaitError() shouldBeSameInstanceAs failure
            }
        }
    })
