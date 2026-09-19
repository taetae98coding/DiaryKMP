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

        test("TC-MUSIC-ADD-DOMAIN-005 TC-MUSIC-ADD-DOMAIN-006 TC-MUSIC-ADD-DATA-007 링크, 제목, 가수, 썸네일과 미삭제 상태, 추가 시각을 그대로 저장하고 함께 조회한다") {
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
