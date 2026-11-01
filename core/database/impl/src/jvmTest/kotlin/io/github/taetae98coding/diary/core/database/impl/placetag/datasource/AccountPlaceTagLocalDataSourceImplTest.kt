package io.github.taetae98coding.diary.core.database.impl.placetag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountTagMemoLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.transaction.AccountMemoPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.place.datasource.AccountPlaceLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.place.datasource.AccountPlaceSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.place.datasource.AccountTagPlaceLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.placetag.transaction.AccountPlaceTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.placetag.transaction.AccountPlaceTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.memo.localMemo
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

        fun addedPlaceTag(
            placeId: Uuid,
            tagId: Uuid,
        ): PlaceTagLocalEntity =
            PlaceTagLocalEntity(
                placeId = placeId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

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

        test("TC-36040314-b2fa-4cd6-9b33-b1d5710bd002 하나의 장소에 여러 태그를 연결할 수 있다") {
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

        test("TC-1caa66c1-25d4-4218-83ca-b975ef8903b3 하나의 태그를 여러 장소에 연결할 수 있다") {
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

        test("TC-41a8ac6d-424e-42eb-b1c9-f1feb9820aa9 같은 장소와 태그의 연결은 중복되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)

            link(accountId = accountId, placeId = place.id, tagId = tag.id)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-9f9a59a5-d2d3-45a2-904b-489b03a5c944 연결된 태그가 없는 장소도 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertPlace(accountId, place)

            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-13ae46b2-5a38-4f9e-a24d-6ae65b6acd79 연결하거나 해제해도 장소와 태그의 내용은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val baseTag = tag()
            val tag = baseTag.copy(detail = baseTag.detail.copy(color = place.detail.color.inv()))
            insertPlace(accountId, place)
            insertTag(accountId, tag)

            link(accountId = accountId, placeId = place.id, tagId = tag.id)
            database.accountPlaceDao().find(accountId = accountId, placeId = place.id).first() shouldBe place
            database.accountTagDao().find(accountId = accountId, tagId = tag.id).first() shouldBe tag

            unlink(accountId = accountId, placeId = place.id, tagId = tag.id)
            database.accountPlaceDao().find(accountId = accountId, placeId = place.id).first() shouldBe place
            database.accountTagDao().find(accountId = accountId, tagId = tag.id).first() shouldBe tag
        }

        test("TC-2cf2151c-47fb-46db-9562-b46d6292a2a9 계정과 연결되지 않은 태그는 연결된 태그로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val otherAccountTag = tag()
            insertPlace(accountId, place)
            insertTag(otherAccountId, otherAccountTag)

            link(accountId = accountId, placeId = place.id, tagId = otherAccountTag.id)

            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-f380865f-40ed-4b8f-adea-05aafc8149b9 연결을 해제하면 조회되지 않고 다시 만들면 복구된다") {
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

        test("TC-202cb7c8-07c8-4f7c-b971-844929636b95 TC-cda91020-0a3f-48e4-af24-76391114f1fa 삭제된 태그는 조회되지 않고 완료된 태그는 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)

            val deletedAt = instant()
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = deletedAt)
            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()

            AccountTagSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                tagList = listOf(tag.copy(isFinished = true, isDeleted = false, updatedAt = deletedAt)),
                cursor = fixtureMonkey.giveMeOne<Long>(),
            )
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-ed9a3af3-c21a-417a-bb26-63211e4875c2 연결된 태그는 제목 오름차순으로 조회된다") {
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

        test("TC-1817b87d-dbfd-444d-9bb6-a8b52d9a0588 기기에 없는 태그를 가리키는 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            insertPlace(accountId, place)

            link(accountId = accountId, placeId = place.id, tagId = fixtureMonkey.giveMeOne<Uuid>())

            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()
        }

        test("TC-df698e6c-8b40-4208-8843-63eed23c334a 태그를 완료하거나 삭제해도 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())

            database
                .placeTagDao()
                .findByPlaceIdList(listOf(place.id))
                .single()
                .isDeleted shouldBe false

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = instant())

            database
                .placeTagDao()
                .findByPlaceIdList(listOf(place.id))
                .single()
                .isDeleted shouldBe false
        }

        test("TC-cfcc2dee-59c0-4ee4-9117-c2c39c75dfa5 장소를 삭제해도 태그 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            placeTransaction.updateDeleted(accountId = accountId, placeId = place.id, isDeleted = true, updatedAt = instant())

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-ede3116b-87ed-43c0-ab0b-7b189644b973 장소의 내용 수정은 태그 연결을 바꾸지 않는다") {
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

        test("TC-4bc90091-859a-4965-ab30-3972f677477d 태그와 태그의 연결을 따라 장소가 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val place = place()
            insertTag(accountId, firstTag, secondTag)
            insertPlace(accountId, place)
            link(accountId = accountId, placeId = place.id, tagId = secondTag.id)

            database.accountTagLinkDao().upsert(
                io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity(
                    accountId = accountId,
                    fromTagId = firstTag.id,
                    toTagId = secondTag.id,
                    isDirty = true,
                ),
            )

            tagPlaceIdList(accountId = accountId, tagId = firstTag.id).shouldBeEmpty()
            tagPlaceIdList(accountId = accountId, tagId = secondTag.id) shouldBe listOf(place.id)
        }

        test("TC-e87bf594-41f5-4e62-85d7-830a592712f9 장소와 태그를 연결해도 그 장소에 연결된 메모에는 태그가 연결되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, tag)
            AccountMemoTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
            )
            AccountMemoPlaceTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoId = memo.id,
                placeId = place.id,
                isDeleted = false,
                updatedAt = instant(),
            )

            link(accountId = accountId, placeId = place.id, tagId = tag.id)

            AccountMemoTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, memoId = memo.id)
                .first()
                .shouldBeEmpty()
            val tagMemoResult =
                AccountTagMemoLocalDataSourceImpl(database = database)
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                    .load(
                        PagingSource.LoadParams.Refresh(
                            key = null,
                            loadSize = 100,
                            placeholdersEnabled = false,
                        ),
                    )
            tagMemoResult
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>()
                .data
                .shouldBeEmpty()
        }

        test("TC-7a28c56d-8d87-4e0f-92a0-97784ad00d1f 연결 해제는 같은 장소의 다른 연결을 바꾸지 않는다") {
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

        test("TC-cb94f332-7620-4cad-8baf-c8fc133c0b75 가리키는 항목이 기기에 없는 연결도 저장은 성공한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val tag = tag()
            insertPlace(accountId, place)

            link(accountId = accountId, placeId = place.id, tagId = tag.id)
            linkedTagList(accountId = accountId, placeId = place.id).shouldBeEmpty()

            insertTag(accountId, tag)
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(tag.id)
        }

        test("TC-514c141c-6bad-40e1-b1bd-77c64ccfdfc2 연결할 수 있는 태그는 계정과 연결된 미완료·미삭제 태그다") {
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

        test("TC-97c62caf-cc7f-4f53-be0d-dd8ad576eca8 연결할 수 있는 태그에 없는 연결된 태그도 목록에 함께 나타난다") {
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

        test("TC-006b9a6d-7ea2-4e2d-9e27-512cfb32355d 목록에 나타난 연결할 수 없는 태그의 연결을 해제하면 목록에서 사라진다") {
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

        test("TC-3330799c-1f9d-4a22-8486-fb2bc9446002 계정의 미완료·미삭제 태그는 하나도 제외되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val firstTag = tag(title = "a")
            val secondTag = tag(title = "b")
            insertPlace(accountId, place)
            insertTag(accountId, firstTag, secondTag)
            link(accountId = accountId, placeId = place.id, tagId = firstTag.id)

            selectableTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(firstTag.id, secondTag.id)
        }

        test("TC-775ecb03-f12a-4bfc-8b94-55ecb871e940 목록의 태그는 완료 여부로 구분하지 않고 제목 오름차순으로 정렬된다") {
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

        test("TC-deb2f172-cf67-4ec6-82cd-7aa59a83badf 검색어는 이모지·제목·설명 중 하나 이상을 포함하면 만족한다") {
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

        test("TC-91a28ed2-a761-49ec-8fa6-a51061cfa4b2 고른 뒤 완료된 태그는 추가한 장소와 연결되어 연결된 태그로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val finishedTag = tag().copy(isFinished = true)
            insertTag(accountId, finishedTag)

            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = listOf(addedPlaceTag(placeId = place.id, tagId = finishedTag.id)))

            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(finishedTag.id)
        }

        test("TC-91a28ed2-a761-49ec-8fa6-a51061cfa4b2 고른 뒤 삭제된 태그도 추가한 장소와 연결되어 삭제를 되돌리면 연결된 태그로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val deletedTag = tag().copy(isDeleted = true)
            insertTag(accountId, deletedTag)

            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = listOf(addedPlaceTag(placeId = place.id, tagId = deletedTag.id)))

            linkedTagIdList(accountId = accountId, placeId = place.id).shouldBeEmpty()
            insertTag(accountId, deletedTag.copy(isDeleted = false))
            linkedTagIdList(accountId = accountId, placeId = place.id) shouldBe listOf(deletedTag.id)
        }

        test("TC-2121054a-8501-4544-acb9-e2ecc6f802c7 TC-4dd1e038-bbcb-4df9-827c-fd1f7f26cd84 TC-a1f67716-6619-4c28-bb0b-db49aee35c12 장소 추가와 태그 연결이 하나의 저장 작업으로 반영된다") {
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

        test("TC-be4de6d2-ac3c-49d1-94a5-1709dadf41c4 TC-1c64c262-6223-452d-b72b-6ff5c108b7a5 저장이 실패하면 장소, 계정 연결과 태그 연결이 모두 남지 않는다") {
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
            listOf("place", "account_place", "place_tag", "account_place_tag").forEach { table ->
                database.useReaderConnection { transactor ->
                    transactor.usePrepared("SELECT COUNT(*) FROM $table") { statement ->
                        statement.step()
                        statement.getLong(0)
                    }
                } shouldBe 0L
            }
        }

        test("TC-b9ccc593-1f09-4710-ba90-3ec29a001f44 TC-60158d7b-c0fc-42ce-b158-12e70939875e 태그 연결은 장소 목록·지도 핀과 장소 검색 결과를 바꾸지 않는다") {
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

        test("TC-ab233228-ddbe-4193-a127-b9e46fc8e48a 태그 연결의 저장은 장소를 업로드 대기로 만들지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)
            placeSyncTransaction.clearPending(accountId = accountId, placeList = listOf(place))

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            placeSyncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            placeTagSyncDataSource
                .readPendingList(accountId = accountId)
                .map { placeTag -> placeTag.tagId } shouldBe listOf(targetTag.id)
        }

        test("TC-30f2e9b2-a8c2-45ba-b29e-2b1bf9ac1e0f 태그 연결 결과가 그 태그의 장소 탭에 반영된다") {
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

        test("TC-2798d9a2-1611-4b32-aaf6-4f9f5b3cdadd 태그 연결과 해제는 장소의 내용과 수정 시각을 바꾸지 않는다") {
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

        test("TC-5cdc8027-6bc1-4935-ba05-377cfa1d5cd9 삭제 상태인 장소에서도 태그 연결을 바꿀 수 있다") {
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

        test("TC-088d4249-7c3b-409f-94e8-dba21aacbdbf TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 연결의 업로드 대기 여부가 계정별로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val targetTag = tag()
            insertPlace(accountId, place)
            insertTag(accountId, targetTag)

            link(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            placeTagSyncDataSource
                .readPendingList(accountId = accountId)
                .map { placeTag -> placeTag.tagId } shouldBe listOf(targetTag.id)
            placeTagSyncDataSource.readPendingList(accountId = otherAccountId).shouldBeEmpty()

            AccountPlaceTagSyncTransactionImpl(database = database).clearPending(
                accountId = accountId,
                placeTagList = placeTagSyncDataSource.readPendingList(accountId = accountId),
            )
            unlink(accountId = accountId, placeId = place.id, tagId = targetTag.id)

            placeTagSyncDataSource
                .readPendingList(accountId = accountId)
                .map { placeTag -> placeTag.tagId to placeTag.isDeleted } shouldBe listOf(targetTag.id to true)
            placeTagSyncDataSource.readPendingList(accountId = otherAccountId).shouldBeEmpty()
        }

        test("TC-7b76bc8d-ce67-4e8f-8798-77c0f5bebeb7 TC-74e4c103-2255-4f3b-8e5f-0dba291014d6 연결된 태그는 페이지로 나누지 않고 조회한다") {
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

        test("TC-6cddd37e-ef3a-48f0-869d-c33623995b26 검색어를 만족하는 태그는 목록의 첫 페이지에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val place = place()
            val matchedTag = tag(title = "z-matched-$SEARCH_ONLY_QUERY")
            insertPlace(accountId, place)
            insertTag(accountId, *List(SELECTABLE_PAGE_SIZE) { index -> tag(title = "a-other-$index") }.toTypedArray())
            insertTag(accountId, matchedTag)

            dataSource
                .pageSelectableTag(accountId = accountId, placeId = place.id, query = SEARCH_ONLY_QUERY)
                .pagedTagIdList(loadSize = SELECTABLE_PAGE_SIZE) shouldBe listOf(matchedTag.id)
        }

        test("TC-0040fa16-bd85-4002-9e38-ad256ce90a59 태그 선택 목록을 페이지 단위로 조회한다") {
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
        private const val SEARCH_ONLY_QUERY: String = "PlaceTagSearchOnly"
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

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

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
