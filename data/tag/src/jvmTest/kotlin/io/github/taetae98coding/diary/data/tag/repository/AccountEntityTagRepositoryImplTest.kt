package io.github.taetae98coding.diary.data.tag.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.entitytag.datasource.AccountEntityTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.transaction.AccountEntityTagTransaction
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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

private val kindPairList: List<Pair<EntityTagKind, EntityTagKindLocalEntity>> =
    listOf(
        EntityTagKind.WEB to EntityTagKindLocalEntity.WEB,
        EntityTagKind.PLACE to EntityTagKindLocalEntity.PLACE,
        EntityTagKind.CONTACT to EntityTagKindLocalEntity.CONTACT,
        EntityTagKind.CHECKLIST to EntityTagKindLocalEntity.CHECKLIST,
        EntityTagKind.MUSIC to EntityTagKindLocalEntity.MUSIC,
        EntityTagKind.QR to EntityTagKindLocalEntity.QR,
        EntityTagKind.DDAY to EntityTagKindLocalEntity.DDAY,
    )

class AccountEntityTagRepositoryImplTest :
    FunSpec({
        test("종류 대응표는 모든 종류를 한 번씩 담는다") {
            kindPairList.map { (kind, _) -> kind } shouldBe kindPairList.map { (kind, _) -> kind }.distinct()
            kindPairList.map { (kind, _) -> kind }.toSet() shouldBe EntityTagKind.entries.toSet()
            kindPairList.map { (_, local) -> local }.toSet() shouldBe EntityTagKindLocalEntity.entries.toSet()
        }

        kindPairList.forEach { (kind, localKind) ->
            test("${kind.name} 항목의 연결된 태그 조회는 같은 종류와 현재 계정의 로컬 태그를 도메인 모델로 변환해 전달한다") {
                val account = Arb.accountUser().next()
                val entityId = Arb.kotlinUuid().next()
                val localTagList = List(2) { localTag() }
                val localDataSource = mockk<AccountEntityTagLocalDataSource>()
                every {
                    localDataSource.getTagList(accountId = account.id, kind = localKind, entityId = entityId)
                } returns flowOf(localTagList)
                val repository = AccountEntityTagRepositoryImpl(localDataSource, mockk())

                repository.getTagList(account = account, kind = kind, entityId = entityId).first() shouldBe
                    localTagList.map { local -> local.toDomain() }

                verify(exactly = 1) { localDataSource.getTagList(accountId = account.id, kind = localKind, entityId = entityId) }
            }

            test("${kind.name} 항목에 연결할 수 있는 태그 페이지 조회는 검색어와 같은 종류로 조회한 로컬 태그를 도메인 모델로 변환해 전달한다") {
                val account = Arb.accountUser().next()
                val entityId = Arb.kotlinUuid().next()
                val query = Arb.string().next()
                val localTagList = List(2) { localTag() }
                val localDataSource = mockk<AccountEntityTagLocalDataSource>()
                every {
                    localDataSource.pageSelectableTag(accountId = account.id, kind = localKind, entityId = entityId, query = query)
                } returns pagingSource(localTagList)
                val repository = AccountEntityTagRepositoryImpl(localDataSource, mockk())

                repository.pageSelectableTag(account = account, kind = kind, entityId = entityId, query = query).first().items() shouldBe
                    localTagList.map { local -> local.toDomain() }

                verify(exactly = 1) {
                    localDataSource.pageSelectableTag(accountId = account.id, kind = localKind, entityId = entityId, query = query)
                }
            }

            test("${kind.name} 항목의 연결 저장은 같은 종류와 현재 계정으로 로컬 트랜잭션에 위임한다") {
                listOf(true, false).forEach { isDeleted ->
                    val account = Arb.accountUser().next()
                    val entityId = Arb.kotlinUuid().next()
                    val tagId = Arb.kotlinUuid().next()
                    val updatedAt = Arb.secondPrecisionInstant().next()
                    val transaction = mockk<AccountEntityTagTransaction>()
                    coEvery {
                        transaction.upsert(
                            accountId = account.id,
                            kind = localKind,
                            entityId = entityId,
                            tagId = tagId,
                            isDeleted = isDeleted,
                            updatedAt = updatedAt,
                        )
                    } just Runs
                    val repository = AccountEntityTagRepositoryImpl(mockk(), transaction)

                    repository.upsert(
                        account = account,
                        kind = kind,
                        entityId = entityId,
                        tagId = tagId,
                        isDeleted = isDeleted,
                        updatedAt = updatedAt,
                    )

                    coVerify(exactly = 1) {
                        transaction.upsert(
                            accountId = account.id,
                            kind = localKind,
                            entityId = entityId,
                            tagId = tagId,
                            isDeleted = isDeleted,
                            updatedAt = updatedAt,
                        )
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun localTag(): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

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
