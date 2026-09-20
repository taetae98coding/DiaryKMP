package io.github.taetae98coding.diary.data.memo.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountDailyMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.DailyMemoLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

class AccountDailyMemoRepositoryImplTest :
    FunSpec({
        test("오늘의 메모 조회는 현재 계정과 날짜로 로컬 저장소를 조회하고 순서를 그대로 유지한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val date = fixtureMonkey.giveMeOne<LocalDate>()
            val localList = List(size = 5) { fixtureMonkey.giveMeOne<DailyMemoLocalEntity>() }
            val localDataSource = mockk<AccountDailyMemoLocalDataSource>()
            every { localDataSource.get(accountId = account.id, date = date) } returns MutableStateFlow(localList)
            val repository = AccountDailyMemoRepositoryImpl(accountDailyMemoLocalDataSource = localDataSource)

            repository.get(account = account, date = date).first() shouldBe localList.map { local -> local.toDomain() }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DATA-002 메모를 조회하지 못하면 빈 결과가 아니라 실패로 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val date = fixtureMonkey.giveMeOne<LocalDate>()
            val throwable = IllegalStateException("query error")
            val localDataSource = mockk<AccountDailyMemoLocalDataSource>()
            every { localDataSource.get(accountId = account.id, date = date) } returns flow { throw throwable }
            val repository = AccountDailyMemoRepositoryImpl(accountDailyMemoLocalDataSource = localDataSource)

            repository.get(account = account, date = date).test {
                awaitError() shouldBeSameInstanceAs throwable
            }
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
