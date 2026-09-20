package io.github.taetae98coding.diary.data.sync.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncPendingLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class AccountSyncPendingRepositoryImplTest :
    FunSpec({
        test("TC-MORE-HOME-DATA-003 계정 식별자로 기기 저장소의 업로드 대기 여부를 조회한다") {
            listOf(true, false).forEach { hasPending ->
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val localDataSource = mockk<SyncPendingLocalDataSource>()
                every { localDataSource.hasPending(accountId = account.id) } returns flowOf(hasPending)
                val repository = AccountSyncPendingRepositoryImpl(syncPendingLocalDataSource = localDataSource)

                repository.find(account = account).test {
                    awaitItem() shouldBe hasPending
                    awaitComplete()
                }

                verify(exactly = 1) { localDataSource.hasPending(accountId = account.id) }
            }
        }

        test("기기 저장소의 업로드 대기 여부가 바뀌면 바뀐 값을 그대로 내보낸다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<SyncPendingLocalDataSource>()
            every { localDataSource.hasPending(accountId = account.id) } returns flowOf(true, false)
            val repository = AccountSyncPendingRepositoryImpl(syncPendingLocalDataSource = localDataSource)

            repository.find(account = account).test {
                awaitItem() shouldBe true
                awaitItem() shouldBe false
                awaitComplete()
            }
        }

        test("기기 저장소 조회가 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<SyncPendingLocalDataSource>()
            every { localDataSource.hasPending(accountId = account.id) } returns flow { throw throwable }
            val repository = AccountSyncPendingRepositoryImpl(syncPendingLocalDataSource = localDataSource)

            repository.find(account = account).test {
                awaitError() shouldBe throwable
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
