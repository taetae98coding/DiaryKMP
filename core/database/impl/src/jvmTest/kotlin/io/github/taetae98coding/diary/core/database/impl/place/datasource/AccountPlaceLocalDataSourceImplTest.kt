package io.github.taetae98coding.diary.core.database.impl.place.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
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

class AccountPlaceLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountPlaceLocalDataSourceImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl
        lateinit var syncTransaction: AccountPlaceSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountPlaceLocalDataSourceImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
            syncTransaction = AccountPlaceSyncTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun PagingSource<Int, PlaceLocalEntity>.pagedPlaces(loadSize: Int = 100): List<PlaceLocalEntity> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, PlaceLocalEntity>>().data
        }

        suspend fun pagedPlaces(accountId: Uuid): List<PlaceLocalEntity> = dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).pagedPlaces()

        test("TC-MEMO-PLACE-CARD-DOMAIN-010 삭제되지 않은 계정의 장소만 제목 오름차순으로 페이지 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPlace = place(title = FIRST_PLACE_TITLE)
            val lastPlace = place(title = LAST_PLACE_TITLE)
            val deletedPlace = place().copy(isDeleted = true)
            val otherAccountPlace = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(lastPlace, firstPlace, deletedPlace), placeTagList = emptyList())
            placeTransaction.upsert(accountId = otherAccountId, placeList = listOf(otherAccountPlace), placeTagList = emptyList())

            pagedPlaces(accountId) shouldBe listOf(firstPlace, lastPlace)
        }

        test("TC-PLACE-HOME-DOMAIN-010 노출 기준을 만족하는 장소만 보이는 영역 기준으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val insidePlace = place(latitude = 37.5, longitude = 127.0)
            val deletedPlace = place(latitude = 37.5, longitude = 127.0).copy(isDeleted = true)
            val outsidePlace = place(latitude = 50.0, longitude = 127.0)
            val otherAccountPlace = place(latitude = 37.5, longitude = 127.0)
            placeTransaction.upsert(accountId = accountId, placeList = listOf(insidePlace, deletedPlace, outsidePlace), placeTagList = emptyList())
            placeTransaction.upsert(accountId = otherAccountId, placeList = listOf(otherAccountPlace), placeTagList = emptyList())

            val result =
                dataSource
                    .get(accountId = accountId, south = 37.0, north = 38.0, west = 126.0, east = 128.0, sort = ListSortLocalEntity.DEFAULT)
                    .first()

            result shouldBe listOf(insidePlace)
        }

        test("TC-PLACE-HOME-DOMAIN-011 영역 경계 위의 좌표도 영역 안으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val boundaryPlaceList =
                listOf(
                    place(title = "A-south", latitude = 37.0, longitude = 127.0),
                    place(title = "B-north", latitude = 38.0, longitude = 127.0),
                    place(title = "C-west", latitude = 37.5, longitude = 126.0),
                    place(title = "D-east", latitude = 37.5, longitude = 128.0),
                )
            placeTransaction.upsert(accountId = accountId, placeList = boundaryPlaceList, placeTagList = emptyList())

            val result =
                dataSource
                    .get(accountId = accountId, south = 37.0, north = 38.0, west = 126.0, east = 128.0, sort = ListSortLocalEntity.DEFAULT)
                    .first()

            result shouldBe boundaryPlaceList
        }

        test("TC-PLACE-HOME-DOMAIN-012 날짜변경선을 걸친 영역은 양쪽 범위의 장소를 모두 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val westSidePlace = place(title = "A-west-side", latitude = 37.5, longitude = 179.0)
            val eastSidePlace = place(title = "B-east-side", latitude = 37.5, longitude = -179.0)
            val outsidePlace = place(title = "C-outside", latitude = 37.5, longitude = 0.0)
            placeTransaction.upsert(accountId = accountId, placeList = listOf(westSidePlace, eastSidePlace, outsidePlace), placeTagList = emptyList())

            val result =
                dataSource
                    .get(accountId = accountId, south = 37.0, north = 38.0, west = 178.0, east = -178.0, sort = ListSortLocalEntity.DEFAULT)
                    .first()

            result shouldBe listOf(westSidePlace, eastSidePlace)
        }

        test("TC-PLACE-HOME-DOMAIN-013 보이는 영역 기준 조회는 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPlace = place(title = FIRST_PLACE_TITLE, latitude = 37.5, longitude = 127.0)
            val lastPlace = place(title = LAST_PLACE_TITLE, latitude = 37.5, longitude = 127.0)
            placeTransaction.upsert(accountId = accountId, placeList = listOf(lastPlace, firstPlace), placeTagList = emptyList())

            val result =
                dataSource
                    .get(accountId = accountId, south = 37.0, north = 38.0, west = 126.0, east = 128.0, sort = ListSortLocalEntity.DEFAULT)
                    .first()

            result shouldBe listOf(firstPlace, lastPlace)
        }

        test("TC-PLACE-HOME-DATA-002 저장된 장소의 변화가 보이는 영역 기준 조회 결과에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place(latitude = 37.5, longitude = 127.0)

            dataSource.get(accountId = accountId, south = 37.0, north = 38.0, west = 126.0, east = 128.0, sort = ListSortLocalEntity.DEFAULT).test {
                awaitItem().shouldBeEmpty()

                placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
                awaitItem() shouldBe listOf(place)

                placeTransaction.upsert(accountId = accountId, placeList = listOf(place.copy(isDeleted = true)), placeTagList = emptyList())
                awaitItem().shouldBeEmpty()

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-PLACE-HOME-DATA-003 서버에서 내려받은 장소 변경이 보이는 영역 기준 조회에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pulledAt = Instant.fromEpochMilliseconds(2_000)
            val place =
                place(title = LAST_PLACE_TITLE, latitude = 10.0, longitude = 20.0)
                    .copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val renamedPlace = place.copy(detail = place.detail.copy(title = FIRST_PLACE_TITLE), updatedAt = pulledAt)
            val addedPlace =
                place(title = LAST_PLACE_TITLE, latitude = 15.0, longitude = 25.0)
                    .copy(updatedAt = pulledAt)
            val movedPlace = renamedPlace.copy(detail = renamedPlace.detail.copy(latitude = 80.0, longitude = 100.0), updatedAt = pulledAt)
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            dataSource.get(accountId = accountId, south = 0.0, north = 30.0, west = 0.0, east = 30.0, sort = ListSortLocalEntity.DEFAULT).test {
                awaitItem() shouldBe listOf(place)

                syncTransaction.save(accountId = accountId, placeList = listOf(renamedPlace), cursor = 1L)
                awaitItem() shouldBe listOf(renamedPlace)

                syncTransaction.save(accountId = accountId, placeList = listOf(addedPlace), cursor = 2L)
                awaitItem() shouldBe listOf(renamedPlace, addedPlace)

                syncTransaction.save(accountId = accountId, placeList = listOf(movedPlace), cursor = 3L)
                awaitItem() shouldBe listOf(addedPlace)

                syncTransaction.save(
                    accountId = accountId,
                    placeList = listOf(addedPlace.copy(isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(3_000))),
                    cursor = 4L,
                )
                awaitItem().shouldBeEmpty()

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-MEMO-PLACE-CARD-DOMAIN-020 검색어는 제목·설명·주소 중 하나 이상을 포함하는 장소만 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val titlePlace = place(title = "Riverside").withDetail(description = "", address = "")
            val descriptionPlace = place(title = "AlphaPlace").withDetail(description = "주차 가능", address = "")
            val addressPlace = place(title = "BetaPlace").withDetail(description = "", address = "서울 성수동")
            val otherPlace = place(title = "GammaPlace").withDetail(description = "", address = "")
            placeTransaction.upsert(accountId = accountId, placeList = listOf(titlePlace, descriptionPlace, addressPlace, otherPlace), placeTagList = emptyList())

            dataSource.page(accountId = accountId, query = "river", sort = ListSortLocalEntity.DEFAULT).pagedPlaces() shouldBe listOf(titlePlace)
            dataSource.page(accountId = accountId, query = "주차", sort = ListSortLocalEntity.DEFAULT).pagedPlaces() shouldBe listOf(descriptionPlace)
            dataSource.page(accountId = accountId, query = "성수동", sort = ListSortLocalEntity.DEFAULT).pagedPlaces() shouldBe listOf(addressPlace)
            dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).pagedPlaces() shouldBe
                listOf(descriptionPlace, addressPlace, otherPlace, titlePlace)
        }

        test("TC-MEMO-PLACE-CARD-DATA-003 검색어를 만족하는 장소가 정렬 뒤쪽에 있어도 첫 페이지에 담긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val placeList =
                List(PLACE_COUNT) { index ->
                    place(title = "Place-${index.toString().padStart(length = 3, padChar = '0')}").withDetail(description = "", address = "")
                }
            val target = place(title = "ZebraRiverside").withDetail(description = "", address = "")
            placeTransaction.upsert(accountId = accountId, placeList = placeList + target, placeTagList = emptyList())

            dataSource
                .page(accountId = accountId, query = "riverside", sort = ListSortLocalEntity.DEFAULT)
                .pagedPlaces(loadSize = PLACE_PAGE_SIZE) shouldBe listOf(target)
        }

        test("TC-MEMO-PLACE-CARD-DATA-001 저장된 장소의 변화가 페이지 조회에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val renamedPlace = place.copy(detail = place.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))

            suspend fun assertPageInvalidated(
                expected: List<PlaceLocalEntity>,
                change: suspend () -> Unit,
            ) {
                val pagingSource = dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT)
                pagingSource.pagedPlaces()
                val invalidated = CompletableDeferred<Unit>()
                pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

                change()

                withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
                pagingSource.invalid.shouldBeTrue()
                pagedPlaces(accountId) shouldBe expected
            }

            pagedPlaces(accountId).shouldBeEmpty()
            assertPageInvalidated(expected = listOf(place)) {
                placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            }
            assertPageInvalidated(expected = listOf(renamedPlace)) {
                placeTransaction.upsert(accountId = accountId, placeList = listOf(renamedPlace), placeTagList = emptyList())
            }
            assertPageInvalidated(expected = emptyList()) {
                placeTransaction.upsert(accountId = accountId, placeList = listOf(renamedPlace.copy(isDeleted = true)), placeTagList = emptyList())
            }
        }

        test("선택한 식별자 조회는 현재 계정의 삭제되지 않은 장소만 제목 오름차순으로 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPlace = place(title = FIRST_PLACE_TITLE)
            val lastPlace = place(title = LAST_PLACE_TITLE)
            val unselectedPlace = place()
            val deletedPlace = place().copy(isDeleted = true)
            val otherAccountPlace = place()
            placeTransaction.upsert(
                accountId = accountId,
                placeList = listOf(lastPlace, firstPlace, unselectedPlace, deletedPlace),
                placeTagList = emptyList(),
            )
            placeTransaction.upsert(accountId = otherAccountId, placeList = listOf(otherAccountPlace), placeTagList = emptyList())

            dataSource
                .get(
                    accountId = accountId,
                    placeIdSet = setOf(firstPlace.id, lastPlace.id, deletedPlace.id, otherAccountPlace.id),
                ).first() shouldBe listOf(firstPlace, lastPlace)
        }

        test("선택한 식별자가 없으면 빈 목록을 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place()), placeTagList = emptyList())

            dataSource.get(accountId = accountId, placeIdSet = emptySet()).first().shouldBeEmpty()
        }

        test("TC-PLACE-DETAIL-DOMAIN-001 대상 식별자 조회는 현재 계정의 장소만 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val otherAccountPlace = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            placeTransaction.upsert(accountId = otherAccountId, placeList = listOf(otherAccountPlace), placeTagList = emptyList())

            dataSource.find(accountId = accountId, placeId = place.id).first() shouldBe place
            dataSource.find(accountId = accountId, placeId = otherAccountPlace.id).first() shouldBe null
        }

        test("TC-PLACE-DETAIL-DOMAIN-020 대상 식별자 조회는 삭제 여부와 관계없이 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val deletedPlace = place().copy(isDeleted = true)
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place, deletedPlace), placeTagList = emptyList())

            dataSource.find(accountId = accountId, placeId = place.id).first() shouldBe place
            dataSource.find(accountId = accountId, placeId = deletedPlace.id).first() shouldBe deletedPlace
        }

        test("대상 식별자의 장소가 없으면 없는 것으로 전달한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place()), placeTagList = emptyList())

            dataSource.find(accountId = accountId, placeId = fixtureMonkey.giveMeOne<Uuid>()).first() shouldBe null
        }

        test("TC-PLACE-DETAIL-DATA-002 저장된 장소의 변화가 대상 식별자 조회 결과에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place(title = FIRST_PLACE_TITLE)
            val renamedPlace = place.copy(detail = place.detail.copy(title = LAST_PLACE_TITLE))
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            dataSource.find(accountId = accountId, placeId = place.id).test {
                awaitItem() shouldBe place

                placeTransaction.upsert(accountId = accountId, placeList = listOf(renamedPlace), placeTagList = emptyList())
                awaitItem() shouldBe renamedPlace

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-PLACE-DETAIL-DATA-006 수정한 내용이 보이는 영역 기준 조회에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place(title = FIRST_PLACE_TITLE, latitude = 10.0, longitude = 20.0)
            val movedDetail =
                place.detail.copy(
                    title = LAST_PLACE_TITLE,
                    latitude = 40.0,
                    longitude = 50.0,
                )
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            dataSource
                .get(accountId = accountId, south = 0.0, north = 30.0, west = 0.0, east = 30.0, sort = ListSortLocalEntity.DEFAULT)
                .test {
                    awaitItem() shouldBe listOf(place)

                    placeTransaction.updateDetail(
                        accountId = accountId,
                        placeId = place.id,
                        detail = movedDetail,
                        updatedAt = instant(),
                    )

                    awaitItem().shouldBeEmpty()

                    cancelAndIgnoreRemainingEvents()
                }

            dataSource
                .get(accountId = accountId, south = 30.0, north = 60.0, west = 30.0, east = 60.0, sort = ListSortLocalEntity.DEFAULT)
                .first()
                .map { local -> local.detail.title } shouldBe listOf(LAST_PLACE_TITLE)
        }

        test("TC-PLACE-DETAIL-DATA-007 삭제한 장소는 미삭제 장소만 노출하는 조회 결과에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())

            dataSource.get(accountId = accountId, placeIdSet = setOf(place.id)).test {
                awaitItem() shouldBe listOf(place)

                placeTransaction.updateDeleted(
                    accountId = accountId,
                    placeId = place.id,
                    isDeleted = true,
                    updatedAt = instant(),
                )
                awaitItem().shouldBeEmpty()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }) {
    public companion object {
        private const val FIRST_PLACE_TITLE = "ApplePlace"
        private const val LAST_PLACE_TITLE = "ZebraPlace"
        private const val INVALIDATION_TIMEOUT_MILLIS = 5_000L
        private const val PLACE_COUNT: Int = 25
        private const val PLACE_PAGE_SIZE: Int = 10

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            latitude: Double = fixtureMonkey.giveMeOne<Long>() % 90 + 0.5,
            longitude: Double = fixtureMonkey.giveMeOne<Long>() % 180 + 0.5,
        ): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::detail, placeDetail(title = title, latitude = latitude, longitude = longitude))
                .setExp(PlaceLocalEntity::isDeleted, false)
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun PlaceLocalEntity.withDetail(
            description: String,
            address: String,
        ): PlaceLocalEntity = copy(detail = detail.copy(description = description, address = address))

        private fun placeDetail(
            title: String,
            latitude: Double,
            longitude: Double,
        ): PlaceDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceDetailLocalEntity>()
                .setExp(PlaceDetailLocalEntity::title, title)
                .setExp(PlaceDetailLocalEntity::latitude, latitude)
                .setExp(PlaceDetailLocalEntity::longitude, longitude)
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
