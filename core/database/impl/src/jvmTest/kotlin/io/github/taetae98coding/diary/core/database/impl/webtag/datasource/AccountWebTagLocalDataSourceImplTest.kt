package io.github.taetae98coding.diary.core.database.impl.webtag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountTagMemoLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memoweb.transaction.AccountMemoWebTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.search.datasource.SearchWebLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.datasource.AccountTagWebLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.web.datasource.AccountWebLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.web.datasource.AccountWebSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.webtag.transaction.AccountWebTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.webtag.transaction.AccountWebTagTransactionImpl
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

class AccountWebTagLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountWebTagLocalDataSourceImpl
        lateinit var tagWebDataSource: AccountTagWebLocalDataSourceImpl
        lateinit var webTagTransaction: AccountWebTagTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var webTransaction: AccountWebTransactionImpl
        lateinit var webDataSource: AccountWebLocalDataSourceImpl
        lateinit var searchWebDataSource: SearchWebLocalDataSourceImpl
        lateinit var webSyncDataSource: AccountWebSyncLocalDataSourceImpl
        lateinit var webTagSyncDataSource: AccountWebTagSyncLocalDataSourceImpl
        lateinit var webSyncTransaction: AccountWebSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountWebTagLocalDataSourceImpl(database = database)
            tagWebDataSource = AccountTagWebLocalDataSourceImpl(database = database)
            webTagTransaction = AccountWebTagTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            webTransaction = AccountWebTransactionImpl(database = database)
            webDataSource = AccountWebLocalDataSourceImpl(database = database)
            searchWebDataSource = SearchWebLocalDataSourceImpl(database = database)
            webSyncDataSource = AccountWebSyncLocalDataSourceImpl(database = database)
            webTagSyncDataSource = AccountWebTagSyncLocalDataSourceImpl(database = database)
            webSyncTransaction = AccountWebSyncTransactionImpl(database = database)
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

        suspend fun insertWeb(
            accountId: Uuid,
            vararg webList: WebLocalEntity,
        ) {
            webTransaction.upsert(accountId = accountId, webList = webList.toList(), webTagList = emptyList())
        }

        suspend fun link(
            accountId: Uuid,
            webId: Uuid,
            tagId: Uuid,
        ) {
            webTagTransaction.upsert(
                accountId = accountId,
                webId = webId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = instant(),
            )
        }

        suspend fun unlink(
            accountId: Uuid,
            webId: Uuid,
            tagId: Uuid,
        ) {
            webTagTransaction.upsert(
                accountId = accountId,
                webId = webId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = instant(),
            )
        }

        suspend fun linkedTagList(
            accountId: Uuid,
            webId: Uuid,
        ): List<TagLocalEntity> = dataSource.getTagList(accountId = accountId, webId = webId).first()

        suspend fun linkedTagIdList(
            accountId: Uuid,
            webId: Uuid,
        ): List<Uuid> = linkedTagList(accountId = accountId, webId = webId).map { tag -> tag.id }

        suspend fun selectableTagIdList(
            accountId: Uuid,
            webId: Uuid,
            query: String = "",
        ): List<Uuid> =
            dataSource
                .pageSelectableTag(accountId = accountId, webId = webId, query = query)
                .pagedTagIdList()

        suspend fun tagWebIdList(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            tagWebDataSource
                .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                .pagedWebIdList()

        test("TC-WEB-TAG-DOMAIN-001 하나의 웹 항목에 여러 태그를 연결할 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val firstTag = tag()
            val secondTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, firstTag, secondTag)

            link(accountId = accountId, webId = web.id, tagId = firstTag.id)
            link(accountId = accountId, webId = web.id, tagId = secondTag.id)

            linkedTagIdList(accountId = accountId, webId = web.id) shouldContainExactlyInAnyOrder
                listOf(firstTag.id, secondTag.id)
        }

        test("TC-WEB-TAG-DOMAIN-002 하나의 태그를 여러 웹 항목에 연결할 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web()
            val secondWeb = web()
            val tag = tag()
            insertWeb(accountId, firstWeb, secondWeb)
            insertTag(accountId, tag)

            link(accountId = accountId, webId = firstWeb.id, tagId = tag.id)
            link(accountId = accountId, webId = secondWeb.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, webId = firstWeb.id) shouldBe listOf(tag.id)
            linkedTagIdList(accountId = accountId, webId = secondWeb.id) shouldBe listOf(tag.id)
        }

        test("TC-WEB-TAG-DOMAIN-003 같은 웹 항목과 태그의 연결은 중복되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)

            link(accountId = accountId, webId = web.id, tagId = tag.id)
            link(accountId = accountId, webId = web.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)
        }

        test("TC-WEB-TAG-DOMAIN-004 연결된 태그가 없는 웹 항목도 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWeb(accountId, web)

            linkedTagList(accountId = accountId, webId = web.id).shouldBeEmpty()
        }

        test("TC-WEB-TAG-DOMAIN-005 연결은 웹 항목과 태그의 내용을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)

            link(accountId = accountId, webId = web.id, tagId = tag.id)

            database.accountWebDao().find(accountId = accountId, webId = web.id).first() shouldBe web
            database.accountTagDao().find(accountId = accountId, tagId = tag.id).first() shouldBe tag
        }

        test("TC-WEB-TAG-DOMAIN-006 계정과 연결되지 않은 태그는 연결된 태그로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val otherAccountTag = tag()
            insertWeb(accountId, web)
            insertTag(otherAccountId, otherAccountTag)

            link(accountId = accountId, webId = web.id, tagId = otherAccountTag.id)

            linkedTagList(accountId = accountId, webId = web.id).shouldBeEmpty()
        }

        test("TC-WEB-TAG-DOMAIN-007 연결을 해제하면 조회되지 않고 다시 만들면 복구된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)
            link(accountId = accountId, webId = web.id, tagId = tag.id)

            unlink(accountId = accountId, webId = web.id, tagId = tag.id)
            linkedTagList(accountId = accountId, webId = web.id).shouldBeEmpty()

            link(accountId = accountId, webId = web.id, tagId = tag.id)
            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)
        }

        test("TC-WEB-TAG-DOMAIN-008 삭제된 태그는 조회되지 않고 완료된 태그는 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)
            link(accountId = accountId, webId = web.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())
            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)

            val deletedAt = instant()
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = deletedAt)
            linkedTagList(accountId = accountId, webId = web.id).shouldBeEmpty()

            AccountTagSyncTransactionImpl(database = database).save(
                accountId = accountId,
                tagList = listOf(tag.copy(isFinished = true, isDeleted = false, updatedAt = deletedAt)),
                cursor = fixtureMonkey.giveMeOne<Long>(),
            )
            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)
        }

        test("TC-WEB-TAG-DOMAIN-009 연결된 태그는 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val firstTag = tag(title = "a")
            val secondTag = tag(title = "b")
            val thirdTag = tag(title = "c")
            insertWeb(accountId, web)
            insertTag(accountId, thirdTag, firstTag, secondTag)
            link(accountId = accountId, webId = web.id, tagId = thirdTag.id)
            link(accountId = accountId, webId = web.id, tagId = firstTag.id)
            link(accountId = accountId, webId = web.id, tagId = secondTag.id)

            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe
                listOf(firstTag.id, secondTag.id, thirdTag.id)
        }

        test("TC-WEB-TAG-DOMAIN-010 기기에 없는 태그를 가리키는 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWeb(accountId, web)

            link(accountId = accountId, webId = web.id, tagId = fixtureMonkey.giveMeOne<Uuid>())

            linkedTagList(accountId = accountId, webId = web.id).shouldBeEmpty()
        }

        test("TC-WEB-TAG-DOMAIN-011 태그를 완료하거나 삭제해도 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)
            link(accountId = accountId, webId = web.id, tagId = tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = instant())

            database
                .webTagDao()
                .findByWebIdList(listOf(web.id))
                .single()
                .isDeleted shouldBe false

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = instant())

            database
                .webTagDao()
                .findByWebIdList(listOf(web.id))
                .single()
                .isDeleted shouldBe false
        }

        test("TC-WEB-TAG-DOMAIN-012 웹 항목을 삭제해도 태그 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)
            link(accountId = accountId, webId = web.id, tagId = tag.id)

            webTransaction.updateDeleted(accountId = accountId, webId = web.id, isDeleted = true, updatedAt = instant())

            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)
        }

        test("TC-WEB-TAG-DOMAIN-013 웹 항목의 내용 수정은 태그 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val firstTag = tag()
            val secondTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, firstTag, secondTag)
            link(accountId = accountId, webId = web.id, tagId = firstTag.id)
            link(accountId = accountId, webId = web.id, tagId = secondTag.id)

            webTransaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = fixtureMonkey.giveMeOne<WebDetailLocalEntity>(),
                updatedAt = instant(),
            )

            linkedTagIdList(accountId = accountId, webId = web.id) shouldContainExactlyInAnyOrder
                listOf(firstTag.id, secondTag.id)
        }

        test("TC-WEB-TAG-DOMAIN-015 태그와 태그의 연결을 따라 웹 항목이 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val web = web()
            insertTag(accountId, firstTag, secondTag)
            insertWeb(accountId, web)
            link(accountId = accountId, webId = web.id, tagId = secondTag.id)

            database.accountTagLinkDao().upsert(
                io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity(
                    accountId = accountId,
                    fromTagId = firstTag.id,
                    toTagId = secondTag.id,
                    isDirty = true,
                ),
            )

            tagWebIdList(accountId = accountId, tagId = firstTag.id).shouldBeEmpty()
            tagWebIdList(accountId = accountId, tagId = secondTag.id) shouldBe listOf(web.id)
        }

        test("TC-WEB-TAG-DOMAIN-016 웹 항목과 태그를 연결해도 그 웹 항목에 연결된 메모에는 태그가 연결되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, tag)
            AccountMemoTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
            )
            AccountMemoWebTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoId = memo.id,
                webId = web.id,
                isDeleted = false,
                updatedAt = instant(),
            )

            link(accountId = accountId, webId = web.id, tagId = tag.id)

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

        test("TC-WEB-TAG-DATA-003 연결 해제는 같은 웹 항목의 다른 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val removedTag = tag()
            val keptTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, removedTag, keptTag)
            link(accountId = accountId, webId = web.id, tagId = removedTag.id)
            link(accountId = accountId, webId = web.id, tagId = keptTag.id)

            unlink(accountId = accountId, webId = web.id, tagId = removedTag.id)

            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(keptTag.id)
        }

        test("TC-WEB-TAG-DATA-010 가리키는 항목이 기기에 없는 연결도 저장은 성공한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tag = tag()
            insertWeb(accountId, web)

            link(accountId = accountId, webId = web.id, tagId = tag.id)
            linkedTagList(accountId = accountId, webId = web.id).shouldBeEmpty()

            insertTag(accountId, tag)
            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(tag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-001 연결할 수 있는 태그는 계정과 연결된 완료·삭제되지 않은 태그다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val selectableTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            val deletedTag = tag().copy(isDeleted = true)
            val otherAccountTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, selectableTag, finishedTag, deletedTag)
            insertTag(otherAccountId, otherAccountTag)

            selectableTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(selectableTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-005 연결할 수 있는 태그에 없는 연결된 태그도 목록에 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val selectableTag = tag(title = "a")
            val finishedTag = tag(title = "b")
            insertWeb(accountId, web)
            insertTag(accountId, selectableTag, finishedTag)
            link(accountId = accountId, webId = web.id, tagId = finishedTag.id)
            tagTransaction.updateFinished(accountId = accountId, tagId = finishedTag.id, isFinished = true, updatedAt = instant())

            selectableTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(selectableTag.id, finishedTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-006 목록에 나타난 연결할 수 없는 태그의 연결을 해제하면 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val finishedTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, finishedTag)
            link(accountId = accountId, webId = web.id, tagId = finishedTag.id)
            tagTransaction.updateFinished(accountId = accountId, tagId = finishedTag.id, isFinished = true, updatedAt = instant())

            selectableTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(finishedTag.id)

            unlink(accountId = accountId, webId = web.id, tagId = finishedTag.id)

            selectableTagIdList(accountId = accountId, webId = web.id).shouldBeEmpty()
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-011 계정의 미완료·미삭제 태그는 하나도 제외되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val firstTag = tag(title = "a")
            val secondTag = tag(title = "b")
            insertWeb(accountId, web)
            insertTag(accountId, firstTag, secondTag)
            link(accountId = accountId, webId = web.id, tagId = firstTag.id)

            selectableTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(firstTag.id, secondTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-002 목록의 태그는 완료 여부로 구분하지 않고 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val firstTag = tag(title = "a")
            val finishedTag = tag(title = "b")
            val thirdTag = tag(title = "c")
            insertWeb(accountId, web)
            insertTag(accountId, thirdTag, finishedTag, firstTag)
            link(accountId = accountId, webId = web.id, tagId = finishedTag.id)
            tagTransaction.updateFinished(accountId = accountId, tagId = finishedTag.id, isFinished = true, updatedAt = instant())

            selectableTagIdList(accountId = accountId, webId = web.id) shouldBe
                listOf(firstTag.id, finishedTag.id, thirdTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DOMAIN-007 검색어는 이모지·제목·설명 중 하나 이상을 포함하면 만족한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val titleTag = tag(title = "여행 기록")
            val englishTag = tag(title = "Travel")
            val descriptionTag = tag(title = "zzz").withDetail(emoji = "", description = "가족 여행")
            val emojiTag = tag(title = "yyy").withDetail(emoji = "✈️", description = "")
            insertWeb(accountId, web)
            insertTag(accountId, titleTag, englishTag, descriptionTag, emojiTag)

            selectableTagIdList(accountId = accountId, webId = web.id, query = "여행") shouldContainExactlyInAnyOrder
                listOf(titleTag.id, descriptionTag.id)
            selectableTagIdList(accountId = accountId, webId = web.id, query = "trav") shouldBe listOf(englishTag.id)
            selectableTagIdList(accountId = accountId, webId = web.id, query = "✈️") shouldBe listOf(emojiTag.id)
            selectableTagIdList(accountId = accountId, webId = web.id, query = "업무").shouldBeEmpty()
        }

        test("TC-WEB-TAG-DATA-001 TC-WEB-ADD-DATA-008 TC-WEB-ADD-DATA-010 웹 항목 추가와 태그 연결이 하나의 저장 작업으로 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val selectedTagList = List(2) { tag() }
            val unselectedTag = tag()
            insertTag(accountId, *selectedTagList.toTypedArray(), unselectedTag)

            webTransaction.upsert(
                accountId = accountId,
                webList = listOf(web),
                webTagList =
                    selectedTagList.map { value ->
                        WebTagLocalEntity(
                            webId = web.id,
                            tagId = value.id,
                            isDeleted = false,
                            updatedAt = instant(),
                            createdAt = instant(),
                        )
                    },
            )

            linkedTagIdList(accountId = accountId, webId = web.id) shouldContainExactlyInAnyOrder
                selectedTagList.map { value -> value.id }
            selectedTagList.forEach { value ->
                tagWebIdList(accountId = accountId, tagId = value.id) shouldBe listOf(web.id)
            }
            tagWebIdList(accountId = accountId, tagId = unselectedTag.id).shouldBeEmpty()
        }

        test("TC-WEB-TAG-DATA-002 TC-WEB-ADD-DATA-004 저장이 실패하면 웹 항목, 계정 연결과 태그 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val targetTag = tag()
            insertTag(accountId, targetTag)
            val failingDatabase = spyk(database)
            every { failingDatabase.accountWebTagDao() } throws IllegalStateException("save failed")
            val failingTransaction = AccountWebTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    webList = listOf(web),
                    webTagList =
                        listOf(
                            WebTagLocalEntity(
                                webId = web.id,
                                tagId = targetTag.id,
                                isDeleted = false,
                                updatedAt = instant(),
                                createdAt = instant(),
                            ),
                        ),
                )
            }

            linkedTagIdList(accountId = accountId, webId = web.id).shouldBeEmpty()
            tagWebIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
            webDataSource.find(accountId = accountId, webId = web.id).first().shouldBeNull()
            listOf("web", "account_web", "web_tag", "account_web_tag").forEach { table ->
                database.useReaderConnection { transactor ->
                    transactor.usePrepared("SELECT COUNT(*) FROM $table") { statement ->
                        statement.step()
                        statement.getLong(0)
                    }
                } shouldBe 0L
            }
        }

        test("TC-WEB-TAG-DOMAIN-014 TC-WEB-DETAIL-DATA-021 태그 연결은 웹 목록과 웹 검색 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web(title = "a-web")
            val secondWeb = web(title = "b-web")
            val targetTag = tag(title = TAG_ONLY_TITLE)
            insertWeb(accountId, firstWeb, secondWeb)
            insertTag(accountId, targetTag)
            val beforeWebIdList = webDataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT).pagedWebIdList()

            link(accountId = accountId, webId = firstWeb.id, tagId = targetTag.id)

            webDataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT).pagedWebIdList() shouldBe beforeWebIdList
            searchWebDataSource
                .page(accountId = accountId, query = TAG_ONLY_TITLE, sort = ListSortLocalEntity.DEFAULT)
                .pagedWebIdList()
                .shouldBeEmpty()
        }

        test("TC-WEB-DETAIL-DATA-019 태그 연결의 저장은 웹 항목을 업로드 대기로 만들지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val targetTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, targetTag)
            webSyncTransaction.clearPending(accountId = accountId, webList = listOf(web))

            link(accountId = accountId, webId = web.id, tagId = targetTag.id)

            webSyncDataSource.findPending(accountId = accountId).shouldBeEmpty()
            webTagSyncDataSource
                .findPending(accountId = accountId)
                .map { webTag -> webTag.tagId } shouldBe listOf(targetTag.id)
        }

        test("TC-WEB-DETAIL-DATA-020 태그 연결 결과가 그 태그의 웹 탭에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val targetTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, targetTag)

            link(accountId = accountId, webId = web.id, tagId = targetTag.id)
            tagWebIdList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(web.id)

            unlink(accountId = accountId, webId = web.id, tagId = targetTag.id)

            tagWebIdList(accountId = accountId, tagId = targetTag.id).shouldBeEmpty()
        }

        test("TC-WEB-DETAIL-DOMAIN-035 태그 연결과 해제는 웹 항목의 내용과 수정 시각을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val targetTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, targetTag)

            link(accountId = accountId, webId = web.id, tagId = targetTag.id)
            webDataSource.find(accountId = accountId, webId = web.id).first() shouldBe web

            unlink(accountId = accountId, webId = web.id, tagId = targetTag.id)

            webDataSource.find(accountId = accountId, webId = web.id).first() shouldBe web
        }

        test("TC-WEB-DETAIL-DOMAIN-037 삭제 상태인 웹 항목에서도 태그 연결을 바꿀 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web().copy(isDeleted = true)
            val targetTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, targetTag)

            link(accountId = accountId, webId = web.id, tagId = targetTag.id)

            linkedTagIdList(accountId = accountId, webId = web.id) shouldBe listOf(targetTag.id)
            webDataSource
                .find(accountId = accountId, webId = web.id)
                .first()
                ?.isDeleted shouldBe true
        }

        test("TC-WEB-TAG-DATA-004 TC-DATA-SYNC-DOMAIN-001 연결의 업로드 대기 여부가 계정별로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val targetTag = tag()
            insertWeb(accountId, web)
            insertTag(accountId, targetTag)

            link(accountId = accountId, webId = web.id, tagId = targetTag.id)

            webTagSyncDataSource
                .findPending(accountId = accountId)
                .map { webTag -> webTag.tagId } shouldBe listOf(targetTag.id)
            webTagSyncDataSource.findPending(accountId = otherAccountId).shouldBeEmpty()

            AccountWebTagSyncTransactionImpl(database = database).clearPending(
                accountId = accountId,
                webTagList = webTagSyncDataSource.findPending(accountId = accountId),
            )
            unlink(accountId = accountId, webId = web.id, tagId = targetTag.id)

            webTagSyncDataSource
                .findPending(accountId = accountId)
                .map { webTag -> webTag.tagId to webTag.isDeleted } shouldBe listOf(targetTag.id to true)
            webTagSyncDataSource.findPending(accountId = otherAccountId).shouldBeEmpty()
        }

        test("TC-ENTITY-TAG-INPUT-DATA-002 TC-WEB-DETAIL-DATA-017 연결된 태그는 페이지로 나누지 않고 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val finishedTag = tag(title = "z-finished").copy(isFinished = true)
            val activeTag = tag(title = "a-active")
            insertWeb(accountId, web)
            insertTag(accountId, finishedTag, activeTag)
            val linkedTagList = List(SELECTABLE_PAGE_SIZE + 1) { index -> tag(title = "linked-$index") }
            insertTag(accountId, *linkedTagList.toTypedArray())
            (linkedTagList + finishedTag).forEach { value ->
                link(accountId = accountId, webId = web.id, tagId = value.id)
            }

            linkedTagIdList(accountId = accountId, webId = web.id).size shouldBe linkedTagList.size + 1
            selectableTagIdList(accountId = accountId, webId = web.id) shouldContainExactlyInAnyOrder
                (linkedTagList + finishedTag + activeTag).map { value -> value.id }
        }

        test("TC-ENTITY-TAG-INPUT-DATA-004 검색어를 만족하는 태그는 목록의 첫 페이지에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val matchedTag = tag(title = "z-matched-$SEARCH_ONLY_QUERY")
            insertWeb(accountId, web)
            insertTag(accountId, *List(SELECTABLE_PAGE_SIZE) { index -> tag(title = "a-other-$index") }.toTypedArray())
            insertTag(accountId, matchedTag)

            dataSource
                .pageSelectableTag(accountId = accountId, webId = web.id, query = SEARCH_ONLY_QUERY)
                .pagedTagIdList(loadSize = SELECTABLE_PAGE_SIZE) shouldBe listOf(matchedTag.id)
        }

        test("TC-ENTITY-TAG-INPUT-DATA-001 태그 선택 목록을 페이지 단위로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val tagList = List(3) { index -> tag(title = "tag-$index") }
            insertWeb(accountId, web)
            insertTag(accountId, *tagList.toTypedArray())

            dataSource
                .pageSelectableTag(accountId = accountId, webId = web.id, query = "")
                .pagedTagIdList(loadSize = 2) shouldBe tagList.take(2).map { tag -> tag.id }
        }
    }) {
    companion object {
        private const val TAG_ONLY_TITLE: String = "WebTagOnlyTitle"
        private const val SEARCH_ONLY_QUERY: String = "WebTagSearchOnly"
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

        private suspend fun PagingSource<Int, WebLocalEntity>.pagedWebIdList(loadSize: Int = 100): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, WebLocalEntity>>().data.map { web -> web.id }
        }

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

        private fun web(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()
                .let { web ->
                    web.copy(
                        detail = web.detail.copy(title = title),
                        isDeleted = false,
                    )
                }
    }
}
