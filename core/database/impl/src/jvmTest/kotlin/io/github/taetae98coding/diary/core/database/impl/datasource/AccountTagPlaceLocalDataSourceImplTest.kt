package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountPlaceTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountTagPlaceLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagPlaceLocalDataSourceImpl
        lateinit var placeTagTransaction: AccountPlaceTagTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountTagPlaceLocalDataSourceImpl(database = database)
            placeTagTransaction = AccountPlaceTagTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            tagTransaction.upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        suspend fun insertPlace(
            accountId: Uuid,
            vararg placeList: PlaceLocalEntity,
        ) {
            placeTransaction.upsert(accountId = accountId, placeList = placeList.toList(), placeTagList = emptyList())
        }

        suspend fun link(
            accountId: Uuid,
            placeId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            placeTagTransaction.upsert(
                accountId = accountId,
                placeId = placeId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = instant(),
            )
        }

        suspend fun pagedPlaceIdList(
            accountId: Uuid,
            tagId: Uuid,
            scope: TagScopeLocalEntity = TagScopeLocalEntity.SELF,
            loadSize: Int = 100,
        ): List<Uuid> =
            dataSource
                .page(accountId = accountId, tagId = tagId, scope = scope, sort = ListSortLocalEntity.DEFAULT)
                .pagedPlaceIdList(loadSize = loadSize)

        suspend fun boundsPlaceList(
            accountId: Uuid,
            tagId: Uuid,
            south: Double,
            north: Double,
            west: Double,
            east: Double,
            scope: TagScopeLocalEntity = TagScopeLocalEntity.SELF,
        ): List<PlaceLocalEntity> =
            dataSource
                .get(
                    accountId = accountId,
                    tagId = tagId,
                    scope = scope,
                    south = south,
                    north = north,
                    west = west,
                    east = east,
                    sort = ListSortLocalEntity.DEFAULT,
                ).first()

        test("TC-TAG-DETAIL-PLACE-DATA-006 현재 계정의 대상 태그와 활성 연결을 가진 미삭제 장소 중 보이는 영역 안의 장소만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val otherTag = tag()
            val insidePlace = place(title = "a-inside", latitude = 37.5, longitude = 127.0)
            val outsidePlace = place(title = "b-outside", latitude = 50.0, longitude = 127.0)
            val otherTagPlace = place(title = "c-other-tag", latitude = 37.5, longitude = 127.0)
            val unlinkedPlace = place(title = "d-unlinked", latitude = 37.5, longitude = 127.0)
            val deletedPlace = place(title = "e-deleted", latitude = 37.5, longitude = 127.0).copy(isDeleted = true)
            val otherAccountPlace = place(title = "f-other-account", latitude = 37.5, longitude = 127.0)

            insertTag(accountId, targetTag, otherTag)
            insertTag(otherAccountId, targetTag)
            insertPlace(accountId, insidePlace, outsidePlace, otherTagPlace, unlinkedPlace, deletedPlace)
            insertPlace(otherAccountId, otherAccountPlace)

            link(accountId = accountId, placeId = insidePlace.id, tagId = targetTag.id)
            link(accountId = accountId, placeId = outsidePlace.id, tagId = targetTag.id)
            link(accountId = accountId, placeId = otherTagPlace.id, tagId = otherTag.id)
            link(accountId = accountId, placeId = unlinkedPlace.id, tagId = targetTag.id, isDeleted = true)
            link(accountId = accountId, placeId = deletedPlace.id, tagId = targetTag.id)
            link(accountId = otherAccountId, placeId = otherAccountPlace.id, tagId = targetTag.id)

            val result =
                boundsPlaceList(
                    accountId = accountId,
                    tagId = targetTag.id,
                    south = 37.0,
                    north = 38.0,
                    west = 126.0,
                    east = 128.0,
                )

            result shouldBe listOf(insidePlace)
        }

        test("TC-TAG-DETAIL-PLACE-DATA-006 보이는 영역 안의 장소는 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val firstPlace = place(title = "a-place", latitude = 37.5, longitude = 127.0)
            val secondPlace = place(title = "b-place", latitude = 37.6, longitude = 127.1)
            val thirdPlace = place(title = "c-place", latitude = 37.7, longitude = 127.2)

            insertTag(accountId, targetTag)
            insertPlace(accountId, thirdPlace, firstPlace, secondPlace)
            listOf(firstPlace, secondPlace, thirdPlace).forEach { place ->
                link(accountId = accountId, placeId = place.id, tagId = targetTag.id)
            }

            val result =
                boundsPlaceList(
                    accountId = accountId,
                    tagId = targetTag.id,
                    south = 37.0,
                    north = 38.0,
                    west = 126.0,
                    east = 128.0,
                )

            result.map { place -> place.id } shouldBe listOf(firstPlace.id, secondPlace.id, thirdPlace.id)
        }

        test("TC-TAG-DETAIL-PLACE-DATA-007 날짜변경선을 걸친 영역은 양쪽 범위의 장소를 모두 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val westSidePlace = place(title = "a-west-side", latitude = 37.5, longitude = 179.0)
            val eastSidePlace = place(title = "b-east-side", latitude = 37.5, longitude = -179.0)
            val outsidePlace = place(title = "c-outside", latitude = 37.5, longitude = 0.0)

            insertTag(accountId, targetTag)
            insertPlace(accountId, westSidePlace, eastSidePlace, outsidePlace)
            listOf(westSidePlace, eastSidePlace, outsidePlace).forEach { place ->
                link(accountId = accountId, placeId = place.id, tagId = targetTag.id)
            }

            val result =
                boundsPlaceList(
                    accountId = accountId,
                    tagId = targetTag.id,
                    south = 37.0,
                    north = 38.0,
                    west = 178.0,
                    east = -178.0,
                )

            result.map { place -> place.id } shouldBe listOf(westSidePlace.id, eastSidePlace.id)
        }

        test("TC-TAG-DETAIL-PLACE-DATA-001 현재 계정의 대상 태그와 활성 연결을 가진 미삭제 장소만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val otherTag = tag()
            val linkedPlace = place(title = "a-linked")
            val otherTagPlace = place(title = "b-other-tag")
            val unlinkedPlace = place(title = "c-unlinked")
            val deletedPlace = place(title = "d-deleted").copy(isDeleted = true)
            val otherAccountPlace = place(title = "e-other-account")

            insertTag(accountId, targetTag, otherTag)
            insertTag(otherAccountId, targetTag)
            insertPlace(accountId, linkedPlace, otherTagPlace, unlinkedPlace, deletedPlace)
            insertPlace(otherAccountId, otherAccountPlace)

            link(accountId = accountId, placeId = linkedPlace.id, tagId = targetTag.id)
            link(accountId = accountId, placeId = otherTagPlace.id, tagId = otherTag.id)
            link(accountId = accountId, placeId = unlinkedPlace.id, tagId = targetTag.id, isDeleted = true)
            link(accountId = accountId, placeId = deletedPlace.id, tagId = targetTag.id)
            link(accountId = otherAccountId, placeId = otherAccountPlace.id, tagId = targetTag.id)

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(linkedPlace.id)
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-002 태그별 장소은 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val firstPlace = place(title = "a-place")
            val secondPlace = place(title = "b-place")
            val thirdPlace = place(title = "c-place")

            insertTag(accountId, targetTag)
            insertPlace(accountId, thirdPlace, firstPlace, secondPlace)
            listOf(firstPlace, secondPlace, thirdPlace).forEach { value ->
                link(accountId = accountId, placeId = value.id, tagId = targetTag.id)
            }

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe
                listOf(firstPlace.id, secondPlace.id, thirdPlace.id)
        }

        test("TC-TAG-DETAIL-PLACE-DATA-002 태그별 장소을 페이지 단위로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val placeList = List(3) { index -> place(title = "place-$index") }

            insertTag(accountId, targetTag)
            insertPlace(accountId, *placeList.toTypedArray())
            placeList.forEach { value -> link(accountId = accountId, placeId = value.id, tagId = targetTag.id) }

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id, loadSize = 2) shouldBe
                placeList.take(2).map { value -> value.id }
        }

        test("TC-TAG-DETAIL-PLACE-DATA-003 연결을 해제하면 목록에서 사라지고 복구하면 다시 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetPlace = place()

            insertTag(accountId, targetTag)
            insertPlace(accountId, targetPlace)
            link(accountId = accountId, placeId = targetPlace.id, tagId = targetTag.id)

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetPlace.id)

            link(accountId = accountId, placeId = targetPlace.id, tagId = targetTag.id, isDeleted = true)

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()

            link(accountId = accountId, placeId = targetPlace.id, tagId = targetTag.id)

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetPlace.id)
        }

        test("TC-TAG-DETAIL-PLACE-DATA-003 장소을 삭제하면 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val targetPlace = place()

            insertTag(accountId, targetTag)
            insertPlace(accountId, targetPlace)
            link(accountId = accountId, placeId = targetPlace.id, tagId = targetTag.id)
            insertPlace(accountId, targetPlace.copy(isDeleted = true))

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
        }

        test("TC-TAG-DETAIL-PLACE-DATA-003 장소의 제목을 바꾸면 바뀐 제목의 정렬 위치로 이동한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val targetTag = tag()
            val firstPlace = place(title = "a-place")
            val secondPlace = place(title = "b-place")

            insertTag(accountId, targetTag)
            insertPlace(accountId, firstPlace, secondPlace)
            listOf(firstPlace, secondPlace).forEach { value ->
                link(accountId = accountId, placeId = value.id, tagId = targetTag.id)
            }

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(firstPlace.id, secondPlace.id)

            insertPlace(accountId, firstPlace.copy(detail = firstPlace.detail.copy(title = "z-place")))

            pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(secondPlace.id, firstPlace.id)
        }

        test("TC-TAG-DETAIL-PLACE-DOMAIN-001 태그를 완료하거나 삭제해도 연결된 장소은 계속 조회된다") {
            listOf(
                true to false,
                false to true,
            ).forEach { (isFinished, isDeleted) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val targetTag = tag()
                val targetPlace = place()

                insertTag(accountId, targetTag)
                insertPlace(accountId, targetPlace)
                link(accountId = accountId, placeId = targetPlace.id, tagId = targetTag.id)
                insertTag(accountId, targetTag.copy(isFinished = isFinished, isDeleted = isDeleted))

                pagedPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(targetPlace.id)
            }
        }

        test("TC-TAG-DETAIL-PLACE-DATA-004 장소는 저장 시점의 태그 선택으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagA = tag()
            val tagB = tag()
            insertTag(accountId, tagA, tagB)

            listOf(
                listOf(tagA) to true,
                listOf(tagA, tagB) to true,
                listOf(tagB) to false,
            ).forEach { (selectedTagList, isShownInTagATab) ->
                val entity = place()

                placeTransaction.upsert(
                    accountId = accountId,
                    placeList = listOf(entity),
                    placeTagList =
                        selectedTagList.map { value ->
                            PlaceTagLocalEntity(
                                placeId = entity.id,
                                tagId = value.id,
                                isDeleted = false,
                                updatedAt = instant(),
                                createdAt = instant(),
                            )
                        },
                )

                pagedPlaceIdList(accountId = accountId, tagId = tagA.id).contains(entity.id) shouldBe isShownInTagATab
                selectedTagList.forEach { value ->
                    pagedPlaceIdList(accountId = accountId, tagId = value.id).contains(entity.id) shouldBe true
                }
            }
        }

        test("TC-TAG-DETAIL-PLACE-DOMAIN-002 태그마다 조회되는 장소이 다르다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val firstPlace = place()
            val secondPlace = place()

            insertTag(accountId, firstTag, secondTag)
            insertPlace(accountId, firstPlace, secondPlace)
            link(accountId = accountId, placeId = firstPlace.id, tagId = firstTag.id)
            link(accountId = accountId, placeId = secondPlace.id, tagId = secondTag.id)

            pagedPlaceIdList(accountId = accountId, tagId = firstTag.id) shouldBe listOf(firstPlace.id)
            pagedPlaceIdList(accountId = accountId, tagId = secondTag.id) shouldBe listOf(secondPlace.id)
        }
    }) {
    companion object {
        private suspend fun PagingSource<Int, PlaceLocalEntity>.pagedPlaceIdList(loadSize: Int): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, PlaceLocalEntity>>().data.map { place -> place.id }
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
                .copy(isFinished = false, isDeleted = false)

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
    }
}
