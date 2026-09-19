package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountMusicTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMusicLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountMusicLocalDataSourceImpl
        lateinit var musicTransaction: AccountMusicTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountMusicLocalDataSourceImpl(database = database)
            musicTransaction = AccountMusicTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun PagingSource<Int, MusicLocalEntity>.pagedMusics(loadSize: Int = 100): List<MusicLocalEntity> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MusicLocalEntity>>().data
        }

        suspend fun pagedMusics(accountId: Uuid): List<MusicLocalEntity> = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT).pagedMusics()

        test("TC-PLAYLIST-HOME-DOMAIN-001 TC-PLAYLIST-HOME-DOMAIN-003 삭제되지 않은 계정의 곡만 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstMusic = music(title = FIRST_MUSIC_TITLE)
            val lastMusic = music(title = LAST_MUSIC_TITLE)
            val deletedMusic = music().copy(isDeleted = true)
            val otherAccountMusic = music()
            musicTransaction.upsert(accountId = accountId, musicList = listOf(lastMusic, firstMusic, deletedMusic))
            musicTransaction.upsert(accountId = otherAccountId, musicList = listOf(otherAccountMusic))

            pagedMusics(accountId) shouldBe listOf(firstMusic, lastMusic)
        }

        test("TC-PLAYLIST-HOME-DOMAIN-004 가수는 정렬 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstMusic = music(title = FIRST_MUSIC_TITLE, artist = LAST_MUSIC_TITLE)
            val lastMusic = music(title = LAST_MUSIC_TITLE, artist = FIRST_MUSIC_TITLE)
            musicTransaction.upsert(accountId = accountId, musicList = listOf(lastMusic, firstMusic))

            pagedMusics(accountId) shouldBe listOf(firstMusic, lastMusic)
        }

        test("TC-PLAYLIST-HOME-DOMAIN-002 저장된 곡이 없는 계정은 빈 목록으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            musicTransaction.upsert(accountId = otherAccountId, musicList = listOf(music()))

            pagedMusics(accountId).shouldBeEmpty()
        }

        test("TC-PLAYLIST-HOME-DATA-001 요청한 크기만큼 페이지로 나누어 이어서 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val musicList = List(3) { index -> music(title = "$index-${fixtureMonkey.giveMeOne<String>()}") }
            musicTransaction.upsert(accountId = accountId, musicList = musicList)

            val pagingSource = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT)
            val firstPage =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            val nextPage =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = checkNotNull(firstPage.nextKey), loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page

            firstPage.data shouldBe musicList.take(2)
            nextPage.data shouldBe musicList.drop(2)
        }

        test("TC-PLAYLIST-HOME-DATA-002 TC-PLAYLIST-HOME-DOMAIN-001 저장된 곡의 변화가 페이지 조회에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            val renamedMusic = music.copy(detail = music.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))

            suspend fun assertPageInvalidated(
                expected: List<MusicLocalEntity>,
                change: suspend () -> Unit,
            ) {
                val pagingSource = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT)
                pagingSource.pagedMusics()
                val invalidated = CompletableDeferred<Unit>()
                pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

                change()

                withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
                pagingSource.invalid.shouldBeTrue()
                pagedMusics(accountId) shouldBe expected
            }

            pagedMusics(accountId).shouldBeEmpty()
            assertPageInvalidated(expected = listOf(music)) {
                musicTransaction.upsert(accountId = accountId, musicList = listOf(music))
            }
            assertPageInvalidated(expected = listOf(renamedMusic)) {
                musicTransaction.upsert(accountId = accountId, musicList = listOf(renamedMusic))
            }
            assertPageInvalidated(expected = emptyList()) {
                musicTransaction.upsert(accountId = accountId, musicList = listOf(renamedMusic.copy(isDeleted = true)))
            }
        }

        test("최근 수정순은 수정 시각 내림차순으로 조회하고 같으면 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val latestMusic = music(title = LAST_MUSIC_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            val sameUpdatedFirstMusic = music(title = FIRST_MUSIC_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val sameUpdatedLastMusic = music(title = MIDDLE_MUSIC_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            musicTransaction.upsert(
                accountId = accountId,
                musicList = listOf(sameUpdatedLastMusic, latestMusic, sameUpdatedFirstMusic),
            )

            dataSource
                .page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED)
                .pagedMusics() shouldBe listOf(latestMusic, sameUpdatedFirstMusic, sameUpdatedLastMusic)
        }

        test("정렬을 고르지 않은 기본 순서는 제목순과 같다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstMusic = music(title = FIRST_MUSIC_TITLE)
            val lastMusic = music(title = LAST_MUSIC_TITLE)
            musicTransaction.upsert(accountId = accountId, musicList = listOf(lastMusic, firstMusic))

            dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT).pagedMusics() shouldBe
                dataSource.page(accountId = accountId, sort = ListSortLocalEntity.TITLE).pagedMusics()
        }
    }) {
    public companion object {
        private const val FIRST_MUSIC_TITLE = "AppleMusic"
        private const val MIDDLE_MUSIC_TITLE = "MangoMusic"
        private const val LAST_MUSIC_TITLE = "ZebraMusic"
        private const val INVALIDATION_TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun music(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            artist: String = "artist-${fixtureMonkey.giveMeOne<String>()}",
        ): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(MusicLocalEntity::detail, MusicDetailLocalEntity(title = title, artist = artist))
                .setExp(MusicLocalEntity::isDeleted, false)
                .setExp(MusicLocalEntity::updatedAt, instant())
                .setExp(MusicLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
