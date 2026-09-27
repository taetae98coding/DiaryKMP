package io.github.taetae98coding.diary.core.database.impl.tag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
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

        test("TC-MEMO-TAG-INPUT-DOMAIN-002 태그 목록은 수정 시각과 생성 시각에 관계없이 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-TAG-INPUT-DATA-003 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-TAG-INPUT-DOMAIN-001 선택 목록 페이지 조회도 계정과 연결되고 완료·삭제되지 않은 태그만 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectableTag = selectableTag()
            val finishedTag = tag().copy(isFinished = true, isDeleted = false)
            val deletedTag = tag().copy(isFinished = false, isDeleted = true)
            val otherAccountTag = selectableTag()

            transaction.upsert(accountId = accountId, tagList = listOf(selectableTag, finishedTag, deletedTag), tagLinkList = emptyList())
            transaction.upsert(accountId = otherAccountId, tagList = listOf(otherAccountTag), tagLinkList = emptyList())

            dataSource.page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).loadPage().data shouldBe listOf(selectableTag)
        }

        test("TC-MEMO-TAG-INPUT-DATA-004 선택한 식별자의 선택할 수 있는 태그만 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-TAG-INPUT-DATA-004 선택한 식별자가 없으면 빈 목록을 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.upsert(accountId = accountId, tagList = listOf(selectableTag()), tagLinkList = emptyList())

            dataSource.get(accountId = accountId, tagIdSet = emptySet()).first().shouldBeEmpty()
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-015 검색어는 이모지·제목·설명 중 하나 이상을 포함하는 태그만 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-TAG-INPUT-DATA-005 검색어를 만족하는 태그가 정렬 뒤쪽에 있어도 첫 페이지에 담긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-MEMO-TAG-INPUT-DATA-002 태그가 추가되거나 상태가 바뀌면 태그 목록 조회 결과가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = selectableTag()

            dataSource.pagedTagList(accountId = accountId).shouldBeEmpty()

            transaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            dataSource.pagedTagList(accountId = accountId) shouldBe listOf(tag)

            transaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = instant())
            dataSource.pagedTagList(accountId = accountId).shouldBeEmpty()
        }
    }) {
    public companion object {
        private const val TAG_COUNT: Int = 25
        private const val PAGE_SIZE: Int = 10

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private suspend fun AccountTagLocalDataSourceImpl.pagedTagList(accountId: Uuid): List<TagLocalEntity> = page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT).loadPage().data

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
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun selectableTag(): TagLocalEntity = tag().copy(isFinished = false, isDeleted = false)

        private fun TagLocalEntity.withTitle(title: String): TagLocalEntity = copy(detail = detail.copy(title = title))

        private fun TagLocalEntity.withDetail(
            emoji: String,
            title: String,
            description: String,
        ): TagLocalEntity = copy(detail = detail.copy(emoji = emoji, title = title, description = description))

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
