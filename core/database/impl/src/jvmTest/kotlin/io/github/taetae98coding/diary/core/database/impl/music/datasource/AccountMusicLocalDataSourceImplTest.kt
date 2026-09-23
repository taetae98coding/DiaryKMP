package io.github.taetae98coding.diary.core.database.impl.music.datasource

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
import io.github.taetae98coding.diary.core.database.impl.music.transaction.AccountMusicTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
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

        test("TC-MUSIC-DETAIL-DOMAIN-001 삭제 여부와 관계없이 계정의 곡을 대상 식별자로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            val deletedMusic = music().copy(isDeleted = true)
            musicTransaction.upsert(accountId = accountId, musicList = listOf(music, deletedMusic))

            dataSource.find(accountId = accountId, musicId = music.id).first() shouldBe music
            dataSource.find(accountId = accountId, musicId = deletedMusic.id).first() shouldBe deletedMusic
        }

        test("TC-MUSIC-DETAIL-DATA-001 다른 계정의 곡은 대상 식별자로도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            musicTransaction.upsert(accountId = otherAccountId, musicList = listOf(music))

            dataSource.find(accountId = accountId, musicId = music.id).first() shouldBe null
        }

        test("TC-MUSIC-DETAIL-DOMAIN-008 TC-MUSIC-DETAIL-DATA-008 수정한 곡은 목록에 반영되고 삭제한 곡은 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val music = music()
            musicTransaction.upsert(accountId = accountId, musicList = listOf(music))
            val detail = music.detail.copy(title = LAST_MUSIC_TITLE, artist = FIRST_MUSIC_TITLE)
            val updatedAt = instant()

            musicTransaction.updateDetail(accountId = accountId, musicId = music.id, detail = detail, updatedAt = updatedAt)

            pagedMusics(accountId) shouldBe listOf(music.copy(detail = detail, updatedAt = updatedAt))

            musicTransaction.updateDeleted(accountId = accountId, musicId = music.id, isDeleted = true, updatedAt = instant())

            pagedMusics(accountId).shouldBeEmpty()
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
            link: String = "https://youtu.be/${fixtureMonkey.giveMeOne<String>()}",
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            artist: String = "artist-${fixtureMonkey.giveMeOne<String>()}",
            thumbnail: String = "https://i.ytimg.com/vi/${fixtureMonkey.giveMeOne<String>()}/hqdefault.jpg",
        ): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(
                    MusicLocalEntity::detail,
                    MusicDetailLocalEntity(link = link, title = title, artist = artist, thumbnail = thumbnail),
                ).setExp(MusicLocalEntity::isDeleted, false)
                .setExp(MusicLocalEntity::updatedAt, instant())
                .setExp(MusicLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
