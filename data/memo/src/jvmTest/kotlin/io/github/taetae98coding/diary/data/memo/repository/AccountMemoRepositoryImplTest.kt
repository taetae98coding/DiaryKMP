package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.data.memo.mapper.toLocal
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountMemoRepositoryImplTest :
    FunSpec({
        test("단일 메모 조회는 로컬 저장소의 후속 변경도 도메인 모델로 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memo = memo()
            val changedMemo = memo.copy(detail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>())
            val memoFlow = MutableStateFlow<MemoLocalEntity?>(memo)
            val localDataSource = mockk<AccountMemoLocalDataSource>()
            val transaction = mockk<AccountMemoTransaction>()
            every { localDataSource.find(accountId = account.id, memoId = memo.id) } returns memoFlow
            val repository = AccountMemoRepositoryImpl(accountMemoLocalDataSource = localDataSource, accountMemoTransaction = transaction)

            repository.find(account = account, memoId = memo.id).test {
                awaitItem() shouldBe memo.toDomain()

                memoFlow.value = changedMemo

                awaitItem() shouldBe changedMemo.toDomain()
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-MEMO-TAG-DATA-001 메모 저장은 연결할 태그를 메모와 같은 시각의 연결로 변환해 함께 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memo = memo().toDomain()
            val tagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())
            val localDataSource = mockk<AccountMemoLocalDataSource>()
            val transaction = mockk<AccountMemoTransaction>()
            coEvery { transaction.upsert(accountId = any(), memoList = any(), memoTagList = any()) } just Runs
            val repository = AccountMemoRepositoryImpl(accountMemoLocalDataSource = localDataSource, accountMemoTransaction = transaction)

            repository.upsert(account = account, memo = memo, tagIdSet = tagIdSet)

            coVerify(exactly = 1) {
                transaction.upsert(
                    accountId = account.id,
                    memoList = listOf(memo.toLocal()),
                    memoTagList =
                        match { memoTagList ->
                            memoTagList.map { memoTag -> memoTag.tagId }.toSet() == tagIdSet &&
                                memoTagList.all { memoTag ->
                                    memoTag.memoId == memo.id &&
                                        !memoTag.isDeleted &&
                                        memoTag.updatedAt == memo.updatedAt &&
                                        memoTag.createdAt == memo.createdAt
                                }
                        },
                )
            }
        }

        test("연결할 태그가 없으면 태그 연결 없이 메모만 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memo = memo().toDomain()
            val localDataSource = mockk<AccountMemoLocalDataSource>()
            val transaction = mockk<AccountMemoTransaction>()
            coEvery { transaction.upsert(accountId = any(), memoList = any(), memoTagList = any()) } just Runs
            val repository = AccountMemoRepositoryImpl(accountMemoLocalDataSource = localDataSource, accountMemoTransaction = transaction)

            repository.upsert(account = account, memo = memo, tagIdSet = emptySet())

            coVerify(exactly = 1) {
                transaction.upsert(
                    accountId = account.id,
                    memoList = listOf(memo.toLocal()),
                    memoTagList = emptyList(),
                )
            }
        }

        test("메모 저장은 연결할 장소를 메모와 같은 시각의 연결로 변환해 함께 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memo = memo().toDomain()
            val placeIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())
            val localDataSource = mockk<AccountMemoLocalDataSource>()
            val transaction = mockk<AccountMemoTransaction>()
            coEvery { transaction.upsert(accountId = any(), memoList = any(), memoTagList = any(), memoPlaceList = any()) } just Runs
            val repository = AccountMemoRepositoryImpl(accountMemoLocalDataSource = localDataSource, accountMemoTransaction = transaction)

            repository.upsert(account = account, memo = memo, tagIdSet = emptySet(), placeIdSet = placeIdSet)

            coVerify(exactly = 1) {
                transaction.upsert(
                    accountId = account.id,
                    memoList = listOf(memo.toLocal()),
                    memoTagList = emptyList(),
                    memoPlaceList =
                        match { memoPlaceList ->
                            memoPlaceList.map { memoPlace -> memoPlace.placeId }.toSet() == placeIdSet &&
                                memoPlaceList.all { memoPlace ->
                                    memoPlace.memoId == memo.id &&
                                        !memoPlace.isDeleted &&
                                        memoPlace.updatedAt == memo.updatedAt &&
                                        memoPlace.createdAt == memo.createdAt
                                }
                        },
                )
            }
        }

        test("연결할 장소가 없으면 장소 연결 없이 메모만 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memo = memo().toDomain()
            val localDataSource = mockk<AccountMemoLocalDataSource>()
            val transaction = mockk<AccountMemoTransaction>()
            coEvery { transaction.upsert(accountId = any(), memoList = any(), memoTagList = any(), memoPlaceList = any()) } just Runs
            val repository = AccountMemoRepositoryImpl(accountMemoLocalDataSource = localDataSource, accountMemoTransaction = transaction)

            repository.upsert(account = account, memo = memo, tagIdSet = emptySet(), placeIdSet = emptySet())

            coVerify(exactly = 1) {
                transaction.upsert(
                    accountId = account.id,
                    memoList = listOf(memo.toLocal()),
                    memoTagList = emptyList(),
                    memoPlaceList = emptyList(),
                )
            }
        }

        test("메모 상세 저장은 도메인 상세를 로컬 엔티티로 변환해 로컬 저장소에 위임하고 갱신된 메모 수를 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = fixtureMonkey.giveMeOne<MemoDetail>()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val localDataSource = mockk<AccountMemoLocalDataSource>()
            val transaction = mockk<AccountMemoTransaction>()
            coEvery {
                transaction.updateDetail(accountId = account.id, memoId = memoId, detail = detail.toLocal(), updatedAt = updatedAt)
            } returns 1
            val repository = AccountMemoRepositoryImpl(accountMemoLocalDataSource = localDataSource, accountMemoTransaction = transaction)

            repository.updateDetail(account = account, memoId = memoId, detail = detail, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 1) {
                transaction.updateDetail(accountId = account.id, memoId = memoId, detail = detail.toLocal(), updatedAt = updatedAt)
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
