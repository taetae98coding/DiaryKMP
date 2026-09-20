package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.impl.SyncTimeData
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountSyncTimeLocalDataSourceImplTest :
    FunSpec({
        test("기록이 없는 계정은 마지막 동기화 시각이 없다") {
            val dataSource = AccountSyncTimeLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(SyncTimeData())))

            dataSource.find(accountId = fixtureMonkey.giveMeOne()).shouldBeNull()
        }

        test("기록한 계정의 마지막 동기화 시각을 그대로 제공한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val syncedAt = fixtureMonkey.giveMeOne<Instant>()
            val dataStore = mockDataStore(MutableStateFlow(SyncTimeData()))
            val dataSource = AccountSyncTimeLocalDataSourceImpl(dataStore = dataStore)

            dataSource.upsert(accountId = accountId, syncedAt = syncedAt)

            dataSource.find(accountId = accountId) shouldBe syncedAt
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("같은 계정을 다시 기록하면 마지막 동기화 시각을 대체한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val syncedAt = fixtureMonkey.giveMeOne<Instant>()
            val dataSource = AccountSyncTimeLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(SyncTimeData())))

            dataSource.upsert(accountId = accountId, syncedAt = fixtureMonkey.giveMeOne())
            dataSource.upsert(accountId = accountId, syncedAt = syncedAt)

            dataSource.find(accountId = accountId) shouldBe syncedAt
        }

        test("한 계정을 기록해도 다른 계정의 마지막 동기화 시각은 그대로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherSyncedAt = fixtureMonkey.giveMeOne<Instant>()
            val dataSource = AccountSyncTimeLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(SyncTimeData())))

            dataSource.upsert(accountId = otherAccountId, syncedAt = otherSyncedAt)
            dataSource.upsert(accountId = accountId, syncedAt = fixtureMonkey.giveMeOne())

            dataSource.find(accountId = otherAccountId) shouldBe otherSyncedAt
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}

private fun mockDataStore(syncTimeFlow: MutableStateFlow<SyncTimeData>): DataStore<SyncTimeData> =
    mockk<DataStore<SyncTimeData>>().also { dataStore ->
        every { dataStore.data } returns syncTimeFlow
        coEvery { dataStore.updateData(any()) } coAnswers {
            val updated = firstArg<suspend (SyncTimeData) -> SyncTimeData>().invoke(syncTimeFlow.value)

            syncTimeFlow.value = updated
            updated
        }
    }
