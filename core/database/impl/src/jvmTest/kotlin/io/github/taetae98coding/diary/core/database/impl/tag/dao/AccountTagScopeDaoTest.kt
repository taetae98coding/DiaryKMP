package io.github.taetae98coding.diary.core.database.impl.tag.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.placetag.transaction.AccountPlaceTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.webtag.transaction.AccountWebTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.uuid.Uuid

private const val SOUTH = -90.0
private const val NORTH = 90.0
private const val WEST = -180.0
private const val EAST = 180.0
private const val INVALIDATION_TIMEOUT_MILLIS = 30_000L

class AccountTagScopeDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var tagLinkTransaction: AccountTagLinkTransactionImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var memoTagTransaction: AccountMemoTagTransactionImpl
        lateinit var webTransaction: AccountWebTransactionImpl
        lateinit var webTagTransaction: AccountWebTagTransactionImpl
        lateinit var placeTransaction: AccountPlaceTransactionImpl
        lateinit var placeTagTransaction: AccountPlaceTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            tagTransaction = AccountTagTransactionImpl(database = database)
            tagLinkTransaction = AccountTagLinkTransactionImpl(database = database)
            memoTransaction = AccountMemoTransactionImpl(database = database)
            memoTagTransaction = AccountMemoTagTransactionImpl(database = database)
            webTransaction = AccountWebTransactionImpl(database = database)
            webTagTransaction = AccountWebTagTransactionImpl(database = database)
            placeTransaction = AccountPlaceTransactionImpl(database = database)
            placeTagTransaction = AccountPlaceTagTransactionImpl(database = database)
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

        suspend fun link(
            accountId: Uuid,
            fromTagId: Uuid,
            toTagId: Uuid,
        ) {
            tagLinkTransaction.upsert(
                accountId = accountId,
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        suspend fun insertMemo(
            accountId: Uuid,
            memo: MemoLocalEntity,
            vararg tagIdList: Uuid,
        ) {
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            tagIdList.forEach { tagId ->
                memoTagTransaction.upsert(
                    accountId = accountId,
                    memoId = memo.id,
                    tagId = tagId,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            }
        }

        suspend fun insertWeb(
            accountId: Uuid,
            web: WebLocalEntity,
            vararg tagIdList: Uuid,
        ) {
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            tagIdList.forEach { tagId ->
                webTagTransaction.upsert(
                    accountId = accountId,
                    webId = web.id,
                    tagId = tagId,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            }
        }

        suspend fun insertPlace(
            accountId: Uuid,
            place: PlaceLocalEntity,
            vararg tagIdList: Uuid,
        ) {
            placeTransaction.upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            tagIdList.forEach { tagId ->
                placeTagTransaction.upsert(
                    accountId = accountId,
                    placeId = place.id,
                    tagId = tagId,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            }
        }

        suspend fun memoIdList(
            accountId: Uuid,
            tagId: Uuid,
            scope: TagScopeLocalEntity,
        ): List<Uuid> =
            database
                .accountTagMemoDao()
                .page(
                    accountId = accountId,
                    tagId = tagId,
                    scope = scope.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIdList { memo -> memo.id }

        suspend fun finishedMemoIdList(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            database
                .accountTagMemoDao()
                .pageFinished(
                    accountId = accountId,
                    tagId = tagId,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIdList { memo -> memo.id }

        suspend fun webIdList(
            accountId: Uuid,
            tagId: Uuid,
            scope: TagScopeLocalEntity,
        ): List<Uuid> =
            database
                .accountTagWebDao()
                .page(
                    accountId = accountId,
                    tagId = tagId,
                    scope = scope.queryValue,
                    sort = ListSortLocalEntity.TITLE.queryValue,
                ).pagedIdList { web -> web.id }

        suspend fun placeIdList(
            accountId: Uuid,
            tagId: Uuid,
            scope: TagScopeLocalEntity,
        ): List<Uuid> =
            database
                .accountTagPlaceDao()
                .page(
                    accountId = accountId,
                    tagId = tagId,
                    scope = scope.queryValue,
                    sort = ListSortLocalEntity.TITLE.queryValue,
                ).pagedIdList { place -> place.id }

        suspend fun boundsPlaceIdList(
            accountId: Uuid,
            tagId: Uuid,
            scope: TagScopeLocalEntity,
        ): List<Uuid> =
            database
                .accountTagPlaceDao()
                .get(
                    accountId = accountId,
                    tagId = tagId,
                    scope = scope.queryValue,
                    south = SOUTH,
                    north = NORTH,
                    west = WEST,
                    east = EAST,
                    sort = ListSortLocalEntity.TITLE.queryValue,
                ).first()
                .map { place -> place.id }

        test("TC-5a247ffc-c974-4880-9f26-ce05e62af7e0 표시 범위에 따라 세 목록에 담기는 태그가 달라진다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            val grandChildTag = tag()
            insertTag(accountId, rootTag, childTag, grandChildTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)
            link(accountId = accountId, fromTagId = childTag.id, toTagId = grandChildTag.id)

            val rootMemo = memo()
            val childMemo = memo()
            val grandChildMemo = memo()
            insertMemo(accountId, rootMemo, rootTag.id)
            insertMemo(accountId, childMemo, childTag.id)
            insertMemo(accountId, grandChildMemo, grandChildTag.id)

            val rootWeb = web()
            val childWeb = web()
            val grandChildWeb = web()
            insertWeb(accountId, rootWeb, rootTag.id)
            insertWeb(accountId, childWeb, childTag.id)
            insertWeb(accountId, grandChildWeb, grandChildTag.id)

            val rootPlace = place()
            val childPlace = place()
            val grandChildPlace = place()
            insertPlace(accountId, rootPlace, rootTag.id)
            insertPlace(accountId, childPlace, childTag.id)
            insertPlace(accountId, grandChildPlace, grandChildTag.id)

            listOf(
                TagScopeLocalEntity.SELF to 0,
                TagScopeLocalEntity.CHILD to 1,
                TagScopeLocalEntity.DESCENDANT to 2,
            ).forEach { (scope, depth) ->
                val expectedMemoIdList = listOf(rootMemo.id, childMemo.id, grandChildMemo.id).take(depth + 1)
                val expectedWebIdList = listOf(rootWeb.id, childWeb.id, grandChildWeb.id).take(depth + 1)
                val expectedPlaceIdList = listOf(rootPlace.id, childPlace.id, grandChildPlace.id).take(depth + 1)

                memoIdList(accountId = accountId, tagId = rootTag.id, scope = scope) shouldContainExactlyInAnyOrder expectedMemoIdList
                webIdList(accountId = accountId, tagId = rootTag.id, scope = scope) shouldContainExactlyInAnyOrder expectedWebIdList
                placeIdList(accountId = accountId, tagId = rootTag.id, scope = scope) shouldContainExactlyInAnyOrder expectedPlaceIdList
            }
        }

        test("TC-f22e32bd-afa2-4ac2-8ee6-2134fed88626 범위 안 여러 태그와 연결된 항목도 목록에 한 번만 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            insertTag(accountId, rootTag, childTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)

            val sharedMemo = memo()
            val sharedWeb = web()
            val sharedPlace = place()
            insertMemo(accountId, sharedMemo, rootTag.id, childTag.id)
            insertWeb(accountId, sharedWeb, rootTag.id, childTag.id)
            insertPlace(accountId, sharedPlace, rootTag.id, childTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(sharedMemo.id)
            webIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(sharedWeb.id)
            placeIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(sharedPlace.id)
        }

        test("TC-b3c18b65-245d-44ed-819b-bc19dc0489ec 범위 안 여러 태그와 연결된 장소도 지도에 핀 하나로만 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            insertTag(accountId, rootTag, childTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)

            val sharedPlace = place().withCoordinate(latitude = 0.0, longitude = 0.0)
            insertPlace(accountId, sharedPlace, rootTag.id, childTag.id)

            boundsPlaceIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(sharedPlace.id)
        }

        test("TC-6144a5a4-353e-49c8-8d51-574bae7c4114 화면이 열려 있는 동안 태그 연결이 바뀌면 세 목록 조회가 갱신된다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            insertTag(accountId, rootTag, childTag)

            val childMemo = memo()
            val childWeb = web()
            val childPlace = place()
            insertMemo(accountId, childMemo, childTag.id)
            insertWeb(accountId, childWeb, childTag.id)
            insertPlace(accountId, childPlace, childTag.id)

            val memoPagingSource =
                database.accountTagMemoDao().page(
                    accountId = accountId,
                    tagId = rootTag.id,
                    scope = TagScopeLocalEntity.CHILD.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                )
            val webPagingSource =
                database.accountTagWebDao().page(
                    accountId = accountId,
                    tagId = rootTag.id,
                    scope = TagScopeLocalEntity.CHILD.queryValue,
                    sort = ListSortLocalEntity.TITLE.queryValue,
                )
            val placePagingSource =
                database.accountTagPlaceDao().page(
                    accountId = accountId,
                    tagId = rootTag.id,
                    scope = TagScopeLocalEntity.CHILD.queryValue,
                    sort = ListSortLocalEntity.TITLE.queryValue,
                )
            memoPagingSource.pagedIdList { memo -> memo.id }.shouldBeEmpty()
            webPagingSource.pagedIdList { web -> web.id }.shouldBeEmpty()
            placePagingSource.pagedIdList { place -> place.id }.shouldBeEmpty()

            val memoInvalidated = CompletableDeferred<Unit>()
            val webInvalidated = CompletableDeferred<Unit>()
            val placeInvalidated = CompletableDeferred<Unit>()
            memoPagingSource.registerInvalidatedCallback { memoInvalidated.complete(Unit) }
            webPagingSource.registerInvalidatedCallback { webInvalidated.complete(Unit) }
            placePagingSource.registerInvalidatedCallback { placeInvalidated.complete(Unit) }

            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) {
                memoInvalidated.await()
                webInvalidated.await()
                placeInvalidated.await()
            }
            memoPagingSource.invalid shouldBe true
            webPagingSource.invalid shouldBe true
            placePagingSource.invalid shouldBe true
            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(childMemo.id)
            webIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(childWeb.id)
            placeIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(childPlace.id)
        }

        test("TC-11a748b2-3712-4bb6-b382-9f0c78449aab 표시 범위를 넓혀도 각 탭의 나머지 노출 기준은 유지된다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            insertTag(accountId, rootTag, childTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)

            insertMemo(accountId, memo().copy(isFinished = true), childTag.id)
            insertMemo(accountId, memo().copy(isDeleted = true), childTag.id)
            insertWeb(accountId, web().copy(isDeleted = true), childTag.id)
            insertPlace(accountId, place().copy(isDeleted = true), childTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT).shouldBeEmpty()
            webIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT).shouldBeEmpty()
            placeIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT).shouldBeEmpty()
        }

        test("TC-ad793021-d335-435e-a56e-637fdfdae722 상세 대상 태그의 완료·삭제 여부는 표시 범위 적용을 바꾸지 않는다") {
            listOf(
                tag().copy(isFinished = true),
                tag().copy(isDeleted = true),
            ).forEach { rootTag ->
                val accountId = Arb.kotlinUuid().next()
                val childTag = tag()
                insertTag(accountId, rootTag, childTag)
                link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)

                val rootMemo = memo()
                val childMemo = memo()
                insertMemo(accountId, rootMemo, rootTag.id)
                insertMemo(accountId, childMemo, childTag.id)

                memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldContainExactlyInAnyOrder
                    listOf(rootMemo.id, childMemo.id)
            }
        }

        test("TC-bd009926-ec5f-4936-8486-ed743d13988f 완료된 메모 목록에는 표시 범위를 적용하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            insertTag(accountId, rootTag, childTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)

            val rootFinishedMemo = memo().copy(isFinished = true)
            val childFinishedMemo = memo().copy(isFinished = true)
            insertMemo(accountId, rootFinishedMemo, rootTag.id)
            insertMemo(accountId, childFinishedMemo, childTag.id)

            finishedMemoIdList(accountId = accountId, tagId = rootTag.id) shouldBe listOf(rootFinishedMemo.id)
        }

        test("TC-1742db05-4807-4512-ab3a-b1d1ba00e892 하위 태그는 그 태그에서 향해 나가는 연결의 도착 태그만 포함한다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            val parentTag = tag()
            insertTag(accountId, rootTag, childTag, parentTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)
            link(accountId = accountId, fromTagId = parentTag.id, toTagId = rootTag.id)

            val rootMemo = memo()
            val childMemo = memo()
            val parentMemo = memo()
            insertMemo(accountId, rootMemo, rootTag.id)
            insertMemo(accountId, childMemo, childTag.id)
            insertMemo(accountId, parentMemo, parentTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldContainExactlyInAnyOrder
                listOf(rootMemo.id, childMemo.id)
        }

        test("TC-9a66b140-8d18-4eef-9a61-89c62db0a66b 후손 태그는 연결을 계속 따라간 태그를 모두 포함한다") {
            val accountId = Arb.kotlinUuid().next()
            val firstTag = tag()
            val secondTag = tag()
            val thirdTag = tag()
            val fourthTag = tag()
            insertTag(accountId, firstTag, secondTag, thirdTag, fourthTag)
            link(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)
            link(accountId = accountId, fromTagId = secondTag.id, toTagId = thirdTag.id)
            link(accountId = accountId, fromTagId = thirdTag.id, toTagId = fourthTag.id)

            val secondMemo = memo()
            val thirdMemo = memo()
            val fourthMemo = memo()
            insertMemo(accountId, secondMemo, secondTag.id)
            insertMemo(accountId, thirdMemo, thirdTag.id)
            insertMemo(accountId, fourthMemo, fourthTag.id)

            memoIdList(accountId = accountId, tagId = firstTag.id, scope = TagScopeLocalEntity.DESCENDANT) shouldContainExactlyInAnyOrder
                listOf(secondMemo.id, thirdMemo.id, fourthMemo.id)
        }

        test("TC-a60bac4d-9731-472a-a6b3-0f608caf441e 순환하는 연결이 있어도 후손 태그 조회가 끝나고 같은 태그가 한 번만 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val firstTag = tag()
            val secondTag = tag()
            val thirdTag = tag()
            insertTag(accountId, firstTag, secondTag, thirdTag)
            link(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)
            link(accountId = accountId, fromTagId = secondTag.id, toTagId = thirdTag.id)
            link(accountId = accountId, fromTagId = thirdTag.id, toTagId = firstTag.id)

            val firstMemo = memo()
            val secondMemo = memo()
            val thirdMemo = memo()
            insertMemo(accountId, firstMemo, firstTag.id)
            insertMemo(accountId, secondMemo, secondTag.id)
            insertMemo(accountId, thirdMemo, thirdTag.id)

            memoIdList(accountId = accountId, tagId = firstTag.id, scope = TagScopeLocalEntity.DESCENDANT) shouldContainExactlyInAnyOrder
                listOf(firstMemo.id, secondMemo.id, thirdMemo.id)
        }

        test("TC-cc30ec87-4523-4857-a365-fc3a3f6176be 서로 다른 경로가 같은 태그에 닿아도 한 번만 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val leftTag = tag()
            val rightTag = tag()
            val sharedTag = tag()
            insertTag(accountId, rootTag, leftTag, rightTag, sharedTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = leftTag.id)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = rightTag.id)
            link(accountId = accountId, fromTagId = leftTag.id, toTagId = sharedTag.id)
            link(accountId = accountId, fromTagId = rightTag.id, toTagId = sharedTag.id)

            val sharedMemo = memo()
            insertMemo(accountId, sharedMemo, sharedTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT) shouldBe listOf(sharedMemo.id)
        }

        test("TC-c4494862-8d80-492e-89b4-82dc09b3c9cb 삭제된 하위 태그와 그 태그를 지나는 태그는 후손 태그에 포함되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val deletedTag = tag().copy(isDeleted = true)
            val beyondTag = tag()
            insertTag(accountId, rootTag, deletedTag, beyondTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = deletedTag.id)
            link(accountId = accountId, fromTagId = deletedTag.id, toTagId = beyondTag.id)

            insertMemo(accountId, memo(), deletedTag.id)
            insertMemo(accountId, memo(), beyondTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT).shouldBeEmpty()
        }

        test("TC-cecd789d-ef15-48f8-ad61-95dbf347c4a6 완료된 하위 태그와 그 태그를 지나는 태그는 후손 태그에 포함된다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            val beyondTag = tag()
            insertTag(accountId, rootTag, finishedTag, beyondTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = finishedTag.id)
            link(accountId = accountId, fromTagId = finishedTag.id, toTagId = beyondTag.id)

            val finishedTagMemo = memo()
            val beyondTagMemo = memo()
            insertMemo(accountId, finishedTagMemo, finishedTag.id)
            insertMemo(accountId, beyondTagMemo, beyondTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT) shouldContainExactlyInAnyOrder
                listOf(finishedTagMemo.id, beyondTagMemo.id)
        }

        test("다른 계정의 태그 연결은 표시 범위를 넓히지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val rootTag = tag()
            val childTag = tag()
            insertTag(accountId, rootTag)
            insertTag(otherAccountId, childTag)
            link(accountId = otherAccountId, fromTagId = rootTag.id, toTagId = childTag.id)

            insertMemo(otherAccountId, memo(), childTag.id)

            memoIdList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT).shouldBeEmpty()
        }
    }) {
    companion object {
        private suspend fun <T : Any> PagingSource<Int, T>.pagedIdList(
            loadSize: Int = 100,
            id: (T) -> Uuid,
        ): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>().data.map(id)
        }

        private fun tag(): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).let { tag ->
                    tag.copy(
                        detail = tag.detail.copy(title = "title-${Arb.string().next()}"),
                        isFinished = false,
                        isDeleted = false,
                    )
                }

        private fun memo(): MemoLocalEntity =
            Arb
                .memoLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).let { memo ->
                    memo.copy(
                        detail = memo.detail.copy(title = "title-${Arb.string().next()}"),
                        primaryTagId = null,
                        isFinished = false,
                        isDeleted = false,
                    )
                }

        private fun web(): WebLocalEntity =
            Arb
                .webLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).let { web ->
                    web.copy(
                        detail = web.detail.copy(title = "title-${Arb.string().next()}"),
                        isDeleted = false,
                    )
                }

        private fun place(): PlaceLocalEntity =
            Arb
                .placeLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ).let { place ->
                    place.copy(
                        detail = place.detail.copy(title = "title-${Arb.string().next()}"),
                        isDeleted = false,
                    )
                }

        private fun PlaceLocalEntity.withCoordinate(
            latitude: Double,
            longitude: Double,
        ): PlaceLocalEntity = copy(detail = detail.copy(latitude = latitude, longitude = longitude))
    }
}
