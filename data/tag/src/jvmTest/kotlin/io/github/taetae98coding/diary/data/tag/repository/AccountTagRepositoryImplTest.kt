package io.github.taetae98coding.diary.data.tag.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagTransaction
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.mapper.tag.toDomain
import io.github.taetae98coding.diary.core.mapper.tag.toLocal
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagRepositoryImplTest :
    FunSpec({
        test("태그 목록 페이지 조회는 검색어와 현재 계정으로 조회한 로컬 태그를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val query = fixtureMonkey.giveMeOne<String>()
            val localTagList = List(2) { localTag() }
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            every { localDataSource.page(accountId = account.id, query = query, sort = ListSortLocalEntity.TITLE) } returns pagingSource(localTagList)
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.page(account = account, query = query, sort = ListSort.TITLE).first().items() shouldBe localTagList.map { tag -> tag.toDomain() }

            verify(exactly = 1) { localDataSource.page(accountId = account.id, query = query, sort = ListSortLocalEntity.TITLE) }
        }

        test("TC-TAG-HOME-DATA-006 최상위 태그 목록 페이지 조회는 현재 계정의 로컬 태그를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localTagList = List(2) { localTag() }
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            every { localDataSource.pageTopLevel(accountId = account.id, sort = ListSortLocalEntity.TITLE) } returns pagingSource(localTagList)
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.pageTopLevel(account = account, sort = ListSort.TITLE).first().items() shouldBe localTagList.map { tag -> tag.toDomain() }

            verify(exactly = 1) { localDataSource.pageTopLevel(accountId = account.id, sort = ListSortLocalEntity.TITLE) }
            verify(exactly = 0) { localDataSource.page(accountId = any(), query = any(), sort = any()) }
        }

        test("완료된 태그 목록 페이지 조회는 현재 계정의 로컬 태그를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localTagList = List(2) { localTag() }
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            every { localDataSource.pageFinished(accountId = account.id, sort = ListSortLocalEntity.TITLE) } returns pagingSource(localTagList)
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.pageFinished(account = account, sort = ListSort.TITLE).first().items() shouldBe localTagList.map { tag -> tag.toDomain() }

            verify(exactly = 1) { localDataSource.pageFinished(accountId = account.id, sort = ListSortLocalEntity.TITLE) }
        }

        test("태그를 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tag = tag()
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            coEvery {
                transaction.upsert(accountId = account.id, tagList = listOf(tag.toLocal()), tagLinkList = emptyList())
            } just Runs
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.upsert(account = account, tag = tag, linkedTagIdSet = emptySet())

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, tagList = listOf(tag.toLocal()), tagLinkList = emptyList())
            }
        }

        test("TC-TAG-ADD-DATA-006 태그와 연결을 한 번의 저장 작업으로 함께 반영한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tag = tag()
            val firstToTagId = fixtureMonkey.giveMeOne<Uuid>()
            val secondToTagId = fixtureMonkey.giveMeOne<Uuid>()
            val tagLinkListSlot = slot<List<TagLinkLocalEntity>>()
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            coEvery {
                transaction.upsert(
                    accountId = account.id,
                    tagList = listOf(tag.toLocal()),
                    tagLinkList = capture(tagLinkListSlot),
                )
            } just Runs
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.upsert(account = account, tag = tag, linkedTagIdSet = setOf(firstToTagId, secondToTagId))

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, tagList = listOf(tag.toLocal()), tagLinkList = any())
            }
            tagLinkListSlot.captured shouldContainExactlyInAnyOrder
                listOf(firstToTagId, secondToTagId).map { toTagId ->
                    TagLinkLocalEntity(
                        fromTagId = tag.id,
                        toTagId = toTagId,
                        isDeleted = false,
                        updatedAt = tag.updatedAt,
                        createdAt = tag.createdAt,
                    )
                }
        }

        test("단일 태그 조회는 로컬 저장소의 후속 변경도 도메인 모델로 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tag = localTag()
            val changedTag = tag.copy(detail = fixtureMonkey.giveMeOne<TagDetailLocalEntity>())
            val tagFlow = MutableStateFlow<TagLocalEntity?>(tag)
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            every { localDataSource.find(accountId = account.id, tagId = tag.id) } returns tagFlow
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.find(account = account, tagId = tag.id).test {
                awaitItem() shouldBe tag.toDomain()

                tagFlow.value = changedTag

                awaitItem() shouldBe changedTag.toDomain()
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("태그 상세 저장은 도메인 상세를 로컬 엔티티로 변환해 로컬 저장소에 위임하고 갱신된 태그 수를 반환한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = fixtureMonkey.giveMeOne<TagDetail>()
            val updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val localDataSource = mockk<AccountTagLocalDataSource>()
            val transaction = mockk<AccountTagTransaction>()
            coEvery {
                transaction.updateDetail(accountId = account.id, tagId = tagId, detail = detail.toLocal(), updatedAt = updatedAt)
            } returns 1
            val repository = AccountTagRepositoryImpl(accountTagLocalDataSource = localDataSource, accountTagTransaction = transaction)

            repository.updateDetail(account = account, tagId = tagId, detail = detail, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 1) {
                transaction.updateDetail(accountId = account.id, tagId = tagId, detail = detail.toLocal(), updatedAt = updatedAt)
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun localTag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(TagLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
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
