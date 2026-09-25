package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountContactLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountContactSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.contact.contactPhoneNumberCaseList
import io.github.taetae98coding.diary.core.testing.contact.localContact
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class AccountContactSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountContactSyncTransactionImpl
        lateinit var dataSource: AccountContactLocalDataSourceImpl
        lateinit var syncDataSource: AccountContactSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountContactSyncTransactionImpl(database = database)
            dataSource = AccountContactLocalDataSourceImpl(database = database)
            syncDataSource = AccountContactSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 연락처는 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            fixtureMonkey.contactPhoneNumberCaseList().forEach { numberList ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val cursor = fixtureMonkey.giveMeOne<Long>()
                val remote =
                    fixtureMonkey.localContact(
                        numberList = numberList,
                        hasMeasure = fixtureMonkey.giveMeOne<Boolean>(),
                        isFavorite = fixtureMonkey.giveMeOne<Boolean>(),
                        isDeleted = false,
                    )

                transaction.save(accountId = accountId, contactList = listOf(remote), cursor = cursor)

                dataSource.find(accountId = accountId, contactId = remote.id).first() shouldBe remote
                syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
                syncCursorDataSource.find(accountId = accountId, kind = SyncKind.CONTACT) shouldBe cursor
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
