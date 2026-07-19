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
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountPlaceTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountPlaceTagLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountPlaceTagLocalDataSourceImpl
        lateinit var tagPlaceDataSource: AccountTagPlaceLocalDataSourceImpl
        lateinit var placeTagTransaction: AccountPlaceTagTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl
        lateinit var placeDataSource: AccountPlaceLocalDataSourceImpl
        lateinit var placeSyncDataSource: AccountPlaceSyncLocalDataSourceImpl
        lateinit var placeTagSyncDataSource: AccountPlaceTagSyncLocalDataSourceImpl
        lateinit var placeSyncTransaction: AccountPlaceSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountPlaceTagLocalDataSourceImpl(database = database)
            tagPlaceDataSource = AccountTagPlaceLocalDataSourceImpl(database = database)
            placeTagTransaction = AccountPlaceTagTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
            placeDataSource = AccountPlaceLocalDataSourceImpl(database = database)
            placeSyncDataSource = AccountPlaceSyncLocalDataSourceImpl(database = database)
            placeTagSyncDataSource = AccountPlaceTagSyncLocalDataSourceImpl(database = database)
            placeSyncTransaction = AccountPlaceSyncTransactionImpl(database = database)
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
        ) {
            placeTagTransaction.upsert(
                accountId = accountId,
                placeId = placeId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = instant(),
            )
        }

        suspend fun unlink(
            accountId: Uuid,
            placeId: Uuid,
            tagId: Uuid,
        ) {
            placeTagTransaction.upsert(
                accountId = accountId,
                placeId = placeId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = instant(),
            )
        }

        suspend fun linkedTagList(
            accountId: Uuid,
            placeId: Uuid,
        ): List<TagLocalEntity> = dataSource.getTagList(accountId = accountId, placeId = placeId).first()

        suspend fun linkedTagIdList(
            accountId: Uuid,
            placeId: Uuid,
        ): List<Uuid> = linkedTagList(accountId = accountId, placeId = placeId).map { tag -> tag.id }

        suspend fun selectableTagIdList(
            accountId: Uuid,
            placeId: Uuid,
            query: String = "",
        ): List<Uuid> =
            dataSource
                .pageSelectableTag(accountId = accountId, placeId = placeId, query = query)
                .pagedTagIdList()

        suspend fun tagPlaceIdList(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            tagPlaceDataSource
                .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                .pagedPlaceIdList()

        suspend fun pinPlaceIdList(accountId: Uuid): List<Uuid> =
            placeDataSource
                .get(
                    accountId = accountId,
                    south = -90.0,
                    north = 90.0,
                    west = -180.0,
                    east = 180.0,
                    sort = ListSortLocalEntity.DEFAULT,
                ).first()
                .map { place -> place.id }

        test("TC-PLACE-TAG-DOMAIN-001 하나의 장소에 여러 태그를 연결할 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstTag = tag()
            val secondTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, firstTag, secondTag)

            link(accountId = accountId, placeId = place.id, tagId = firstTag.id)
            link(accountId = accountId, placeId = place.id, tagId = secondTag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldContainExactlyInAnyOrder
                listOf(firstTag.id, secondTag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-002 하나의 태그를 여러 장소에 연결할 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPlace = place()
            val secondPlace = place()
            val tag = tag()
            insertPlace(accountId, firstPlace, secondPlace)
            insertTag(accountId, tag)

            link(accountId = accountId, placeId = firstPlace.id, tagId = tag.id)
            link(accountId = accountId, placeId = secondPlace.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, placeId = firstPlace.id) shouldBe listOf(tag.id)
            linkedTagIdList(accountId = accountId, placeId = secondPlace.id) shouldBe listOf(tag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-003 같은 장소와 태그의 연결은 중복되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)

            link(accountId = accountId, placeId = place.id, tagId = tag.id)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-004 연결된 태그가 없는 장소도 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertPlace(accountId, place)

            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-PLACE-TAG-DOMAIN-005 연결은 장소와 태그의 내용을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)

            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            database.accountPlaceDao().find(accountId = accountId, placeId = place.id).first() shouldBe place
            database.accountTagDao().find(accountId = accountId, tagId = tag.id).first() shouldBe tag
        }

        test("TC-PLACE-TAG-DOMAIN-006 계정과 연결되지 않은 태그는 연결된 태그로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val otherAccountTag = tag()
            insertPlace(accountId, place)
            insertTag(otherAccountId, otherAccountTag)

            link(accountId = accountId, placeId = place.id, tagId = otherAccountTag.id)

            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-PLACE-TAG-DOMAIN-007 연결을 해제하면 조회되지 않고 다시 만들면 복구된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            unlink(accountId = accountId, placeId = place.id, tagId = tag.id)
            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()

            link(accountId = accountId, placeId = place.id, tagId = tag.id)
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-008 TC-PLACE-DETAIL-DOMAIN-027 삭제된 태그는 조회되지 않고 완료된 태그는 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = instant())
            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = false, updatedAt = instant())
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-009 연결된 태그는 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstTag = tag(title = "a")
            val secondTag = tag(title = "b")
            val thirdTag = tag(title = "c")
            insertPlace(accountId, place)
            insertTag(accountId, thirdTag, firstTag, secondTag)
            link(accountId = accountId, placeId = place.id, tagId = thirdTag.id)
            link(accountId = accountId, placeId = place.id, tagId = firstTag.id)
            link(accountId = accountId, placeId = place.id, tagId = secondTag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe
                listOf(firstTag.id, secondTag.id, thirdTag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-010 기기에 없는 태그를 가리키는 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertPlace(accountId, place)

            link(accountId = accountId, placeId = place.id, tagId = fixtureMonkey.giveMeOne<Uuid>())

            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-PLACE-TAG-DOMAIN-011 태그를 완료하거나 삭제해도 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = instant())

            database
                .placeTagDao()
                .findByPlaceIdList(listOf(place.id))
                .single()
                .isDeleted shouldBe false
        }

        test("TC-PLACE-TAG-DOMAIN-012 장소를 삭제해도 태그 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            placeTransaction.updateDeleted(accountId = accountId, placeId = place.id, isDeleted = true, updatedAt = instant())

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-013 장소의 내용 수정은 태그 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstTag = tag()
            val secondTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, firstTag, secondTag)
            link(accountId = accountId, placeId = place.id, tagId = firstTag.id)
            link(accountId = accountId, placeId = place.id, tagId = secondTag.id)

            placeTransaction.updateDetail(
                accountId = accountId,
                placeId = place.id,
                detail = fixtureMonkey.giveMeOne<PlaceDetailLocalEntity>(),
                updatedAt = instant(),
            )

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldContainExactlyInAnyOrder
                listOf(firstTag.id, secondTag.id)
        }

        test("TC-PLACE-TAG-DOMAIN-015 태그와 태그의 연결을 따라 장소가 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val place = place()
            insertTag(accountId, firstTag, secondTag)
            insertPlace(accountId, place)
            link(accountId = accountId, placeId = place.id, tagId = secondTag.id)

            database.accountTagLinkDao().upsert(
                io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLinkLocalEntity(
                    accountId = accountId,
                    fromTagId = firstTag.id,
                    toTagId = secondTag.id,
                    isDirty = true,
                ),
            )

            tagPlaceIdList(accountId = accountId, tagId = firstTag.id).shouldBeEmpty()
            tagPlaceIdList(accountId = accountId, tagId = secondTag.id) shouldBe listOf(place.id)
        }

        test("TC-PLACE-TAG-DATA-003 연결 해제는 같은 장소의 다른 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val removedTag = tag()
            val keptTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, removedTag, keptTag)
            link(accountId = accountId, placeId = place.id, tagId = removedTag.id)
            link(accountId = accountId, placeId = place.id, tagId = keptTag.id)

            unlink(accountId = accountId, placeId = place.id, tagId = removedTag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(keptTag.id)
        }

        test("TC-PLACE-TAG-DATA-010 가리키는 항목이 기기에 없는 연결도 저장은 성공한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)

            link(accountId = accountId, placeId = place.id, tagId = tag.id)
            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()

            insertTag(accountId, tag)
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-001 연결할 수 있는 태그는 계정과 연결된 완료·삭제되지 않은 태그다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val selectableTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            val deletedTag = tag().copy(isDeleted = true)
            val otherAccountTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, selectableTag, finishedTag, deletedTag)
            insertTag(otherAccountId, otherAccountTag)

            selectableTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(selectableTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-005 연결할 수 있는 태그에 없는 연결된 태그도 목록에 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val selectableTag = tag(title = "a")
            val finishedTag = tag(title = "b")
            insertPlace(accountId, place)
            insertTag(accountId, selectableTag, finishedTag)
            link(accountId = accountId, placeId = place.id, tagId = finishedTag.id)
            tagTransaction.updateFinished(accountId = accountId, tagId = finishedTag.id, isFinished = true, updatedAt = instant())

            selectableTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(selectableTag.id, finishedTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-006 목록에 나타난 연결할 수 없는 태그의 연결을 해제하면 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val finishedTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, finishedTag)
            link(accountId = accountId, placeId = place.id, tagId = finishedTag.id)
            tagTransaction.updateFinished(accountId = accountId, tagId = finishedTag.id, isFinished = true, updatedAt = instant())

            selectableTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(finishedTag.id)

            unlink(accountId = accountId, placeId = place.id, tagId = finishedTag.id)

            selectableTagIdList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-011 계정의 미완료·미삭제 태그는 하나도 제외되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstTag = tag(title = "a")
            val secondTag = tag(title = "b")
            insertPlace(accountId, place)
            insertTag(accountId, firstTag, secondTag)
            link(accountId = accountId, placeId = place.id, tagId = firstTag.id)

            selectableTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(firstTag.id, secondTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-002 목록의 태그는 완료 여부로 구분하지 않고 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstTag = tag(title = "a")
            val finishedTag = tag(title = "b")
            val thirdTag = tag(title = "c")
            insertPlace(accountId, place)
            insertTag(accountId, thirdTag, finishedTag, firstTag)
            link(accountId = accountId, placeId = place.id, tagId = finishedTag.id)
            tagTransaction.updateFinished(accountId = accountId, tagId = finishedTag.id, isFinished = true, updatedAt = instant())

            selectableTagIdList(accountId = accountId, placeId = place.id) shouldBe
                listOf(firstTag.id, finishedTag.id, thirdTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-007 검색어는 이모지·제목·설명 중 하나 이상을 포함하면 만족한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val titleTag = tag(title = "여행 기록")
            val englishTag = tag(title = "Travel")
            val descriptionTag = tag(title = "zzz").withDetail(emoji = "", description = "가족 여행")
            val emojiTag = tag(title = "yyy").withDetail(emoji = "✈️", description = "")
            insertPlace(accountId, place)
            insertTag(accountId, titleTag, englishTag, descriptionTag, emojiTag)

            selectableTagIdList(accountId = accountId, placeId = place.id, query = "여행") shouldContainExactlyInAnyOrder
                listOf(titleTag.id, descriptionTag.id)
            selectableTagIdList(accountId = accountId, placeId = place.id, query = "trav") shouldBe listOf(englishTag.id)
            selectableTagIdList(accountId = accountId, placeId = place.id, query = "✈️") shouldBe listOf(emojiTag.id)
            selectableTagIdList(accountId = accountId, placeId = place.id, query = "업무").shouldBeEmpty()
        }

        test("TC-PLACE-TAG-DATA-001 TC-PLACE-ADD-DATA-009 TC-PLACE-ADD-DATA-011 장소 추가와 태그 연결이 하나의 저장 작업으로 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val selectedTagList = List(2) { tag() }
            val unselectedTag = tag()
            insertTag(accountId, *selectedTagList.toTypedArray(), unselectedTag)

            placeTransaction.upsert(
                accountId = accountId,
                placeList = listOf(place),
                placeTagList =
                    selectedTagList.map { value ->
                        PlaceTagLocalEntity(
                            placeId = place.id,
                            tagId = value.id,
                            isDeleted = false,
                            updatedAt = instant(),
                            createdAt = instant(),
                        )
                    },
            )

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldContainExactlyInAnyOrder
                selectedTagList.map { value -> value.id }
            selectedTagList.forEach { value ->
                tagPlaceIdList(accountId = accountId, tagId = value.id) shouldBe listOf(place.id)
            }
            tagPlaceIdList(accountId = accountId, tagId = unselectedTag.id).shouldBeEmpty()
        }

        test("TC-PLACE-TAG-DATA-002 저장이 실패하면 장소와 태그 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertTag(accountId, targetTag)
            val failingDatabase = spyk(database)
            every { failingDatabase.accountPlaceTagDao() } throws IllegalStateException("save failed")
            val failingTransaction = AccountPlaceTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    placeList = listOf(place),
                    placeTagList =
                        listOf(
                            PlaceTagLocalEntity(
                                placeId = place.id,
                                tagId = targetTag.id,
                                isDeleted = false,
                                updatedAt = instant(),
                                createdAt = instant(),
                            ),
                        ),
                )
            }

            linkedTagIdList(accountId = accountId, placeId = place.id).shouldBeEmpty()
            tagPlaceIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
            placeDataSource.find(accountId = accountId, placeId = place.id).first().shouldBeNull()
        }

        test("TC-PLACE-TAG-DOMAIN-014 TC-PLACE-DETAIL-DATA-015 태그 연결은 장소 목록·지도 핀과 장소 검색 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPlace = place(title = "a-place").withCoordinate(latitude = 1.0, longitude = 1.0)
            val secondPlace = place(title = "b-place").withCoordinate(latitude = 2.0, longitude = 2.0)
            val targetTag = tag(title = TAG_ONLY_TITLE)
            insertPlace(accountId, firstPlace, secondPlace)
            insertTag(accountId, targetTag)
            val beforePlaceIdList = placeDataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).pagedPlaceIdList()
            val beforePinIdList = pinPlaceIdList(accountId = accountId)

            link(accountId = accountId, placeId = firstPlace.id, tagId = targetTag.id)

            placeDataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).pagedPlaceIdList() shouldBe beforePlaceIdList
            pinPlaceIdList(accountId = accountId) shouldBe beforePinIdList
            placeDataSource
                .page(accountId = accountId, query = TAG_ONLY_TITLE, sort = ListSortLocalEntity.DEFAULT)
                .pagedPlaceIdList()
                .shouldBeEmpty()
        }

        test("TC-PLACE-DETAIL-DATA-013 태그 연결의 저장은 장소를 업로드 대기로 만들지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)
            placeSyncTransaction.clearPending(accountId = accountId, placeList = listOf(place))

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            placeSyncDataSource.findPending(accountId = accountId).shouldBeEmpty()
            placeTagSyncDataSource
                .findPending(accountId = accountId)
                .map { placeTag -> placeTag.tagId } shouldBe listOf(targetTag.id)
        }

        test("TC-PLACE-DETAIL-DATA-014 태그 연결 결과가 그 태그의 장소 탭에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)
            tagPlaceIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(place.id)

            unlink(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            tagPlaceIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
        }

        test("TC-PLACE-DETAIL-DOMAIN-028 태그 연결과 해제는 장소의 내용과 수정 시각을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)
            placeDataSource.find(accountId = accountId, placeId = place.id).first() shouldBe place

            unlink(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            placeDataSource.find(accountId = accountId, placeId = place.id).first() shouldBe place
        }

        test("TC-PLACE-DETAIL-DOMAIN-030 삭제 상태인 장소에서도 태그 연결을 바꿀 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place().copy(isDeleted = true)
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(targetTag.id)
            placeDataSource
                .find(accountId = accountId, placeId = place.id)
                .first()
                ?.isDeleted shouldBe true
        }

        test("TC-PLACE-TAG-DATA-004 연결의 업로드 대기 여부가 계정별로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            placeTagSyncDataSource
                .findPending(accountId = accountId)
                .map { placeTag -> placeTag.tagId } shouldBe listOf(targetTag.id)
            placeTagSyncDataSource.findPending(accountId = otherAccountId).shouldBeEmpty()
        }

        test("TC-PLACE-DETAIL-DATA-011 연결된 태그는 페이지로 나누지 않고 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val finishedTag = tag(title = "z-finished").copy(isFinished = true)
            val activeTag = tag(title = "a-active")
            insertPlace(accountId, place)
            insertTag(accountId, finishedTag, activeTag)
            val linkedTagList = List(SELECTABLE_PAGE_SIZE + 1) { index -> tag(title = "linked-$index") }
            insertTag(accountId, *linkedTagList.toTypedArray())
            (linkedTagList + finishedTag).forEach { value ->
                link(accountId = accountId, placeId = place.id, tagId = value.id)
            }

            linkedTagIdList(accountId = accountId, placeId = place.id).size shouldBe linkedTagList.size + 1
            selectableTagIdList(accountId = accountId, placeId = place.id) shouldContainExactlyInAnyOrder
                (linkedTagList + finishedTag + activeTag).map { value -> value.id }
        }

        test("TC-ENTITY-TAG-INPUT-DATA-001 태그 선택 목록을 페이지 단위로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tagList = List(3) { index -> tag(title = "tag-$index") }
            insertPlace(accountId, place)
            insertTag(accountId, *tagList.toTypedArray())

            dataSource
                .pageSelectableTag(accountId = accountId, placeId = place.id, query = "")
                .pagedTagIdList(loadSize = 2) shouldBe tagList.take(2).map { tag -> tag.id }
        }
    }) {
    companion object {
        private const val TAG_ONLY_TITLE: String = "PlaceTagOnlyTitle"
        private const val SELECTABLE_PAGE_SIZE: Int = 3

        private suspend fun PagingSource<Int, TagLocalEntity>.pagedTagIdList(loadSize: Int = 100): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>().data.map { tag -> tag.id }
        }

        private suspend fun PagingSource<Int, PlaceLocalEntity>.pagedPlaceIdList(loadSize: Int = 100): List<Uuid> {
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

        private fun PlaceLocalEntity.withCoordinate(
            latitude: Double,
            longitude: Double,
        ): PlaceLocalEntity = copy(detail = detail.copy(latitude = latitude, longitude = longitude))

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun tag(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
                .let { tag ->
                    tag.copy(
                        detail = tag.detail.copy(title = title),
                        isFinished = false,
                        isDeleted = false,
                    )
                }

        private fun TagLocalEntity.withDetail(
            emoji: String,
            description: String,
        ): TagLocalEntity = copy(detail = detail.copy(emoji = emoji, description = description))

        private fun place(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()
                .let { place ->
                    place.copy(
                        detail = place.detail.copy(title = title),
                        isDeleted = false,
                    )
                }
    }
}
