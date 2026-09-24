package io.github.taetae98coding.diary.data.contact.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountCalendarContactBirthdayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.LunarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.data.contact.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

class AccountCalendarContactBirthdayRepositoryImplTest :
    FunSpec({
        test("캘린더 생일 조회는 표시 대상 기간으로 로컬 저장소를 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 6, 28), LocalDate(2026, 8, 8))
            val localList = listOf(calendarContactBirthday(), calendarContactBirthday())
            val localDataSource = mockk<AccountCalendarContactBirthdayLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(localList)
            val repository = AccountCalendarContactBirthdayRepositoryImpl(accountCalendarContactBirthdayLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).first() shouldBe localList.map { local -> local.toDomain() }
        }

        test("캘린더 생일 조회는 조회 결과의 순서를 그대로 유지한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val localList = List(size = 5) { calendarContactBirthday() }
            val localDataSource = mockk<AccountCalendarContactBirthdayLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(localList)
            val repository = AccountCalendarContactBirthdayRepositoryImpl(accountCalendarContactBirthdayLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).first().map { birthday -> birthday.contactId } shouldBe
                localList.map { local -> local.contactId }
        }

        test("캘린더 생일 조회는 로컬 저장소의 후속 변경도 도메인 모델로 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val localList = listOf(calendarContactBirthday())
            val changedLocalList = listOf(calendarContactBirthday(), calendarContactBirthday())
            val localFlow = MutableStateFlow(localList)
            val localDataSource = mockk<AccountCalendarContactBirthdayLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns localFlow
            val repository = AccountCalendarContactBirthdayRepositoryImpl(accountCalendarContactBirthdayLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).test {
                awaitItem() shouldBe localList.map { local -> local.toDomain() }

                localFlow.value = changedLocalList

                awaitItem() shouldBe changedLocalList.map { local -> local.toDomain() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("표시 대상 기간에 드는 생일이 없으면 빈 목록을 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDateRange(LocalDate(2026, 7, 5), LocalDate(2026, 7, 11))
            val localDataSource = mockk<AccountCalendarContactBirthdayLocalDataSource>()
            every {
                localDataSource.get(accountId = account.id, dateRange = dateRange)
            } returns MutableStateFlow(emptyList())
            val repository = AccountCalendarContactBirthdayRepositoryImpl(accountCalendarContactBirthdayLocalDataSource = localDataSource)

            repository.get(account = account, dateRange = dateRange).first() shouldBe emptyList()
        }

        test("음력 생일 조회는 현재 계정의 음력 생일 연락처를 도메인 모델로 반환하고 후속 변경도 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localList = listOf(fixtureMonkey.giveMeOne<LunarContactBirthdayLocalEntity>())
            val changedLocalList = listOf(fixtureMonkey.giveMeOne<LunarContactBirthdayLocalEntity>(), fixtureMonkey.giveMeOne<LunarContactBirthdayLocalEntity>())
            val localFlow = MutableStateFlow(localList)
            val localDataSource = mockk<AccountCalendarContactBirthdayLocalDataSource>()
            every { localDataSource.getLunar(accountId = account.id) } returns localFlow
            val repository = AccountCalendarContactBirthdayRepositoryImpl(accountCalendarContactBirthdayLocalDataSource = localDataSource)

            repository.getLunar(account = account).test {
                awaitItem() shouldBe localList.map { local -> local.toDomain() }

                localFlow.value = changedLocalList

                awaitItem() shouldBe changedLocalList.map { local -> local.toDomain() }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun calendarContactBirthday(): CalendarContactBirthdayLocalEntity = fixtureMonkey.giveMeOne<CalendarContactBirthdayLocalEntity>()
    }
}
