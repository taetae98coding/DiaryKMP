package io.github.taetae98coding.diary.data.tag.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tagfilter.datasource.AccountTagFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class AccountTagFilterRepositoryImplTest :
    FunSpec({
        test("TC-TAG-HOME-DATA-008 저장된 필터 선택을 그대로 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountTagFilterLocalDataSource>()
            every { localDataSource.find(accountId = account.id) } returns
                flowOf(TagFilterLocalEntity(accountId = account.id, isTopLevelOnly = true))
            val repository = AccountTagFilterRepositoryImpl(accountTagFilterLocalDataSource = localDataSource)

            repository.getTopLevelOnly(account = account).first() shouldBe true
        }

        test("TC-TAG-HOME-DATA-009 저장된 필터 선택이 없으면 꺼진 상태로 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountTagFilterLocalDataSource>()
            every { localDataSource.find(accountId = account.id) } returns flowOf(null)
            val repository = AccountTagFilterRepositoryImpl(accountTagFilterLocalDataSource = localDataSource)

            repository.getTopLevelOnly(account = account).first() shouldBe false
        }

        test("TC-TAG-HOME-DATA-008 필터 선택을 현재 계정 식별자와 함께 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountTagFilterLocalDataSource>()
            coEvery { localDataSource.upsert(accountId = account.id, isTopLevelOnly = true) } just Runs
            val repository = AccountTagFilterRepositoryImpl(accountTagFilterLocalDataSource = localDataSource)

            repository.upsert(account = account, isTopLevelOnly = true)

            coVerify(exactly = 1) { localDataSource.upsert(accountId = account.id, isTopLevelOnly = true) }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
