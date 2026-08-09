package io.github.taetae98coding.diary.data.place.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceTransaction
import io.github.taetae98coding.diary.core.mapper.place.toDomain
import io.github.taetae98coding.diary.core.mapper.place.toLocal
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountPlaceRepositoryImplTest :
    FunSpec({
        test("TC-PLACE-ADD-DATA-001 장소를 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val place = place()
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            coEvery { transaction.upsert(accountId = account.id, placeList = listOf(place.toLocal()), placeTagList = emptyList()) } just Runs
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            repository.upsert(account = account, place = place, tagIdSet = emptySet())

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, placeList = listOf(place.toLocal()), placeTagList = emptyList())
            }
        }

        test("TC-PLACE-ADD-DATA-004 로컬 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val place = place()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            coEvery { transaction.upsert(accountId = account.id, placeList = listOf(place.toLocal()), placeTagList = emptyList()) } throws throwable
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.upsert(account = account, place = place, tagIdSet = emptySet())
            } shouldBeSameInstanceAs throwable
        }

        test("장소 목록 페이지 조회는 검색어와 현재 계정으로 조회한 로컬 장소를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val query = fixtureMonkey.giveMeOne<String>()
            val localPlaceList = List(2) { localPlace() }
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            every { localDataSource.page(accountId = account.id, query = query, sort = ListSortLocalEntity.TITLE) } returns pagingSource(localPlaceList)
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            repository.page(account = account, query = query, sort = ListSort.TITLE).first().items() shouldBe localPlaceList.map { local -> local.toDomain() }
        }

        test("선택한 장소 조회는 선택한 식별자를 그대로 전달하고 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localPlaceList = List(2) { localPlace() }
            val placeIdSet = localPlaceList.mapTo(mutableSetOf()) { local -> local.id }
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            every { localDataSource.get(accountId = account.id, placeIdSet = placeIdSet) } returns flowOf(localPlaceList)
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            repository.get(account = account, placeIdSet = placeIdSet).first() shouldBe localPlaceList.map { local -> local.toDomain() }
        }

        test("TC-PLACE-HOME-DATA-001 계정과 보이는 영역을 기준으로 로컬 저장소에서 장소를 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val bounds = fixtureMonkey.giveMeOne<CoordinateBounds>()
            val localPlaceList = List(2) { localPlace() }
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            every {
                localDataSource.get(
                    accountId = account.id,
                    south = bounds.south,
                    north = bounds.north,
                    west = bounds.west,
                    east = bounds.east,
                    sort = ListSortLocalEntity.TITLE,
                )
            } returns flowOf(localPlaceList)
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            repository.get(account = account, bounds = bounds, sort = ListSort.TITLE).first() shouldBe localPlaceList.map { local -> local.toDomain() }
        }

        test("TC-PLACE-DETAIL-DATA-001 계정과 대상 식별자를 기준으로 로컬 저장소에서 장소를 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localPlace = localPlace()
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            every { localDataSource.find(accountId = account.id, placeId = localPlace.id) } returns flowOf(localPlace)
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            repository.find(account = account, placeId = localPlace.id).first() shouldBe localPlace.toDomain()
        }

        test("TC-PLACE-DETAIL-DOMAIN-001 조회되지 않는 대상 장소는 없는 것으로 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            every { localDataSource.find(accountId = account.id, placeId = placeId) } returns flowOf(null)
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            repository.find(account = account, placeId = placeId).first() shouldBe null
        }

        test("TC-PLACE-DETAIL-DATA-003 수정 내용을 로컬 모델로 변환해 저장하고 반영 건수를 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val place = place()
            val updatedAt = instant()
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            coEvery {
                transaction.updateDetail(
                    accountId = account.id,
                    placeId = place.id,
                    detail = place.detail.toLocal(),
                    updatedAt = updatedAt,
                )
            } returns 1
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            val count =
                repository.updateDetail(
                    account = account,
                    placeId = place.id,
                    detail = place.detail,
                    updatedAt = updatedAt,
                )

            count shouldBe 1
            coVerify(exactly = 1) {
                transaction.updateDetail(
                    accountId = account.id,
                    placeId = place.id,
                    detail = place.detail.toLocal(),
                    updatedAt = updatedAt,
                )
            }
        }

        test("TC-PLACE-DETAIL-DATA-004 삭제 상태를 저장하고 반영 건수를 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val updatedAt = instant()
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            coEvery {
                transaction.updateDeleted(
                    accountId = account.id,
                    placeId = placeId,
                    isDeleted = true,
                    updatedAt = updatedAt,
                )
            } returns 1
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            val count =
                repository.updateDeleted(
                    account = account,
                    placeId = placeId,
                    isDeleted = true,
                    updatedAt = updatedAt,
                )

            count shouldBe 1
            coVerify(exactly = 1) {
                transaction.updateDeleted(
                    accountId = account.id,
                    placeId = placeId,
                    isDeleted = true,
                    updatedAt = updatedAt,
                )
            }
        }

        test("TC-PLACE-DETAIL-DATA-005 수정과 삭제의 로컬 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val place = place()
            val updatedAt = instant()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountPlaceLocalDataSource>()
            val transaction = mockk<AccountPlaceTransaction>()
            coEvery {
                transaction.updateDetail(accountId = any(), placeId = any(), detail = any(), updatedAt = any())
            } throws throwable
            coEvery {
                transaction.updateDeleted(accountId = any(), placeId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val repository = AccountPlaceRepositoryImpl(accountPlaceLocalDataSource = localDataSource, accountPlaceTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateDetail(
                    account = account,
                    placeId = place.id,
                    detail = place.detail,
                    updatedAt = updatedAt,
                )
            } shouldBeSameInstanceAs throwable
            shouldThrow<IllegalStateException> {
                repository.updateDeleted(
                    account = account,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = updatedAt,
                )
            } shouldBeSameInstanceAs throwable
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): Place =
            fixtureMonkey
                .giveMeKotlinBuilder<Place>()
                .setExp(Place::updatedAt, instant())
                .setExp(Place::createdAt, instant())
                .sample()

        private fun localPlace(): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun pagingSource(placeList: List<PlaceLocalEntity>): PagingSource<Int, PlaceLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = placeList,
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
