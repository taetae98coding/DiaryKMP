package io.github.taetae98coding.diary.core.database.impl.search.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SearchPagingDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemo(
            accountId: Uuid,
            vararg memoList: MemoLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(memoList.toList())
                database.accountMemoDao().upsert(
                    memoList.map { memo ->
                        AccountMemoLocalEntity(
                            accountId = accountId,
                            memoId = memo.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(tagList.toList())
                database.accountTagDao().upsert(
                    tagList.map { tag ->
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun insertPlace(
            accountId: Uuid,
            vararg placeList: PlaceLocalEntity,
        ) {
            database.withWriteTransaction {
                database.placeDao().upsert(placeList.toList())
                database.accountPlaceDao().upsert(
                    placeList.map { place ->
                        AccountPlaceLocalEntity(
                            accountId = accountId,
                            placeId = place.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun insertWeb(
            accountId: Uuid,
            vararg webList: WebLocalEntity,
        ) {
            database.withWriteTransaction {
                database.webDao().upsert(webList.toList())
                database.accountWebDao().upsert(
                    webList.map { web ->
                        AccountWebLocalEntity(
                            accountId = accountId,
                            webId = web.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun insertMemoTag(
            accountId: Uuid,
            memoTag: MemoTagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoTagDao().upsert(listOf(memoTag))
                database.accountMemoTagDao().upsert(
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoTag.memoId,
                        tagId = memoTag.tagId,
                        isDirty = true,
                    ),
                )
            }
        }

        suspend fun memoIdList(
            accountId: Uuid,
            query: String = QUERY,
        ): List<Uuid> =
            database
                .searchMemoDao()
                .page(accountId = accountId, query = query, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadMemoPage()
                .data
                .map { memo -> memo.id }

        suspend fun tagIdList(
            accountId: Uuid,
            query: String = QUERY,
        ): List<Uuid> =
            database
                .searchTagDao()
                .page(accountId = accountId, query = query, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadTagPage()
                .data
                .map { tag -> tag.id }

        suspend fun placeIdList(
            accountId: Uuid,
            query: String = QUERY,
        ): List<Uuid> =
            database
                .searchPlaceDao()
                .page(accountId = accountId, query = query, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadPlacePage()
                .data
                .map { place -> place.id }

        suspend fun webIdList(
            accountId: Uuid,
            query: String = QUERY,
        ): List<Uuid> =
            database
                .searchWebDao()
                .page(accountId = accountId, query = query, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadWebPage()
                .data
                .map { web -> web.id }

        test("TC-cd483a5b-ad04-4c6c-bedb-ea734aa93d42 메모는 제목과 설명 중 하나가 질의를 포함하면 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val titleMatched = memo(title = "$QUERY 계획")
            val descriptionMatched = memo(description = "$QUERY 준비물")
            val unmatched = memo()
            insertMemo(accountId, titleMatched, descriptionMatched, unmatched)

            memoIdList(accountId) shouldContainExactlyInAnyOrder listOf(titleMatched.id, descriptionMatched.id)
        }

        test("TC-cd483a5b-ad04-4c6c-bedb-ea734aa93d42 태그는 이모지, 제목, 설명 중 하나가 질의를 포함하면 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val emojiMatched = tag(emoji = QUERY)
            val titleMatched = tag(title = "$QUERY 태그")
            val descriptionMatched = tag(description = "$QUERY 기록")
            val unmatched = tag()
            insertTag(accountId, emojiMatched, titleMatched, descriptionMatched, unmatched)

            tagIdList(accountId) shouldContainExactlyInAnyOrder
                listOf(emojiMatched.id, titleMatched.id, descriptionMatched.id)
        }

        test("TC-cd483a5b-ad04-4c6c-bedb-ea734aa93d42 장소는 제목, 설명, 주소 중 하나가 질의를 포함하면 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val titleMatched = place(title = "$QUERY 숙소")
            val descriptionMatched = place(description = "$QUERY 후보")
            val addressMatched = place(address = "$QUERY 로 12")
            val unmatched = place()
            insertPlace(accountId, titleMatched, descriptionMatched, addressMatched, unmatched)

            placeIdList(accountId) shouldContainExactlyInAnyOrder
                listOf(titleMatched.id, descriptionMatched.id, addressMatched.id)
        }

        test("TC-cd483a5b-ad04-4c6c-bedb-ea734aa93d42 웹 항목은 제목, 설명, URL 중 하나가 질의를 포함하면 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val titleMatched = web(title = "$QUERY 준비")
            val descriptionMatched = web(description = "$QUERY 정보")
            val urlMatched = web(url = "https://example.com/$QUERY")
            val unmatched = web()
            insertWeb(accountId, titleMatched, descriptionMatched, urlMatched, unmatched)

            webIdList(accountId) shouldContainExactlyInAnyOrder
                listOf(titleMatched.id, descriptionMatched.id, urlMatched.id)
        }

        test("TC-5aa4a76d-2c8d-4107-b957-ea606797d8fd 웹 항목의 요청 헤더만 질의를 포함하면 결과가 되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val headerMatched =
                web(
                    headerList =
                        listOf(
                            WebHeaderLocalEntity(name = "$QUERY-name", value = "$QUERY-value"),
                        ),
                )
            insertWeb(accountId, headerMatched)

            webIdList(accountId).shouldBeEmpty()
        }

        test("TC-8612b422-528c-4e41-a851-b278b1a8449e 값 전체가 질의와 같지 않아도 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(title = "여름 $QUERY 계획")
            insertMemo(accountId, memo)

            memoIdList(accountId) shouldBe listOf(memo.id)
        }

        test("TC-48bc994f-728d-4d74-a8bc-6f61c8516199 ASCII 영문자는 대소문자를 구분하지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(title = "Travel")
            insertMemo(accountId, memo)

            memoIdList(accountId = accountId, query = "travel") shouldBe listOf(memo.id)
        }

        test("TC-b4a06a31-5f7d-417f-a3f1-42efa5a4c66e ASCII 밖의 글자는 대소문자를 맞추지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(title = "Café")
            insertMemo(accountId, memo)

            memoIdList(accountId = accountId, query = "CAFÉ").shouldBeEmpty()
        }

        test("TC-ea4292ca-1646-4d09-b29a-33653c5e14d9 연결된 태그의 제목만 질의를 포함하면 메모는 결과가 되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag(title = "$QUERY 태그")
            insertMemo(accountId, memo)
            insertTag(accountId, tag)
            insertMemoTag(
                accountId = accountId,
                memoTag = memoTag(memoId = memo.id, tagId = tag.id),
            )

            memoIdList(accountId).shouldBeEmpty()
            tagIdList(accountId) shouldBe listOf(tag.id)
        }

        test("TC-51e82cb9-b34f-47f6-8f80-7ec533eaa72e 삭제된 항목은 어느 유형에서도 결과가 되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            insertMemo(accountId, memo(title = QUERY, isDeleted = true))
            insertTag(accountId, tag(title = QUERY, isDeleted = true))
            insertPlace(accountId, place(title = QUERY, isDeleted = true))
            insertWeb(accountId, web(title = QUERY, isDeleted = true))

            memoIdList(accountId).shouldBeEmpty()
            tagIdList(accountId).shouldBeEmpty()
            placeIdList(accountId).shouldBeEmpty()
            webIdList(accountId).shouldBeEmpty()
        }

        test("TC-ab2e0d4c-f761-4de6-b44c-6ed7de86c7f9 완료된 메모와 완료된 태그도 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedMemo = memo(title = QUERY, isFinished = true)
            val finishedTag = tag(title = QUERY, isFinished = true)
            insertMemo(accountId, finishedMemo)
            insertTag(accountId, finishedTag)

            memoIdList(accountId) shouldBe listOf(finishedMemo.id)
            tagIdList(accountId) shouldBe listOf(finishedTag.id)
        }

        test("TC-609e4786-9dfc-48eb-a035-4bf76058601e 네 유형의 결과를 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val secondMemo = memo(title = "나 $QUERY")
            val firstMemo = memo(title = "가 $QUERY")
            val thirdMemo = memo(title = "다 $QUERY")
            val secondTag = tag(title = "나 $QUERY")
            val firstTag = tag(title = "가 $QUERY")
            val thirdTag = tag(title = "다 $QUERY")
            val secondPlace = place(title = "나 $QUERY")
            val firstPlace = place(title = "가 $QUERY")
            val thirdPlace = place(title = "다 $QUERY")
            val secondWeb = web(title = "나 $QUERY")
            val firstWeb = web(title = "가 $QUERY")
            val thirdWeb = web(title = "다 $QUERY")
            insertMemo(accountId, secondMemo, firstMemo, thirdMemo)
            insertTag(accountId, secondTag, firstTag, thirdTag)
            insertPlace(accountId, secondPlace, firstPlace, thirdPlace)
            insertWeb(accountId, secondWeb, firstWeb, thirdWeb)

            memoIdList(accountId) shouldBe listOf(firstMemo.id, secondMemo.id, thirdMemo.id)
            tagIdList(accountId) shouldBe listOf(firstTag.id, secondTag.id, thirdTag.id)
            placeIdList(accountId) shouldBe listOf(firstPlace.id, secondPlace.id, thirdPlace.id)
            webIdList(accountId) shouldBe listOf(firstWeb.id, secondWeb.id, thirdWeb.id)
        }

        test("TC-dbb63bc4-ee53-4d78-ac07-f920773ca782 한 유형을 이어서 조회해도 다른 유형의 조회 범위는 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoList = List(ITEM_COUNT) { index -> memo(title = "$QUERY-${index.pad()}") }
            val tagList = List(ITEM_COUNT) { index -> tag(title = "$QUERY-${index.pad()}") }
            insertMemo(accountId, *memoList.toTypedArray())
            insertTag(accountId, *tagList.toTypedArray())

            val memoPagingSource = database.searchMemoDao().page(accountId = accountId, query = QUERY, sort = ListSortLocalEntity.DEFAULT.queryValue)
            val firstMemoPage = memoPagingSource.loadMemoPage(loadSize = PAGE_SIZE)
            firstMemoPage.data.map { memo -> memo.id } shouldBe memoList.take(PAGE_SIZE).map { memo -> memo.id }

            val secondMemoPage = memoPagingSource.loadMemoPage(key = firstMemoPage.nextKey, loadSize = PAGE_SIZE)
            secondMemoPage.data.map { memo -> memo.id } shouldBe
                memoList.drop(PAGE_SIZE).take(PAGE_SIZE).map { memo -> memo.id }

            database
                .searchTagDao()
                .page(accountId = accountId, query = QUERY, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadTagPage(loadSize = PAGE_SIZE)
                .data
                .map { tag -> tag.id } shouldBe tagList.take(PAGE_SIZE).map { tag -> tag.id }
        }

        test("TC-55b41830-5dd2-442f-ad02-4fb73a8e0e4f 저장된 항목이 바뀌면 결과가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val savedMemo = memo(title = "가 $QUERY")
            insertMemo(accountId, savedMemo)
            val pagingSource = database.searchMemoDao().page(accountId = accountId, query = QUERY, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.loadMemoPage().data.map { memo -> memo.id } shouldBe listOf(savedMemo.id)

            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }
            val addedMemo = memo(title = "나 $QUERY")
            insertMemo(accountId, addedMemo)

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            memoIdList(accountId) shouldBe listOf(savedMemo.id, addedMemo.id)
        }

        test("TC-5f357cf5-11d8-4dcf-898a-46e6d9bb2cf1 질의를 만족하지 않게 바뀌거나 삭제된 항목은 결과에서 사라진다") {
            val changeList: List<(MemoLocalEntity) -> MemoLocalEntity> =
                listOf(
                    { memo -> memo.copy(detail = memo.detail.copy(title = "변경-${fixtureMonkey.giveMeOne<Int>()}", description = "")) },
                    { memo -> memo.copy(isDeleted = true) },
                )

            changeList.forEach { change ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val savedMemo = memo(title = "가 $QUERY", description = "")
                insertMemo(accountId, savedMemo)
                val pagingSource = database.searchMemoDao().page(accountId = accountId, query = QUERY, sort = ListSortLocalEntity.DEFAULT.queryValue)
                pagingSource.loadMemoPage().data.map { memo -> memo.id } shouldBe listOf(savedMemo.id)

                val invalidated = CompletableDeferred<Unit>()
                pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }
                insertMemo(accountId, change(savedMemo))

                withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
                pagingSource.invalid.shouldBeTrue()
                memoIdList(accountId).shouldBeEmpty()
            }
        }

        test("TC-e7eb7b80-8ee7-4906-ae52-8081df9ff769 다른 계정과 연결된 항목은 어느 유형에서도 결과가 되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            insertMemo(otherAccountId, memo(title = QUERY))
            insertTag(otherAccountId, tag(title = QUERY))
            insertPlace(otherAccountId, place(title = QUERY))
            insertWeb(otherAccountId, web(title = QUERY))

            memoIdList(accountId).shouldBeEmpty()
            tagIdList(accountId).shouldBeEmpty()
            placeIdList(accountId).shouldBeEmpty()
            webIdList(accountId).shouldBeEmpty()
        }

        test("TC-1d910620-bfc0-4ea1-871c-f17561570aaf MemoHome에 걸어 둔 필터는 메모 결과를 좁히지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val filterTag = tag()
            val filterTagMemo = memo(title = "$QUERY 태그 있음")
            val untaggedMemo = memo(title = "$QUERY 태그 없음")
            insertTag(accountId, filterTag)
            insertMemo(accountId, filterTagMemo, untaggedMemo)
            insertMemoTag(accountId, memoTag(memoId = filterTagMemo.id, tagId = filterTag.id))
            database.memoFilterTagDao().upsert(entity = MemoFilterTagLocalEntity(accountId = accountId, tagId = filterTag.id))
            // 장소가 있는 메모만 남기는 유무 필터라 MemoHome 목록에는 두 메모 모두 보이지 않는다.
            database.memoExistenceFilterDao().upsert(
                entity = MemoExistenceFilterLocalEntity(hasDate = null, hasTag = true, hasPlace = true),
            )

            memoIdList(accountId) shouldContainExactlyInAnyOrder listOf(filterTagMemo.id, untaggedMemo.id)
        }

        test("TC-3675cf4a-13d8-4607-b7db-80f39b09fafb 서로 멀리 떨어진 장소도 위치와 관계없이 모두 결과가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val seoulPlace = place(title = "$QUERY 서울").located(latitude = SEOUL_LATITUDE, longitude = SEOUL_LONGITUDE)
            val parisPlace = place(title = "$QUERY 파리").located(latitude = PARIS_LATITUDE, longitude = PARIS_LONGITUDE)
            insertPlace(accountId, seoulPlace, parisPlace)

            placeIdList(accountId) shouldContainExactlyInAnyOrder listOf(seoulPlace.id, parisPlace.id)
        }

        test("질의의 SQL 와일드카드 문자는 패턴이 아니라 문자 그대로 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val literalMemo = memo(title = "10% 할인")
            val patternMemo = memo(title = "10 할인")
            insertMemo(accountId, literalMemo, patternMemo)

            memoIdList(accountId = accountId, query = "10%") shouldBe listOf(literalMemo.id)
        }
    }) {
    public companion object {
        private const val QUERY: String = "여행"
        private const val ITEM_COUNT: Int = 25
        private const val PAGE_SIZE: Int = 10
        private const val INVALIDATION_TIMEOUT_MILLIS: Long = 30_000
        private const val SEOUL_LATITUDE: Double = 37.5665
        private const val SEOUL_LONGITUDE: Double = 126.978
        private const val PARIS_LATITUDE: Double = 48.8566
        private const val PARIS_LONGITUDE: Double = 2.3522

        private fun Int.pad(): String = toString().padStart(length = 3, padChar = '0')

        private suspend fun PagingSource<Int, MemoLocalEntity>.loadMemoPage(
            key: Int? = null,
            loadSize: Int = 100,
        ): PagingSource.LoadResult.Page<Int, MemoLocalEntity> =
            load(loadParams(key = key, loadSize = loadSize))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>()

        private suspend fun PagingSource<Int, TagLocalEntity>.loadTagPage(
            key: Int? = null,
            loadSize: Int = 100,
        ): PagingSource.LoadResult.Page<Int, TagLocalEntity> =
            load(loadParams(key = key, loadSize = loadSize))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>()

        private suspend fun PagingSource<Int, PlaceLocalEntity>.loadPlacePage(
            key: Int? = null,
            loadSize: Int = 100,
        ): PagingSource.LoadResult.Page<Int, PlaceLocalEntity> =
            load(loadParams(key = key, loadSize = loadSize))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, PlaceLocalEntity>>()

        private suspend fun PagingSource<Int, WebLocalEntity>.loadWebPage(
            key: Int? = null,
            loadSize: Int = 100,
        ): PagingSource.LoadResult.Page<Int, WebLocalEntity> =
            load(loadParams(key = key, loadSize = loadSize))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, WebLocalEntity>>()

        private fun loadParams(
            key: Int?,
            loadSize: Int,
        ): PagingSource.LoadParams<Int> =
            if (key == null) {
                PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
            } else {
                PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
            }

        private fun memo(
            title: String = fixtureMonkey.giveMeOne<String>(),
            description: String = fixtureMonkey.giveMeOne<String>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(
                    MemoLocalEntity::detail,
                    fixtureMonkey.giveMeOne<MemoDetailLocalEntity>().copy(title = title, description = description),
                ).setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun tag(
            emoji: String = "",
            title: String = fixtureMonkey.giveMeOne<String>(),
            description: String = fixtureMonkey.giveMeOne<String>(),
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(
                    TagLocalEntity::detail,
                    fixtureMonkey
                        .giveMeOne<TagDetailLocalEntity>()
                        .copy(emoji = emoji, title = title, description = description),
                ).setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun place(
            title: String = fixtureMonkey.giveMeOne<String>(),
            description: String = fixtureMonkey.giveMeOne<String>(),
            address: String = fixtureMonkey.giveMeOne<String>(),
            isDeleted: Boolean = false,
        ): PlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<PlaceLocalEntity>()
                .setExp(
                    PlaceLocalEntity::detail,
                    fixtureMonkey
                        .giveMeOne<PlaceDetailLocalEntity>()
                        .copy(title = title, description = description, address = address),
                ).setExp(PlaceLocalEntity::isDeleted, isDeleted)
                .setExp(PlaceLocalEntity::updatedAt, instant())
                .setExp(PlaceLocalEntity::createdAt, instant())
                .sample()

        private fun web(
            title: String = fixtureMonkey.giveMeOne<String>(),
            description: String = fixtureMonkey.giveMeOne<String>(),
            url: String = fixtureMonkey.giveMeOne<String>(),
            headerList: List<WebHeaderLocalEntity> = emptyList(),
            isDeleted: Boolean = false,
        ): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(
                    WebLocalEntity::detail,
                    WebDetailLocalEntity(
                        title = title,
                        description = description,
                        url = url,
                        headerList = headerList,
                    ),
                ).setExp(WebLocalEntity::isDeleted, isDeleted)
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun PlaceLocalEntity.located(
            latitude: Double,
            longitude: Double,
        ): PlaceLocalEntity = copy(detail = detail.copy(latitude = latitude, longitude = longitude))

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
        ): MemoTagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoTagLocalEntity>()
                .setExp(MemoTagLocalEntity::memoId, memoId)
                .setExp(MemoTagLocalEntity::tagId, tagId)
                .setExp(MemoTagLocalEntity::isDeleted, false)
                .setExp(MemoTagLocalEntity::updatedAt, instant())
                .setExp(MemoTagLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
