package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountMusicLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountMusicSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MusicSyncTestException : RuntimeException()

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

        suspend fun insertWithSyncState(
            accountId: Uuid,
            music: MusicLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.musicDao().upsert(listOf(music))
                database.accountMusicDao().upsert(
                    AccountMusicLocalEntity(accountId = accountId, musicId = music.id, isDirty = isDirty),
                )
            }
        }

        suspend fun isPending(
            accountId: Uuid,
            musicId: Uuid,
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { music -> music.id == musicId }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 곡은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = music()
            val accountEntity = music()
            insertWithSyncState(accountId = Uuid.NIL, music = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, music = accountEntity, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            insertWithSyncState(accountId = accountId, music = music, isDirty = true)

            transaction.clearPending(accountId = accountId, musicList = listOf(music))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 곡은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedMusic = music(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedMusic = pushedMusic.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, music = changedMusic, isDirty = true)

            transaction.clearPending(accountId = accountId, musicList = listOf(pushedMusic))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedMusic)
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localMusic = music(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteMusic =
                    localMusic.copy(
                        detail = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>(),
                        isDeleted = !localMusic.isDeleted,
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, music = localMusic, isDirty = false)

                transaction.save(accountId = accountId, musicList = listOf(remoteMusic), cursor = 5L)

                dataSource.find(accountId = accountId, musicId = localMusic.id).first() shouldBe remoteMusic
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localMusic = music(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteMusic =
                localMusic.copy(
                    detail = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId = accountId, music = localMusic, isDirty = true)

            transaction.save(accountId = accountId, musicList = listOf(remoteMusic), cursor = 5L)

            dataSource.find(accountId = accountId, musicId = localMusic.id).first() shouldBe localMusic
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MUSIC) shouldBe 5L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localMusic = music(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteMusic =
                    localMusic.copy(
                        detail = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>(),
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, music = localMusic, isDirty = true)

                transaction.save(accountId = accountId, musicList = listOf(remoteMusic), cursor = 5L)

                isPending(accountId = accountId, musicId = localMusic.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 곡과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteMusic = music()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MusicSyncTestException()
            val failingTransaction = AccountMusicSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MusicSyncTestException> {
                failingTransaction.save(accountId = accountId, musicList = listOf(remoteMusic), cursor = 5L)
            }

            dataSource.find(accountId = accountId, musicId = remoteMusic.id).first().shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MUSIC) shouldBe 0L
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

        private fun music(updatedAt: Instant = instant()): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(MusicLocalEntity::updatedAt, updatedAt)
                .setExp(MusicLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochSeconds(fixtureMonkey.giveMeOne<Int>().toLong())
    }
}
