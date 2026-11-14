package io.github.taetae98coding.diary.core.database.impl.tag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.tagDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagLocalDataSourceImpl
        lateinit var transaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountTagLocalDataSourceImpl(database = database)
            transaction = AccountTagTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        // 열어 둔 선택 목록이 스스로 다시 조회되는지는 목록 조회가 무효화되는지로 확인한다.
        suspend fun awaitTagPageInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT)
            pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = PAGE_SIZE, placeholdersEnabled = false))
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
        }

        test("TC-be0e29f8-7742-4e50-8d0e-a4de57cd8c6d 태그 목록은 수정 시각과 생성 시각에 관계없이 제목 오름차순으로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val bravoTag =
                selectableTag()
                    .withTitle("Bravo")
                    .copy(
                        updatedAt = Instant.fromEpochMilliseconds(3_000),
                        createdAt = Instant.fromEpochMilliseconds(3_000),
                    )
            val alphaTag =
                selectableTag()
                    .withTitle("Alpha")
                    .copy(
                        updatedAt = Instant.fromEpochMilliseconds(1_000),
                        createdAt = Instant.fromEpochMilliseconds(1_000),
                    )
            val charlieTag =
                selectableTag()
                    .withTitle("Charlie")
                    .copy(
                        updatedAt = Instant.fromEpochMilliseconds(2_000),
                        createdAt = Instant.fromEpochMilliseconds(2_000),
                    )

            transaction.upsert(accountId = accountId, tagList = listOf(bravoTag, alphaTag, charlieTag), tagLinkList = emptyList())

            dataSource.pagedTagList(accountId = accountId) shouldBe
                listOf(alphaTag, bravoTag, charlieTag)
        }

        test("TC-36238c18-3987-4f50-9051-4272285d7c16 TC-5cc6759b-36d2-4796-8f7e-17088d9e99e9 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList =
                List(TAG_COUNT) { index ->
                    selectableTag().withTitle("Tag-${index.toString().padStart(length = 3, padChar = '0')}")
                }.reversed()
            val expected = tagList.sortedBy { tag -> tag.detail.title }

            transaction.upsert(accountId = accountId, tagList = tagList, tagLinkList = emptyList())

            val firstPage = dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).loadPage(key = null, loadSize = PAGE_SIZE)

            firstPage.data shouldBe expected.take(PAGE_SIZE)
            firstPage.nextKey shouldBe PAGE_SIZE

            val secondPage = dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).loadPage(key = firstPage.nextKey, loadSize = PAGE_SIZE)

            secondPage.data shouldBe expected.drop(PAGE_SIZE).take(PAGE_SIZE)
        }

        test("TC-7fc44645-d468-48c4-b2da-4d65f3e9f1bb TC-5f57b3f0-f4ef-423a-8da1-3a88b34048cf 선택 목록 페이지 조회도 계정과 연결되고 완료·삭제되지 않은 태그만 가져온다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val selectableTag = selectableTag()
            val finishedTag = tag().copy(isFinished = true, isDeleted = false)
            val deletedTag = tag().copy(isFinished = false, isDeleted = true)
            val otherAccountTag = selectableTag()

            transaction.upsert(accountId = accountId, tagList = listOf(selectableTag, finishedTag, deletedTag), tagLinkList = emptyList())
            transaction.upsert(accountId = otherAccountId, tagList = listOf(otherAccountTag), tagLinkList = emptyList())

            dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe listOf(selectableTag)
        }

        test("TC-15e61e6f-8dc1-47f3-96fe-04dcdfd33502 TC-d4f72f37-9bfa-4fc3-ad02-c0c6ccbf1e46 선택한 식별자의 선택할 수 있는 태그만 제목 오름차순으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val selectedLastTag = selectableTag().withTitle("Zebra")
            val selectedFirstTag = selectableTag().withTitle("Alpha")
            val unselectedTag = selectableTag().withTitle("Bravo")
            val selectedFinishedTag = tag().copy(isFinished = true, isDeleted = false)

            transaction.upsert(
                accountId = accountId,
                tagList = listOf(selectedLastTag, selectedFirstTag, unselectedTag, selectedFinishedTag),
                tagLinkList = emptyList(),
            )

            dataSource
                .get(
                    accountId = accountId,
                    tagIdSet = setOf(selectedLastTag.id, selectedFirstTag.id, selectedFinishedTag.id),
                ).first() shouldBe listOf(selectedFirstTag, selectedLastTag)
        }

        test("TC-15e61e6f-8dc1-47f3-96fe-04dcdfd33502 선택한 식별자가 없으면 빈 목록을 조회한다") {
            val accountId = Arb.kotlinUuid().next()

            transaction.upsert(accountId = accountId, tagList = listOf(selectableTag()), tagLinkList = emptyList())

            dataSource.get(accountId = accountId, tagIdSet = emptySet()).first().shouldBeEmpty()
        }

        test("TC-5eaf66e6-583e-4d17-9bdc-e1a9fd38bf34 검색어는 이모지·제목·설명 중 하나 이상을 포함하는 태그만 남긴다") {
            val accountId = Arb.kotlinUuid().next()
            val titleTag = selectableTag().withDetail(emoji = "", title = "Travel", description = "")
            val emojiTag = selectableTag().withDetail(emoji = "✈️", title = "AlphaTag", description = "")
            val descriptionTag = selectableTag().withDetail(emoji = "", title = "BetaTag", description = "여행 기록")
            val otherTag = selectableTag().withDetail(emoji = "", title = "GammaTag", description = "")
            val koreanTitleTag = selectableTag().withDetail(emoji = "", title = "여행 기록", description = "")

            transaction.upsert(
                accountId = accountId,
                tagList = listOf(titleTag, emojiTag, descriptionTag, otherTag, koreanTitleTag),
                tagLinkList = emptyList(),
            )

            dataSource.page(accountId = accountId, query = "trav", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe listOf(titleTag)
            dataSource.page(accountId = accountId, query = "✈️", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe listOf(emojiTag)
            dataSource.page(accountId = accountId, query = "여행", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe listOf(descriptionTag, koreanTitleTag)
            dataSource.page(accountId = accountId, query = "업무", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe emptyList()
            dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe listOf(emojiTag, descriptionTag, otherTag, titleTag, koreanTitleTag)
        }

        test("TC-ecf420f3-f92b-4577-8542-1b176636869a TC-c5dcf9a0-63c2-4ee5-a895-5dc4f6978c42 검색어를 만족하는 태그가 정렬 뒤쪽에 있어도 첫 페이지에 담긴다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList =
                List(TAG_COUNT) { index ->
                    selectableTag().withDetail(
                        emoji = "",
                        title = "Tag-${index.toString().padStart(length = 3, padChar = '0')}",
                        description = "",
                    )
                }
            val target = selectableTag().withDetail(emoji = "", title = "ZebraTravel", description = "")

            transaction.upsert(accountId = accountId, tagList = tagList + target, tagLinkList = emptyList())

            dataSource.page(accountId = accountId, query = "travel", sort = ListSortLocalEntity.DEFAULT).loadPage(loadSize = PAGE_SIZE).data shouldBe listOf(target)
        }

        test("TC-27d25f56-f19b-42c9-b786-43f19557d404 저장된 태그의 내용이 바뀌면 선택한 태그 조회와 선택 목록 조회에 함께 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = selectableTag()
            val changedDetail = Arb.tagDetailLocalEntity().next()
            val updatedAt = Arb.secondPrecisionInstant().next()
            transaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())

            transaction.updateDetail(accountId = accountId, tagId = tag.id, detail = changedDetail, updatedAt = updatedAt)

            val changedTag = tag.copy(detail = changedDetail, updatedAt = updatedAt)
            dataSource.get(accountId = accountId, tagIdSet = setOf(tag.id)).first() shouldBe listOf(changedTag)
            dataSource.pagedTagList(accountId = accountId) shouldBe listOf(changedTag)
        }

        test("TC-a79a3e06-9752-4c82-8ff3-8e9b880c87ec 태그가 추가되거나 이모지·제목이 바뀌면 선택한 태그 조회와 열어 둔 선택 목록이 스스로 갱신된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = selectableTag().withTitle("Bravo")
            val addedTag = selectableTag().withTitle("Charlie")
            transaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())

            dataSource.get(accountId = accountId, tagIdSet = setOf(tag.id, addedTag.id)).distinctUntilChanged().test {
                awaitItem() shouldBe listOf(tag)

                awaitTagPageInvalidated(accountId = accountId) {
                    transaction.upsert(accountId = accountId, tagList = listOf(addedTag), tagLinkList = emptyList())
                }

                awaitItem() shouldBe listOf(tag, addedTag)
                dataSource.pagedTagList(accountId = accountId) shouldBe listOf(tag, addedTag)

                val changedDetail = tag.detail.copy(emoji = "🧪", title = "Delta")
                val updatedAt = Arb.secondPrecisionInstant().next()
                awaitTagPageInvalidated(accountId = accountId) {
                    transaction.updateDetail(accountId = accountId, tagId = tag.id, detail = changedDetail, updatedAt = updatedAt)
                }

                val changedTag = tag.copy(detail = changedDetail, updatedAt = updatedAt)
                awaitItem() shouldBe listOf(addedTag, changedTag)
                dataSource.pagedTagList(accountId = accountId) shouldBe listOf(addedTag, changedTag)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("새로 저장된 태그가 열어 둔 선택 목록의 정렬 위치에 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val firstTag = selectableTag().withTitle("Alpha")
            val lastTag = selectableTag().withTitle("Echo")
            val addedTag = selectableTag().withTitle("Bravo")
            val pulledTag = selectableTag().withTitle("Charlie")
            transaction.upsert(accountId = accountId, tagList = listOf(firstTag, lastTag), tagLinkList = emptyList())
            dataSource.pagedTagList(accountId = accountId) shouldBe listOf(firstTag, lastTag)

            awaitTagPageInvalidated(accountId = accountId) {
                transaction.upsert(accountId = accountId, tagList = listOf(addedTag), tagLinkList = emptyList())
            }
            dataSource.pagedTagList(accountId = accountId) shouldBe listOf(firstTag, addedTag, lastTag)

            awaitTagPageInvalidated(accountId = accountId) {
                AccountTagSyncTransactionImpl(database = database).upsert(accountId = accountId, tagList = listOf(pulledTag), cursor = Arb.long().next())
            }
            dataSource.pagedTagList(accountId = accountId) shouldBe listOf(firstTag, addedTag, pulledTag, lastTag)
        }

        test("TC-6104ae0a-c9a0-423f-bfe1-fb75276db910 최근 수정순에서는 제목이 바뀌지 않는 수정에도 태그가 목록의 앞으로 옮겨진다") {
            val accountId = Arb.kotlinUuid().next()
            val updatedAt = Arb.secondPrecisionInstant().next()
            val olderTag = selectableTag().copy(updatedAt = updatedAt)
            val newerTag = selectableTag().copy(updatedAt = updatedAt + 1.seconds)
            transaction.upsert(accountId = accountId, tagList = listOf(olderTag, newerTag), tagLinkList = emptyList())
            dataSource.pagedTagList(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(newerTag, olderTag)

            val changedTag = olderTag.copy(detail = olderTag.detail.copy(description = Arb.nonBlankString().next()), updatedAt = updatedAt + 2.seconds)
            transaction.updateDetail(accountId = accountId, tagId = changedTag.id, detail = changedTag.detail, updatedAt = changedTag.updatedAt)

            dataSource.pagedTagList(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe listOf(changedTag, newerTag)
        }

        test("TC-ff2c4657-771f-4c5b-90e7-973294e5a60b 제목순에서는 제목이 바뀌지 않는 수정에 태그의 순서가 바뀌지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val updatedAt = Arb.secondPrecisionInstant().next()
            val tagList =
                listOf(
                    selectableTag().withTitle(Arb.alphanumericString(prefix = "First").next()).copy(updatedAt = updatedAt),
                    selectableTag().withTitle(Arb.alphanumericString(prefix = "Second").next()).copy(updatedAt = updatedAt + 1.seconds),
                )
            transaction.upsert(accountId = accountId, tagList = tagList, tagLinkList = emptyList())
            val (firstTag, lastTag) = dataSource.pagedTagList(accountId = accountId, sort = ListSortLocalEntity.TITLE)

            val changedTag = lastTag.copy(detail = lastTag.detail.copy(description = Arb.nonBlankString().next()), updatedAt = updatedAt + 2.seconds)
            transaction.updateDetail(accountId = accountId, tagId = changedTag.id, detail = changedTag.detail, updatedAt = changedTag.updatedAt)

            dataSource.pagedTagList(accountId = accountId, sort = ListSortLocalEntity.TITLE) shouldBe listOf(firstTag, changedTag)
        }
    }) {
    public companion object {
        private const val TAG_COUNT: Int = 25
        private const val PAGE_SIZE: Int = 10
        private const val INVALIDATION_TIMEOUT_MILLIS: Long = 30_000L

        private suspend fun AccountTagLocalDataSourceImpl.pagedTagList(
            accountId: Uuid,
            sort: ListSortLocalEntity = ListSortLocalEntity.DEFAULT,
        ): List<TagLocalEntity> = page(accountId = accountId, query = "", sort = sort).loadPage().data

        private suspend fun PagingSource<Int, TagLocalEntity>.loadPage(
            key: Int? = null,
            loadSize: Int = 100,
        ): PagingSource.LoadResult.Page<Int, TagLocalEntity> {
            val params: PagingSource.LoadParams<Int> =
                if (key == null) {
                    PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
                } else {
                    PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
                }

            return load(params).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>()
        }

        private fun tag(): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun selectableTag(): TagLocalEntity = tag().copy(isFinished = false, isDeleted = false)

        private fun TagLocalEntity.withTitle(title: String): TagLocalEntity = copy(detail = detail.copy(title = title))

        private fun TagLocalEntity.withDetail(
            emoji: String,
            title: String,
            description: String,
        ): TagLocalEntity = copy(detail = detail.copy(emoji = emoji, title = title, description = description))
    }
}
