package io.github.taetae98coding.diary.data.web.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.data.web.mapper.toDomain
import io.github.taetae98coding.diary.data.web.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountWebRepositoryImplTest :
    FunSpec({
        test("TC-WEB-ADD-DATA-001 웹 항목을 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val web = web()
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery { transaction.upsert(accountId = account.id, webList = listOf(web.toLocal()), webTagList = emptyList()) } just Runs
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.upsert(account = account, web = web, tagIdSet = emptySet())

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, webList = listOf(web.toLocal()), webTagList = emptyList())
            }
        }

        test("TC-WEB-ADD-DATA-003 요청 헤더는 배치한 순서 그대로 로컬 모델로 변환된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val headerList =
                listOf(
                    WebHeader(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeader(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeader(name = "X-Region", value = ""),
                )
            val web = web().let { web -> web.copy(detail = web.detail.copy(headerList = headerList)) }
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery { transaction.upsert(accountId = any(), webList = any(), webTagList = any()) } just Runs
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.upsert(account = account, web = web, tagIdSet = emptySet())

            coVerify(exactly = 1) {
                transaction.upsert(
                    accountId = account.id,
                    webList = listOf(web.toLocal()),
                    webTagList = emptyList(),
                )
            }
        }

        test("TC-WEB-ADD-DATA-004 기기 저장이 실패하면 추가를 성공으로 다루지 않고 실패를 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val web = web()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery { transaction.upsert(accountId = account.id, webList = listOf(web.toLocal()), webTagList = emptyList()) } throws throwable
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.upsert(account = account, web = web, tagIdSet = emptySet())
            } shouldBeSameInstanceAs throwable
        }

        test("TC-WEB-HOME-DATA-001 웹 목록 페이지 조회는 현재 계정의 로컬 웹 항목을 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localWebList = List(2) { localWeb() }
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.TITLE) } returns pagingSource(localWebList)
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.page(account = account, sort = ListSort.TITLE).first().items() shouldBe localWebList.map { local -> local.toDomain() }
        }

        test("TC-WEB-DETAIL-DATA-001 상세 대상 조회는 현재 계정의 로컬 웹 항목을 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localWeb = localWeb()
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            every { localDataSource.find(accountId = account.id, webId = localWeb.id) } returns flowOf(localWeb)
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.find(account = account, webId = localWeb.id).first() shouldBe localWeb.toDomain()
        }

        test("TC-WEB-DETAIL-DOMAIN-001 상세 대상이 조회되지 않으면 없음을 그대로 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            every { localDataSource.find(accountId = account.id, webId = webId) } returns flowOf(null)
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.find(account = account, webId = webId).first().shouldBeNull()
        }

        test("TC-WEB-DETAIL-DATA-008 삭제는 현재 계정 식별자와 함께 로컬 저장소에 반영한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val updatedAt = instant()
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery {
                transaction.updateDeleted(accountId = account.id, webId = webId, isDeleted = true, updatedAt = updatedAt)
            } returns 1
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.updateDeleted(account = account, webId = webId, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 1) {
                transaction.updateDeleted(accountId = account.id, webId = webId, isDeleted = true, updatedAt = updatedAt)
            }
        }

        test("TC-WEB-DETAIL-DATA-014 수정은 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 반영한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = web().detail
            val updatedAt = instant()
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery {
                transaction.updateDetail(accountId = account.id, webId = webId, detail = detail.toLocal(), updatedAt = updatedAt)
            } returns 1
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.updateDetail(account = account, webId = webId, detail = detail, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 1) {
                transaction.updateDetail(accountId = account.id, webId = webId, detail = detail.toLocal(), updatedAt = updatedAt)
            }
        }

        test("TC-WEB-DETAIL-DATA-010 수정 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery {
                transaction.updateDetail(accountId = any(), webId = any(), detail = any(), updatedAt = any())
            } throws throwable
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateDetail(
                    account = account,
                    webId = fixtureMonkey.giveMeOne<Uuid>(),
                    detail = web().detail,
                    updatedAt = instant(),
                )
            } shouldBeSameInstanceAs throwable
        }

        test("TC-WEB-DETAIL-DATA-010 삭제 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            coEvery {
                transaction.updateDeleted(accountId = any(), webId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateDeleted(
                    account = account,
                    webId = fixtureMonkey.giveMeOne<Uuid>(),
                    isDeleted = true,
                    updatedAt = instant(),
                )
            } shouldBeSameInstanceAs throwable
        }

        test("TC-WEB-HOME-DOMAIN-002 조회된 웹 항목이 없으면 빈 목록을 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountWebLocalDataSource>()
            val transaction = mockk<AccountWebTransaction>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.TITLE) } returns pagingSource(emptyList())
            val repository = AccountWebRepositoryImpl(accountWebLocalDataSource = localDataSource, accountWebTransaction = transaction)

            repository.page(account = account, sort = ListSort.TITLE).first().items() shouldBe emptyList()
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::updatedAt, instant())
                .setExp(Web::createdAt, instant())
                .sample()

        private fun localWeb(): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun pagingSource(webList: List<WebLocalEntity>): PagingSource<Int, WebLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = webList,
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
