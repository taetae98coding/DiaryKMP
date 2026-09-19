package io.github.taetae98coding.diary.data.playlist.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicTransaction
import io.github.taetae98coding.diary.core.mapper.playlist.toDomain
import io.github.taetae98coding.diary.core.mapper.playlist.toLocal
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant

class AccountMusicRepositoryImplTest :
    FunSpec({
        test("TC-MUSIC-ADD-DATA-001 곡을 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val music = music()
            val localDataSource = mockk<AccountMusicLocalDataSource>()
            val transaction = mockk<AccountMusicTransaction>()
            coEvery { transaction.upsert(accountId = account.id, musicList = listOf(music.toLocal())) } just Runs
            val repository = AccountMusicRepositoryImpl(accountMusicLocalDataSource = localDataSource, accountMusicTransaction = transaction)

            repository.upsert(account = account, music = music)

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, musicList = listOf(music.toLocal()))
            }
        }

        test("TC-MUSIC-ADD-DATA-004 로컬 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val music = music()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountMusicLocalDataSource>()
            val transaction = mockk<AccountMusicTransaction>()
            coEvery { transaction.upsert(accountId = account.id, musicList = listOf(music.toLocal())) } throws throwable
            val repository = AccountMusicRepositoryImpl(accountMusicLocalDataSource = localDataSource, accountMusicTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.upsert(account = account, music = music)
            } shouldBeSameInstanceAs throwable
        }

        test("TC-PLAYLIST-HOME-DATA-001 곡 목록 페이지 조회는 현재 계정의 로컬 곡을 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localMusicList = List(2) { localMusic() }
            val localDataSource = mockk<AccountMusicLocalDataSource>()
            val transaction = mockk<AccountMusicTransaction>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.TITLE) } returns pagingSource(localMusicList)
            val repository = AccountMusicRepositoryImpl(accountMusicLocalDataSource = localDataSource, accountMusicTransaction = transaction)

            repository.page(account = account, sort = ListSort.TITLE).first().items() shouldBe localMusicList.map { local -> local.toDomain() }
        }

        test("선택한 정렬을 로컬 정렬로 변환해 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localMusicList = List(2) { localMusic() }
            val localDataSource = mockk<AccountMusicLocalDataSource>()
            val transaction = mockk<AccountMusicTransaction>()
            every {
                localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.RECENTLY_UPDATED)
            } returns pagingSource(localMusicList)
            val repository = AccountMusicRepositoryImpl(accountMusicLocalDataSource = localDataSource, accountMusicTransaction = transaction)

            repository.page(account = account, sort = ListSort.RECENTLY_UPDATED).first().items() shouldBe
                localMusicList.map { local -> local.toDomain() }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun music(): Music =
            fixtureMonkey
                .giveMeKotlinBuilder<Music>()
                .setExp(Music::updatedAt, instant())
                .setExp(Music::createdAt, instant())
                .sample()

        private fun localMusic(): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(MusicLocalEntity::updatedAt, instant())
                .setExp(MusicLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun pagingSource(musicList: List<MusicLocalEntity>): PagingSource<Int, MusicLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = musicList,
                        prevKey = null,
                        nextKey = null,
                    )
            }

        private suspend fun <T : Any> PagingData<T>.items(): List<T> =
            coroutineScope {
                val presenter =
                    object : PagingDataPresenter<T>(mainContext = coroutineContext) {
                        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) = Unit
                    }
                val collection = launch { presenter.collectFrom(this@items) }

                presenter.loadStateFlow
                    .filterNotNull()
                    .first { loadStates -> loadStates.refresh is LoadState.NotLoading }
                val result = presenter.snapshot().items

                collection.cancelAndJoin()
                result
            }
    }
}
