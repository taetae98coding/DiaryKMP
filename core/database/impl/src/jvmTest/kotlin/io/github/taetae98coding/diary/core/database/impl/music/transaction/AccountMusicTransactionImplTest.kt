package io.github.taetae98coding.diary.core.database.impl.music.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMusicTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMusicTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMusicTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun findMusicList(): List<MusicLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT id, link, title, artist, thumbnail, is_deleted, updated_at, created_at
                    FROM music
                    ORDER BY id ASC
                    """,
                ) { statement -> statement.readAll { it.toMusic() } }
            }

        suspend fun findAccountMusicList(): List<AccountMusicLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT account_id, music_id, is_dirty
                    FROM account_music
                    ORDER BY music_id ASC
                    """,
                ) { statement -> statement.readAll { it.toAccountMusic() } }
            }

        test("TC-MUSIC-ADD-DATA-001 TC-MUSIC-ADD-DATA-005 TC-DATA-SYNC-DOMAIN-001 곡과 현재 계정의 연결을 업로드 대기 상태로 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()

            transaction.upsert(accountId = accountId, musicList = listOf(music))

            findMusicList() shouldBe listOf(music)
            findAccountMusicList() shouldBe
                listOf(
                    AccountMusicLocalEntity(
                        accountId = accountId,
                        musicId = music.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-MUSIC-ADD-DOMAIN-005 TC-MUSIC-ADD-DOMAIN-006 TC-MUSIC-ADD-DATA-007 제목, 가수, 링크와 미삭제 상태, 추가 시각을 그대로 저장하고 함께 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val music =
                music().copy(
                    detail =
                        MusicDetailLocalEntity(
                            link = "https://youtu.be/${fixtureMonkey.giveMeOne<String>()}",
                            title = "  title-${fixtureMonkey.giveMeOne<String>()}  ",
                            artist = "  artist-${fixtureMonkey.giveMeOne<String>()}  ",
                            thumbnail = "https://i.ytimg.com/vi/${fixtureMonkey.giveMeOne<String>()}/hqdefault.jpg",
                        ),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, musicList = listOf(music))

            findMusicList() shouldBe listOf(music)
        }

        test("TC-MUSIC-DETAIL-DATA-003 TC-MUSIC-DETAIL-DATA-007 TC-MUSIC-ADD-DATA-011 TC-DATA-SYNC-DOMAIN-001 수정은 제목, 가수, 링크와 수정 시각만 바꾸고 남은 썸네일 주소는 지우지 않으며 업로드 대기로 기록한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            transaction.upsert(accountId = accountId, musicList = listOf(music))
            markUploaded(database = database, accountId = accountId, musicId = music.id)
            val detail = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>()
            val updatedAt = instant()

            transaction.updateDetail(accountId = accountId, musicId = music.id, detail = detail, updatedAt = updatedAt) shouldBe 1

            findMusicList() shouldBe listOf(music.copy(detail = detail.copy(thumbnail = music.detail.thumbnail), updatedAt = updatedAt))
            findAccountMusicList() shouldBe listOf(AccountMusicLocalEntity(accountId = accountId, musicId = music.id, isDirty = true))
        }

        test("TC-MUSIC-DETAIL-DOMAIN-007 TC-MUSIC-DETAIL-DATA-005 TC-DATA-SYNC-DOMAIN-001 삭제는 삭제 여부와 수정 시각만 바꾸고 곡과 계정 연결을 남기며 업로드 대기로 기록한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, musicList = listOf(music))
            markUploaded(database = database, accountId = accountId, musicId = music.id)
            val updatedAt = instant()

            transaction.updateDeleted(accountId = accountId, musicId = music.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            findMusicList() shouldBe listOf(music.copy(isDeleted = true, updatedAt = updatedAt))
            findAccountMusicList() shouldBe listOf(AccountMusicLocalEntity(accountId = accountId, musicId = music.id, isDirty = true))
        }

        test("TC-MUSIC-DETAIL-DATA-004 현재 계정과 대상 식별자를 만족하는 곡이 없으면 아무것도 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            transaction.upsert(accountId = accountId, musicList = listOf(music))

            transaction.updateDetail(
                accountId = otherAccountId,
                musicId = music.id,
                detail = fixtureMonkey.giveMeOne<MusicDetailLocalEntity>(),
                updatedAt = instant(),
            ) shouldBe 0
            transaction.updateDeleted(accountId = otherAccountId, musicId = music.id, isDeleted = true, updatedAt = instant()) shouldBe 0

            findMusicList() shouldBe listOf(music)
        }

        test("TC-MUSIC-ADD-DATA-003 곡을 추가해도 이미 저장된 곡은 덮어쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val savedMusic = music()
            val addedMusic =
                music().copy(
                    detail =
                        MusicDetailLocalEntity(
                            link = "https://www.youtube.com/watch?v=added-${fixtureMonkey.giveMeOne<String>()}",
                            title = "added-${savedMusic.detail.title}",
                            artist = "added-${savedMusic.detail.artist}",
                            thumbnail = savedMusic.detail.thumbnail,
                        ),
                )
            transaction.upsert(accountId = accountId, musicList = listOf(savedMusic))

            transaction.upsert(accountId = accountId, musicList = listOf(addedMusic))

            findMusicList() shouldBe listOf(savedMusic, addedMusic).sortedBy { music -> music.id.toString() }
        }

        test("TC-MUSIC-ADD-DATA-004 저장에 실패하면 곡과 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountMusicDao() } throws throwable
            val failingTransaction = AccountMusicTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(accountId = accountId, musicList = listOf(music))
            }.message shouldBe throwable.message

            findMusicList().shouldBeEmpty()
            findAccountMusicList().shouldBeEmpty()
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun music(): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(MusicLocalEntity::detail, fixtureMonkey.giveMeOne<MusicDetailLocalEntity>())
                .setExp(MusicLocalEntity::updatedAt, instant())
                .setExp(MusicLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        // 수정과 삭제가 업로드 대기를 다시 세우는지 보려면 저장 직후의 대기 상태를 먼저 지워야 한다.
        private suspend fun markUploaded(
            database: DiaryDatabase,
            accountId: Uuid,
            musicId: Uuid,
        ) {
            database.useWriterConnection { transactor ->
                transactor.usePrepared("UPDATE account_music SET is_dirty = 0 WHERE account_id = ? AND music_id = ?") { statement ->
                    statement.bindText(1, accountId.toString())
                    statement.bindText(2, musicId.toString())
                    statement.step()
                }
            }
        }

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toMusic(): MusicLocalEntity =
            MusicLocalEntity(
                id = Uuid.parse(getText(0)),
                detail =
                    MusicDetailLocalEntity(
                        link = getText(1),
                        title = getText(2),
                        artist = getText(3),
                        thumbnail = getText(4),
                    ),
                isDeleted = getBoolean(5),
                updatedAt = Instant.fromEpochMilliseconds(getLong(6)),
                createdAt = Instant.fromEpochMilliseconds(getLong(7)),
            )

        private fun SQLiteStatement.toAccountMusic(): AccountMusicLocalEntity =
            AccountMusicLocalEntity(
                accountId = Uuid.parse(getText(0)),
                musicId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
