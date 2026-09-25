package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountMusicLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountMusicSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class AccountMusicSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMusicSyncTransactionImpl
        lateinit var dataSource: AccountMusicLocalDataSourceImpl
        lateinit var syncDataSource: AccountMusicSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMusicSyncTransactionImpl(database = database)
            dataSource = AccountMusicLocalDataSourceImpl(database = database)
            syncDataSource = AccountMusicSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 곡은 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote =
                fixtureMonkey
                    .giveMeKotlinBuilder<MusicLocalEntity>()
                    .setExp(MusicLocalEntity::isDeleted, false)
                    .sample()

            transaction.save(accountId = accountId, musicList = listOf(remote), cursor = 5L)

            dataSource.find(accountId = accountId, musicId = remote.id).first() shouldBe remote
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MUSIC) shouldBe 5L
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
