package io.github.taetae98coding.diary.data.web.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.data.tag.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountWebTagRepositoryImplTest :
    FunSpec({
        test("연결된 태그 조회는 현재 계정의 로컬 태그를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val localTagList = List(2) { localTag() }
            val localDataSource = mockk<AccountWebTagLocalDataSource>()
            val transaction = mockk<AccountWebTagTransaction>()
            every {
                localDataSource.getTagList(accountId = account.id, webId = webId)
            } returns flowOf(localTagList)
            val repository =
                AccountWebTagRepositoryImpl(
                    accountWebTagLocalDataSource = localDataSource,
                    accountWebTagTransaction = transaction,
                )

            repository.getTagList(account = account, webId = webId).first() shouldBe
                localTagList.map { local -> local.toDomain() }

            verify(exactly = 1) { localDataSource.getTagList(accountId = account.id, webId = webId) }
        }

        test("연결할 수 있는 태그 페이지 조회는 검색어와 현재 계정으로 조회한 로컬 태그를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val query = fixtureMonkey.giveMeOne<String>()
            val localTagList = List(2) { localTag() }
            val localDataSource = mockk<AccountWebTagLocalDataSource>()
            val transaction = mockk<AccountWebTagTransaction>()
            every {
                localDataSource.pageSelectableTag(accountId = account.id, webId = webId, query = query)
            } returns pagingSource(localTagList)
            val repository =
                AccountWebTagRepositoryImpl(
                    accountWebTagLocalDataSource = localDataSource,
                    accountWebTagTransaction = transaction,
                )

            repository.pageSelectableTag(account = account, webId = webId, query = query).first().items() shouldBe
                localTagList.map { local -> local.toDomain() }

            verify(exactly = 1) { localDataSource.pageSelectableTag(accountId = account.id, webId = webId, query = query) }
        }

        test("연결 저장은 현재 계정으로 로컬 트랜잭션에 위임한다") {
            listOf(true, false).forEach { isDeleted ->
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val webId = fixtureMonkey.giveMeOne<Uuid>()
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val updatedAt = fixtureMonkey.giveMeOne<Instant>()
                val localDataSource = mockk<AccountWebTagLocalDataSource>()
                val transaction = mockk<AccountWebTagTransaction>()
                coEvery {
                    transaction.upsert(
                        accountId = account.id,
                        webId = webId,
                        tagId = tagId,
                        isDeleted = isDeleted,
                        updatedAt = updatedAt,
                    )
                } just Runs
                val repository =
                    AccountWebTagRepositoryImpl(
                        accountWebTagLocalDataSource = localDataSource,
                        accountWebTagTransaction = transaction,
                    )

                repository.upsert(
                    account = account,
                    webId = webId,
                    tagId = tagId,
                    isDeleted = isDeleted,
                    updatedAt = updatedAt,
                )

                coVerify(exactly = 1) {
                    transaction.upsert(
                        accountId = account.id,
                        webId = webId,
                        tagId = tagId,
                        isDeleted = isDeleted,
                        updatedAt = updatedAt,
                    )
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun localTag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(TagLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun pagingSource(tagList: List<TagLocalEntity>): PagingSource<Int, TagLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = tagList,
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
