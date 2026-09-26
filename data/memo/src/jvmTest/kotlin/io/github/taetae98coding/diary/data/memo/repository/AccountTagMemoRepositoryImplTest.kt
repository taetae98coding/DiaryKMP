package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountTagMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountTagMemoRepositoryImplTest :
    FunSpec({
        test("태그별 메모 페이지는 현재 계정과 태그의 로컬 페이지를 도메인 모델로 변환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val localMemoList = listOf(memo(), memo())
            val localDataSource = mockk<AccountTagMemoLocalDataSource>()
            every {
                localDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = TagScopeLocalEntity.SELF,
                    sort = ListSortLocalEntity.DEFAULT,
                )
            } returns mockMemoPagingSource(localMemoList)
            val repository = AccountTagMemoRepositoryImpl(accountTagMemoLocalDataSource = localDataSource)

            repository.page(account = account, tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT).first().items() shouldBe
                localMemoList.map { memo -> memo.toDomain() }

            verify(exactly = 1) {
                localDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = TagScopeLocalEntity.SELF,
                    sort = ListSortLocalEntity.DEFAULT,
                )
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DATA-001 태그별 완료 메모 페이지는 현재 계정과 태그의 로컬 완료 페이지를 도메인 모델로 변환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val localMemoList = listOf(memo(), memo())
            val localDataSource = mockk<AccountTagMemoLocalDataSource>()
            every {
                localDataSource.pageFinished(
                    accountId = account.id,
                    tagId = tagId,
                    sort = ListSortLocalEntity.DEFAULT,
                )
            } returns mockMemoPagingSource(localMemoList)
            val repository = AccountTagMemoRepositoryImpl(accountTagMemoLocalDataSource = localDataSource)

            repository.pageFinished(account = account, tagId = tagId, sort = ListSort.DEFAULT).first().items() shouldBe
                localMemoList.map { memo -> memo.toDomain() }

            verify(exactly = 1) {
                localDataSource.pageFinished(
                    accountId = account.id,
                    tagId = tagId,
                    sort = ListSortLocalEntity.DEFAULT,
                )
            }
        }
    })

private fun memo(): MemoLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoLocalEntity>()
        .setExp(MemoLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(MemoLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()

private fun mockMemoPagingSource(memoList: List<MemoLocalEntity>): PagingSource<Int, MemoLocalEntity> =
    mockk(relaxed = true) {
        coEvery { load(any()) } returns
            PagingSource.LoadResult.Page(
                data = memoList,
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
