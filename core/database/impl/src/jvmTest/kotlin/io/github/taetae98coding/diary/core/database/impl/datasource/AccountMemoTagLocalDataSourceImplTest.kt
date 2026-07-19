package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMemoTagLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountMemoTagLocalDataSourceImpl
        lateinit var memoTagTransaction: AccountMemoTagTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountMemoTagLocalDataSourceImpl(database = database)
            memoTagTransaction = AccountMemoTagTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemo(
            accountId: Uuid,
            memo: MemoLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(listOf(memo))
                database.accountMemoDao().upsert(
                    listOf(AccountMemoLocalEntity(accountId = accountId, memoId = memo.id, isDirty = false)),
                )
            }
        }

        suspend fun connect(
            accountId: Uuid,
            memoId: Uuid,
            tag: TagLocalEntity,
        ) {
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            memoTagTransaction.upsert(
                accountId = accountId,
                memoId = memoId,
                tagId = tag.id,
                isDeleted = false,
                updatedAt = instant(),
            )
        }

        test("TC-MEMO-DETAIL-FEATURE-033 계정과 연결된 메모의 해제되지 않은 연결의 태그를 모두 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstTag = tag()
            val secondTag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = firstTag)
            connect(accountId = accountId, memoId = memo.id, tag = secondTag)

            dataSource
                .getTagList(accountId = accountId, memoId = memo.id)
                .first() shouldContainExactlyInAnyOrder listOf(firstTag, secondTag)
        }

        test("TC-MEMO-DETAIL-DATA-005 해제된 연결의 태그는 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            memoTagTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-TAG-DATA-010 가리키는 메모가 기기에 없는 연결은 메모별 태그 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            database.memoTagDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            insertMemo(accountId = accountId, memo = memo)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(tag)
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-001 TC-MEMO-TAG-DOMAIN-011 다른 계정의 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag())

            dataSource.getTagList(accountId = otherAccountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-DETAIL-DATA-018 TC-MEMO-TAG-DOMAIN-012 완료된 태그의 연결도 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            val finishedTag = tag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedTag), tagLinkList = emptyList())

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldContainExactlyInAnyOrder listOf(finishedTag)
        }

        test("TC-MEMO-DETAIL-DATA-018 TC-MEMO-DETAIL-FEATURE-049 TC-MEMO-TAG-DOMAIN-012 삭제된 태그의 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag.copy(isDeleted = true)), tagLinkList = emptyList())

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-DETAIL-DATA-019 연결된 태그가 완료되어도 조회 결과에 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldContainExactlyInAnyOrder listOf(tag)

            val finishedTag = tag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedTag), tagLinkList = emptyList())

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldContainExactlyInAnyOrder listOf(finishedTag)
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-002 TC-MEMO-TAG-DOMAIN-013 조회한 태그는 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val lastTag = tag().let { tag -> tag.copy(detail = tag.detail.copy(title = LAST_TAG_TITLE)) }
            val firstTag = tag().let { tag -> tag.copy(detail = tag.detail.copy(title = FIRST_TAG_TITLE)) }
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = lastTag)
            connect(accountId = accountId, memoId = memo.id, tag = firstTag)

            dataSource
                .getTagList(accountId = accountId, memoId = memo.id)
                .first()
                .map { tag -> tag.detail.title } shouldBe listOf(FIRST_TAG_TITLE, LAST_TAG_TITLE)
        }

        test("TC-MEMO-DETAIL-DATA-015 계정과 연결된 메모의 해제되지 않은 연결의 태그 id를 모두 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstTag = tag()
            val secondTag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = firstTag)
            connect(accountId = accountId, memoId = memo.id, tag = secondTag)

            dataSource.findTagIdList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder listOf(firstTag.id, secondTag.id)
        }

        test("TC-MEMO-DETAIL-DATA-015 해제된 연결의 태그 id는 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            memoTagTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            dataSource.findTagIdList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        listOf(
            "완료된" to { tag: TagLocalEntity -> tag.copy(isFinished = true) },
            "삭제된" to { tag: TagLocalEntity -> tag.copy(isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-MEMO-DETAIL-DATA-015 $label 태그의 해제되지 않은 연결도 태그 id로 조회된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val tag = tag()
                insertMemo(accountId = accountId, memo = memo)
                connect(accountId = accountId, memoId = memo.id, tag = tag)

                tagTransaction.upsert(accountId = accountId, tagList = listOf(change(tag)), tagLinkList = emptyList())

                dataSource.findTagIdList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder listOf(tag.id)
            }
        }

        test("TC-MEMO-DETAIL-DATA-015 다른 계정의 연결은 태그 id로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag())

            dataSource.findTagIdList(accountId = otherAccountId, memoId = memo.id).shouldBeEmpty()
        }

        test("다른 메모의 연결은 태그 id로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val otherMemo = memo()
            insertMemo(accountId = accountId, memo = memo)
            insertMemo(accountId = accountId, memo = otherMemo)
            connect(accountId = accountId, memoId = otherMemo.id, tag = tag())

            dataSource.findTagIdList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-009 TC-MEMO-DETAIL-FEATURE-051 선택 목록에는 선택할 수 있는 태그와 연결된 완료된 태그가 함께 담긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val selectableTag = tag().withTitle(FIRST_TAG_TITLE)
            val connectedFinishedTag = tag().withTitle(LAST_TAG_TITLE)
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(selectableTag), tagLinkList = emptyList())
            connect(accountId = accountId, memoId = memo.id, tag = connectedFinishedTag)

            val finishedTag = connectedFinishedTag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedTag), tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data shouldBe listOf(selectableTag, finishedTag)
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-008 TC-MEMO-DETAIL-FEATURE-052 연결되지 않은 완료된 태그는 선택 목록에 담기지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val selectableTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(selectableTag, finishedTag), tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data shouldBe listOf(selectableTag)
        }

        test("TC-MEMO-DETAIL-FEATURE-049 삭제된 태그는 연결이 남아 있어도 선택 목록에 담기지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag.copy(isDeleted = true)), tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data
                .shouldBeEmpty()
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-010 선택 목록은 완료 여부로 구분하지 않고 제목 오름차순으로 정렬한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstTag = tag().withTitle(FIRST_TAG_TITLE)
            val middleTag = tag().withTitle(MIDDLE_TAG_TITLE)
            val lastTag = tag().withTitle(LAST_TAG_TITLE)
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(lastTag, firstTag), tagLinkList = emptyList())
            connect(accountId = accountId, memoId = memo.id, tag = middleTag)

            val finishedMiddleTag = middleTag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedMiddleTag), tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data shouldBe listOf(firstTag, finishedMiddleTag, lastTag)
        }

        test("TC-MEMO-DETAIL-FEATURE-053 연결이 해제되면 완료된 태그가 선택 목록에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            val finishedTag = tag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedTag), tagLinkList = emptyList())
            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "").loadPage().data shouldBe listOf(finishedTag)

            memoTagTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data
                .shouldBeEmpty()
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-001 다른 계정의 태그는 선택 목록에 담기지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = otherAccountId, tagList = listOf(tag()), tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data
                .shouldBeEmpty()
        }

        test("TC-MEMO-TAG-INPUT-DATA-003 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tagList =
                List(TAG_COUNT) { index ->
                    tag().withTitle("Tag-${index.toString().padStart(length = 3, padChar = '0')}")
                }.reversed()
            val expected = tagList.sortedBy { tag -> tag.detail.title }
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = accountId, tagList = tagList, tagLinkList = emptyList())

            val firstPage = dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "").loadPage(loadSize = PAGE_SIZE)

            firstPage.data shouldBe expected.take(PAGE_SIZE)
            firstPage.nextKey shouldBe PAGE_SIZE

            val secondPage =
                dataSource
                    .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                    .loadPage(key = firstPage.nextKey, loadSize = PAGE_SIZE)

            secondPage.data shouldBe expected.drop(PAGE_SIZE).take(PAGE_SIZE)
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-015 검색어는 이모지·제목·설명 중 하나 이상을 포함하는 태그만 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val titleTag = tag().withDetail(emoji = "", title = "Travel", description = "")
            val emojiTag = tag().withDetail(emoji = "✈️", title = "AlphaTag", description = "")
            val descriptionTag = tag().withDetail(emoji = "", title = "BetaTag", description = "여행 기록")
            val otherTag = tag().withDetail(emoji = "", title = "GammaTag", description = "")
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(
                accountId = accountId,
                tagList = listOf(titleTag, emojiTag, descriptionTag, otherTag),
                tagLinkList = emptyList(),
            )

            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "trav").loadPage().data shouldBe listOf(titleTag)
            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "✈️").loadPage().data shouldBe listOf(emojiTag)
            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "여행").loadPage().data shouldBe listOf(descriptionTag)
            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data shouldBe listOf(emojiTag, descriptionTag, otherTag, titleTag)
        }

        test("TC-MEMO-TAG-INPUT-DOMAIN-017 검색어를 만족하지 않으면 연결된 완료된 태그도 선택 목록에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val selectableTag = tag().withDetail(emoji = "", title = "Travel", description = "")
            val connectedTag = tag().withDetail(emoji = "", title = "AlphaTag", description = "")
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(selectableTag), tagLinkList = emptyList())
            connect(accountId = accountId, memoId = memo.id, tag = connectedTag)

            val finishedConnectedTag = connectedTag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedConnectedTag), tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "trav")
                .loadPage()
                .data shouldBe listOf(selectableTag)
        }

        test("TC-MEMO-TAG-INPUT-DATA-005 검색어를 만족하는 태그가 정렬 뒤쪽에 있어도 첫 페이지에 담긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tagList =
                List(TAG_COUNT) { index ->
                    tag().withDetail(emoji = "", title = "Tag-${index.toString().padStart(length = 3, padChar = '0')}", description = "")
                }
            val target = tag().withDetail(emoji = "", title = "ZebraTravel", description = "")
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = accountId, tagList = tagList + target, tagLinkList = emptyList())

            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "travel")
                .loadPage(loadSize = PAGE_SIZE)
                .data shouldBe listOf(target)
        }

        test("TC-MEMO-DETAIL-FEATURE-038 연결이 바뀌면 조회 결과가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            connect(accountId = accountId, memoId = memo.id, tag = tag)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldContainExactlyInAnyOrder listOf(tag)
        }
    }) {
    public companion object {
        private const val FIRST_TAG_TITLE = "AppleTag"
        private const val MIDDLE_TAG_TITLE = "MangoTag"
        private const val LAST_TAG_TITLE = "ZebraTag"
        private const val TAG_COUNT: Int = 25
        private const val PAGE_SIZE: Int = 10

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun TagLocalEntity.withTitle(title: String): TagLocalEntity = copy(detail = detail.copy(title = title))

        private fun TagLocalEntity.withDetail(
            emoji: String,
            title: String,
            description: String,
        ): TagLocalEntity = copy(detail = detail.copy(emoji = emoji, title = title, description = description))

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

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
