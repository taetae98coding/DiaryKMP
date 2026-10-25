package io.github.taetae98coding.diary.core.database.impl.memotag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
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

        test("TC-5282a1f8-8c3c-4dd2-b017-585e42c532b0 계정과 연결된 메모의 해제되지 않은 연결의 태그를 모두 조회한다") {
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

        test("TC-278d2a9d-3386-4cee-a542-56f2577f0050 해제된 연결의 태그는 조회되지 않는다") {
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

        test("TC-23953088-b463-4072-9ca9-13be23784eb2 가리키는 메모가 기기에 없는 연결은 메모별 태그 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            database.memoTagDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            insertMemo(accountId = accountId, memo = memo)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(tag)
        }

        test("TC-7a2f780f-348c-4006-8b7e-ea46cd179c08 계정과 연결되지 않은 태그를 메모에 연결해도 메모의 연결된 태그로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val otherAccountTag = tag()
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(accountId = otherAccountId, tagList = listOf(otherAccountTag), tagLinkList = emptyList())

            memoTagTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = otherAccountTag.id,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            )

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-7fc44645-d468-48c4-b2da-4d65f3e9f1bb 다른 계정의 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag())

            dataSource.getTagList(accountId = otherAccountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-a96e6ab1-53aa-412b-8818-9efac6db49e3 TC-76d211fc-cc39-4af9-ae09-da3e22841bda 완료된 태그의 연결도 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            val finishedTag = tag.copy(isFinished = true)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(finishedTag), tagLinkList = emptyList())

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldContainExactlyInAnyOrder listOf(finishedTag)
        }

        test("TC-a96e6ab1-53aa-412b-8818-9efac6db49e3 TC-fac3274a-65c0-48b6-a0aa-e43b7e597803 TC-76d211fc-cc39-4af9-ae09-da3e22841bda 삭제된 태그의 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)

            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag.copy(isDeleted = true)), tagLinkList = emptyList())

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-ee72205f-e02d-4403-b4ad-b743994d46bb 삭제된 태그의 삭제가 다른 기기에서 받은 내용으로 풀리면 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag.copy(isDeleted = true)), tagLinkList = emptyList())
            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            AccountTagSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                tagList = listOf(tag.copy(isDeleted = false)),
                cursor = 1L,
            )

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(tag)
        }

        test("TC-038d714a-47cc-4ba1-ba9a-0531f3dc10bc 태그의 이모지·제목·설명·컬러를 수정해도 메모와의 연결은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag)
            val memoTagList = database.memoTagDao().findByMemoIdList(listOf(memo.id))
            val newDetail =
                TagDetailLocalEntity(
                    emoji = "✈️",
                    title = "edited-${fixtureMonkey.giveMeOne<Uuid>()}",
                    description = "edited-${fixtureMonkey.giveMeOne<Uuid>()}",
                    color = tag.detail.color.inv(),
                )

            tagTransaction.updateDetail(accountId = accountId, tagId = tag.id, detail = newDetail, updatedAt = instant())

            dataSource
                .getTagList(accountId = accountId, memoId = memo.id)
                .first()
                .map { connectedTag -> connectedTag.id to connectedTag.detail } shouldBe listOf(tag.id to newDetail)
            database.memoTagDao().findByMemoIdList(listOf(memo.id)) shouldBe memoTagList
        }

        listOf<Pair<String, Boolean>>(
            "연결되지 않은 태그 하나를 새로 연결하면" to false,
            "연결된 태그 하나의 연결을 해제하면" to true,
        ).forEach { (label, isDeleted) ->
            test("TC-0c6f0881-63d7-456b-8439-9c1d76999476 TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 $label 그 연결만 업로드 대기가 된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo().copy(primaryTagId = null)
                val keptTag = tag()
                val removedTag = tag()
                val addedTag = tag()
                insertMemo(accountId = accountId, memo = memo)
                connect(accountId = accountId, memoId = memo.id, tag = keptTag)
                connect(accountId = accountId, memoId = memo.id, tag = removedTag)
                tagTransaction.upsert(accountId = accountId, tagList = listOf(addedTag), tagLinkList = emptyList())
                AccountMemoTagSyncTransactionImpl(database = database).clearPending(
                    accountId = accountId,
                    memoTagList = database.memoTagDao().findByMemoIdList(listOf(memo.id)),
                )
                val syncDataSource = AccountMemoTagSyncLocalDataSourceImpl(database = database)
                syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
                val changedTagId = if (isDeleted) removedTag.id else addedTag.id

                memoTagTransaction.upsert(
                    accountId = accountId,
                    memoId = memo.id,
                    tagId = changedTagId,
                    isDeleted = isDeleted,
                    updatedAt = instant(),
                )

                syncDataSource.readPendingList(accountId = accountId).map { memoTag -> memoTag.tagId } shouldBe listOf(changedTagId)
            }
        }

        test("TC-bc8feb4b-8381-4ed7-ad9e-2b3c78442d89 연결된 태그가 완료되어도 조회 결과에 남는다") {
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

        test("TC-be0e29f8-7742-4e50-8d0e-a4de57cd8c6d TC-d2942532-4b96-432c-8b0a-9acd2d608c22 조회한 태그는 제목 오름차순으로 정렬된다") {
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

        test("TC-d6c69891-28f5-4a36-b188-e01dbff987e4 계정과 연결된 메모의 해제되지 않은 연결의 태그 id를 모두 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstTag = tag()
            val secondTag = tag()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = firstTag)
            connect(accountId = accountId, memoId = memo.id, tag = secondTag)

            dataSource.readTagIdList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder listOf(firstTag.id, secondTag.id)
        }

        test("TC-d6c69891-28f5-4a36-b188-e01dbff987e4 해제된 연결의 태그 id는 조회되지 않는다") {
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

            dataSource.readTagIdList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        listOf(
            "완료된" to { tag: TagLocalEntity -> tag.copy(isFinished = true) },
            "삭제된" to { tag: TagLocalEntity -> tag.copy(isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-d6c69891-28f5-4a36-b188-e01dbff987e4 $label 태그의 해제되지 않은 연결도 태그 id로 조회된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val tag = tag()
                insertMemo(accountId = accountId, memo = memo)
                connect(accountId = accountId, memoId = memo.id, tag = tag)

                tagTransaction.upsert(accountId = accountId, tagList = listOf(change(tag)), tagLinkList = emptyList())

                dataSource.readTagIdList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder listOf(tag.id)
            }
        }

        test("TC-d6c69891-28f5-4a36-b188-e01dbff987e4 다른 계정의 연결은 태그 id로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag())

            dataSource.readTagIdList(accountId = otherAccountId, memoId = memo.id).shouldBeEmpty()
        }

        test("다른 메모의 연결은 태그 id로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val otherMemo = memo()
            insertMemo(accountId = accountId, memo = memo)
            insertMemo(accountId = accountId, memo = otherMemo)
            connect(accountId = accountId, memoId = otherMemo.id, tag = tag())

            dataSource.readTagIdList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        test("TC-8a88520a-5d49-4adb-abcc-d21a05f7ead8 TC-5b370c4c-d285-41ba-965b-da7e6ecf8c95 선택 목록에는 선택할 수 있는 태그와 연결된 완료된 태그가 함께 담긴다") {
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

        test("TC-eef3ed3f-7869-4ba5-afd7-8c0158d21cce TC-c8b46a53-3227-4f18-9061-defb7a9cf9de 연결되지 않은 완료된 태그는 선택 목록에 담기지 않는다") {
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

        test("TC-fac3274a-65c0-48b6-a0aa-e43b7e597803 삭제된 태그는 연결이 남아 있어도 선택 목록에 담기지 않는다") {
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

        test("TC-b0aa3cdf-77a4-449a-bb0e-38fcff2ecbdf 선택 목록은 완료 여부로 구분하지 않고 제목 오름차순으로 정렬한다") {
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

        test("TC-435c14a0-7434-48a6-9775-b8910c85738a 연결이 해제되면 완료된 태그가 선택 목록에서 빠진다") {
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

        test("TC-7fc44645-d468-48c4-b2da-4d65f3e9f1bb 다른 계정의 태그는 선택 목록에 담기지 않는다") {
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

        test("TC-36238c18-3987-4f50-9051-4272285d7c16 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
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

        test("TC-5eaf66e6-583e-4d17-9bdc-e1a9fd38bf34 검색어는 이모지·제목·설명 중 하나 이상을 포함하는 태그만 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val titleTag = tag().withDetail(emoji = "", title = "Travel", description = "")
            val emojiTag = tag().withDetail(emoji = "✈️", title = "AlphaTag", description = "")
            val descriptionTag = tag().withDetail(emoji = "", title = "BetaTag", description = "여행 기록")
            val otherTag = tag().withDetail(emoji = "", title = "GammaTag", description = "")
            val koreanTitleTag = tag().withDetail(emoji = "", title = "여행 기록", description = "")
            insertMemo(accountId = accountId, memo = memo)
            tagTransaction.upsert(
                accountId = accountId,
                tagList = listOf(titleTag, emojiTag, descriptionTag, otherTag, koreanTitleTag),
                tagLinkList = emptyList(),
            )

            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "trav").loadPage().data shouldBe listOf(titleTag)
            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "✈️").loadPage().data shouldBe listOf(emojiTag)
            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "여행").loadPage().data shouldBe listOf(descriptionTag, koreanTitleTag)
            dataSource.pageSelectableTag(accountId = accountId, memoId = memo.id, query = "업무").loadPage().data shouldBe emptyList()
            dataSource
                .pageSelectableTag(accountId = accountId, memoId = memo.id, query = "")
                .loadPage()
                .data shouldBe listOf(emojiTag, descriptionTag, otherTag, titleTag, koreanTitleTag)
        }

        test("TC-6fead397-e8d2-4843-bca0-d62477cafcce 검색어를 만족하지 않으면 연결된 완료된 태그도 선택 목록에서 빠진다") {
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

        test("TC-ecf420f3-f92b-4577-8542-1b176636869a 검색어를 만족하는 태그가 정렬 뒤쪽에 있어도 첫 페이지에 담긴다") {
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

        test("TC-ef9f8c54-750a-42b9-9491-ef622a6f420d 연결이 바뀌면 조회 결과가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val tag = tag()
            insertMemo(accountId = accountId, memo = memo)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            connect(accountId = accountId, memoId = memo.id, tag = tag)

            dataSource.getTagList(accountId = accountId, memoId = memo.id).first() shouldContainExactlyInAnyOrder listOf(tag)
        }

        test("TC-546496c5-218d-4e17-ae7b-937df11b75ef 필터를 고르지 않은 메모 목록에서 태그 연결은 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val linkedMemo = memo().visible().withTitle(title = linkedTitle)
                val unlinkedMemo = memo().visible().withTitle(title = unlinkedTitle)
                insertMemo(accountId = accountId, memo = linkedMemo)
                insertMemo(accountId = accountId, memo = unlinkedMemo)
                connect(accountId = accountId, memoId = linkedMemo.id, tag = tag())

                val memoList =
                    database
                        .accountMemoDao()
                        .page(accountId = accountId, sort = "title")
                        .loadAll()

                memoList.map { memo -> memo.id } shouldBe listOf(linkedMemo, unlinkedMemo).sortedBy { memo -> memo.detail.title }.map { memo -> memo.id }
            }
        }

        test("TC-215cd5bd-6759-4904-9b87-33b084d3e62f 태그 필터를 고르지 않은 캘린더에서 태그 연결은 메모 노출을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val start = LocalDateTime(year = 2026, month = 9, day = 25, hour = 9, minute = 0)
            val endInclusive = LocalDateTime(year = 2026, month = 9, day = 26, hour = 9, minute = 0)
            val linkedMemo = memo().visible().withPeriod(start = start, endInclusive = endInclusive)
            val unlinkedMemo = memo().visible().withPeriod(start = start, endInclusive = endInclusive)
            insertMemo(accountId = accountId, memo = linkedMemo)
            insertMemo(accountId = accountId, memo = unlinkedMemo)
            connect(accountId = accountId, memoId = linkedMemo.id, tag = tag())

            val calendarMemoList =
                database
                    .accountCalendarMemoDao()
                    .get(accountId = accountId, start = start.date, endInclusive = endInclusive.date)
                    .first()

            calendarMemoList.map { memo -> memo.id } shouldContainExactlyInAnyOrder listOf(linkedMemo.id, unlinkedMemo.id)
        }

        test("TC-953b1503-454f-4047-abff-2a6cd8878c14 연결된 태그의 제목은 메모 검색에 쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val query = "query-${fixtureMonkey.giveMeOne<Uuid>()}"
            val memo = memo().visible().let { value -> value.copy(detail = value.detail.copy(title = "memo-title", description = "memo-description")) }
            insertMemo(accountId = accountId, memo = memo)
            connect(accountId = accountId, memoId = memo.id, tag = tag().withTitle(title = "tag-$query"))

            val memoList =
                database
                    .searchMemoDao()
                    .page(accountId = accountId, query = query, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
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

        private fun MemoLocalEntity.visible(): MemoLocalEntity = copy(isFinished = false, isDeleted = false)

        private fun MemoLocalEntity.withTitle(title: String): MemoLocalEntity = copy(detail = detail.copy(title = title))

        private fun MemoLocalEntity.withPeriod(
            start: LocalDateTime,
            endInclusive: LocalDateTime,
        ): MemoLocalEntity = copy(detail = detail.copy(isAllDay = false, start = start, endInclusive = endInclusive))

        private suspend fun <T : Any> PagingSource<Int, T>.loadAll(): List<T> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()
                .data

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

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
