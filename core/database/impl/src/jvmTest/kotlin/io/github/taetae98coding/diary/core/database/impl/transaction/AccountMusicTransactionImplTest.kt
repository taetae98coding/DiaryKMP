package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
                    SELECT id, title, artist, is_deleted, updated_at, created_at
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

        test("TC-MUSIC-ADD-DATA-001 TC-MUSIC-ADD-DATA-005 곡과 현재 계정의 연결을 업로드 대기 상태로 함께 저장한다") {
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

        test("TC-MUSIC-ADD-DOMAIN-005 TC-MUSIC-ADD-DOMAIN-006 제목, 가수와 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val music =
                music().copy(
                    detail =
                        MusicDetailLocalEntity(
                            title = "  title-${fixtureMonkey.giveMeOne<String>()}  ",
                            artist = "  artist-${fixtureMonkey.giveMeOne<String>()}  ",
                        ),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, musicList = listOf(music))

            findMusicList() shouldBe listOf(music)
        }

        test("TC-MUSIC-ADD-DATA-003 같은 식별자의 곡이 있으면 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            val renamedMusic = music.copy(detail = music.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))

            transaction.upsert(accountId = accountId, musicList = listOf(music))
            transaction.upsert(accountId = accountId, musicList = listOf(renamedMusic))

            findMusicList() shouldBe listOf(renamedMusic)
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
                        title = getText(1),
                        artist = getText(2),
                    ),
                isDeleted = getBoolean(3),
                updatedAt = Instant.fromEpochMilliseconds(getLong(4)),
                createdAt = Instant.fromEpochMilliseconds(getLong(5)),
            )

        private fun SQLiteStatement.toAccountMusic(): AccountMusicLocalEntity =
            AccountMusicLocalEntity(
                accountId = Uuid.parse(getText(0)),
                musicId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
