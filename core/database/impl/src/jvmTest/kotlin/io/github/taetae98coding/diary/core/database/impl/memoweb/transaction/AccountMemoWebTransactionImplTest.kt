package io.github.taetae98coding.diary.core.database.impl.memoweb.transaction

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoweb.datasource.AccountMemoWebLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memoweb.datasource.AccountMemoWebSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoWebTestException : RuntimeException()

class AccountMemoWebTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var webTransaction: AccountWebTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var accountMemoWebTransaction: AccountMemoWebTransactionImpl
        lateinit var syncTransaction: AccountMemoWebSyncTransactionImpl
        lateinit var dataSource: AccountMemoWebLocalDataSourceImpl
        lateinit var syncDataSource: AccountMemoWebSyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            memoTransaction = AccountMemoTransactionImpl(database = database)
            webTransaction = AccountWebTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            accountMemoWebTransaction = AccountMemoWebTransactionImpl(database = database)
            syncTransaction = AccountMemoWebSyncTransactionImpl(database = database)
            dataSource = AccountMemoWebLocalDataSourceImpl(database = database)
            syncDataSource = AccountMemoWebSyncLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemoWithWebList(
            accountId: Uuid,
            memo: MemoLocalEntity,
            webList: List<WebLocalEntity>,
        ) {
            webTransaction.upsert(accountId = accountId, webList = webList, webTagList = emptyList())
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoWebList = webList.map { web -> memoWeb(memoId = memo.id, webId = web.id, memo = memo) },
            )
        }

        suspend fun findMemo(
            accountId: Uuid,
            memoId: Uuid,
        ): MemoLocalEntity? = database.accountMemoDao().find(accountId = accountId, memoId = memoId).first()

        suspend fun findMemoWebList(memoId: Uuid): List<MemoWebLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT memo_id, web_id, is_deleted, updated_at, created_at
                    FROM memo_web
                    WHERE memo_id = '$memoId'
                    ORDER BY web_id ASC
                    """,
                ) { statement -> statement.readAll { it.toMemoWeb() } }
            }

        suspend fun getWebList(
            accountId: Uuid,
            memoId: Uuid,
        ): List<WebLocalEntity> = dataSource.getWebList(accountId = accountId, memoId = memoId).first()

        suspend fun findPendingWebIdList(accountId: Uuid): List<Uuid> =
            syncDataSource
                .findPending(accountId = accountId)
                .map { memoWeb -> memoWeb.webId }

        suspend fun clearAllPending(accountId: Uuid) {
            syncTransaction.clearPending(
                accountId = accountId,
                memoWebList = syncDataSource.findPending(accountId = accountId),
            )
        }

        test("TC-MEMO-WEB-DOMAIN-001 하나의 메모에 여러 웹 항목을 연결해 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val webList = List(2) { web() }

            insertMemoWithWebList(accountId = accountId, memo = memo, webList = webList)

            getWebList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder webList
        }

        test("TC-MEMO-WEB-DOMAIN-002 같은 웹 항목을 여러 메모에 연결해도 서로 대체되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val firstMemo = memo()
            val secondMemo = memo()

            listOf(firstMemo, secondMemo).forEach { memo ->
                insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))
            }

            getWebList(accountId = accountId, memoId = firstMemo.id) shouldBe listOf(web)
            getWebList(accountId = accountId, memoId = secondMemo.id) shouldBe listOf(web)
        }

        test("TC-MEMO-WEB-DOMAIN-003 같은 메모와 웹 항목의 연결을 다시 저장해도 한 건으로 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()

            repeat(2) {
                insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))
            }

            findMemoWebList(memoId = memo.id) shouldHaveSize 1
        }

        test("TC-MEMO-WEB-DOMAIN-004 연결할 웹 항목이 없으면 웹 연결 없이 메모만 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            findMemoWebList(memoId = memo.id).shouldBeEmpty()
            getWebList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DOMAIN-005 웹 항목을 삭제해도 메모와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            webTransaction.upsert(accountId = accountId, webList = listOf(web.copy(isDeleted = true)), webTagList = emptyList())

            findMemoWebList(memoId = memo.id) shouldBe
                listOf(memoWeb(memoId = memo.id, webId = web.id, memo = memo))
        }

        test("TC-MEMO-WEB-DOMAIN-006 메모를 완료하거나 삭제해도 웹 항목과의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(isFinished = false, isDeleted = false)
            val web = web()
            val memoWeb = memoWeb(memoId = memo.id, webId = web.id, memo = memo)
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            memoTransaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = true, updatedAt = instant())
            findMemoWebList(memoId = memo.id) shouldBe listOf(memoWeb)

            memoTransaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = true, updatedAt = instant())
            findMemoWebList(memoId = memo.id) shouldBe listOf(memoWeb)
        }

        test("TC-MEMO-WEB-DOMAIN-007 메모의 제목·설명·컬러·기간 수정은 웹 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()
            val updatedAt = instant()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            memoTransaction.updateDetail(accountId = accountId, memoId = memo.id, detail = newDetail, updatedAt = updatedAt)

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo.copy(detail = newDetail, updatedAt = updatedAt)
            findMemoWebList(memoId = memo.id) shouldBe
                listOf(memoWeb(memoId = memo.id, webId = web.id, memo = memo))
            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(web)
        }

        test("TC-MEMO-WEB-DOMAIN-007 웹 항목의 제목·설명·URL·요청 헤더 수정은 웹 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            val changedWeb = web.copy(detail = webDetail(), updatedAt = instant())
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            webTransaction.upsert(accountId = accountId, webList = listOf(changedWeb), webTagList = emptyList())

            findMemoWebList(memoId = memo.id) shouldBe
                listOf(memoWeb(memoId = memo.id, webId = web.id, memo = memo))
            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(changedWeb)
        }

        test("TC-MEMO-WEB-DOMAIN-008 TC-MEMO-DETAIL-DATA-025 연결을 해제하면 조회에서 제외되고 해제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))
            val removedAt = instant()

            accountMemoWebTransaction.upsert(accountId = accountId, memoId = memo.id, webId = web.id, isDeleted = true, updatedAt = removedAt)

            getWebList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
            findMemoWebList(memoId = memo.id) shouldBe
                listOf(
                    MemoWebLocalEntity(
                        memoId = memo.id,
                        webId = web.id,
                        isDeleted = true,
                        updatedAt = removedAt,
                        createdAt = memo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-WEB-DOMAIN-009 해제된 연결을 다시 만들면 생성 시각을 유지한 채 되살아난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            val createdAt = instant()
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            database.memoWebDao().upsert(
                MemoWebLocalEntity(
                    memoId = memo.id,
                    webId = web.id,
                    isDeleted = true,
                    updatedAt = instant(),
                    createdAt = createdAt,
                ),
            )
            val restoredAt = instant()

            accountMemoWebTransaction.upsert(accountId = accountId, memoId = memo.id, webId = web.id, isDeleted = false, updatedAt = restoredAt)

            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(web)
            findMemoWebList(memoId = memo.id) shouldBe
                listOf(
                    MemoWebLocalEntity(
                        memoId = memo.id,
                        webId = web.id,
                        isDeleted = false,
                        updatedAt = restoredAt,
                        createdAt = createdAt,
                    ),
                )
        }

        test("TC-MEMO-WEB-DOMAIN-010 삭제된 웹 항목은 메모의 연결된 웹 항목 조회에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptWeb = web()
            val deletedWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(keptWeb, deletedWeb))

            webTransaction.upsert(accountId = accountId, webList = listOf(deletedWeb.copy(isDeleted = true)), webTagList = emptyList())

            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptWeb)
        }

        test("TC-MEMO-WEB-DOMAIN-011 연결된 웹 항목은 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstWeb = web().withTitle(title = "AAA")
            val secondWeb = web().withTitle(title = "BBB")
            val thirdWeb = web().withTitle(title = "CCC")
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(thirdWeb, firstWeb, secondWeb))

            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(firstWeb, secondWeb, thirdWeb)
        }

        test("TC-MEMO-WEB-DOMAIN-012 웹 연결은 메모 목록의 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val linkedMemo = memo().visible().withTitle(title = linkedTitle)
                val unlinkedMemo = memo().visible().withTitle(title = unlinkedTitle)
                val web = web()
                insertMemoWithWebList(accountId = accountId, memo = linkedMemo, webList = listOf(web))
                memoTransaction.upsert(accountId = accountId, memoList = listOf(unlinkedMemo), memoTagList = emptyList())

                val memoList =
                    database
                        .accountMemoDao()
                        .page(accountId = accountId, sort = "title")
                        .loadAll()

                memoList.map { memo -> memo.id } shouldBe listOf(linkedMemo, unlinkedMemo).sortedBy { memo -> memo.detail.title }.map { memo -> memo.id }
            }
        }

        test("TC-MEMO-WEB-DOMAIN-013 메모 연결은 웹 목록의 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val linkedWeb = web().withTitle(title = linkedTitle)
                val unlinkedWeb = web().withTitle(title = unlinkedTitle)
                insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(linkedWeb))
                webTransaction.upsert(accountId = accountId, webList = listOf(unlinkedWeb), webTagList = emptyList())

                val webList =
                    database
                        .accountWebDao()
                        .page(accountId = accountId, sort = "title")
                        .loadAll()

                webList.map { web -> web.id } shouldBe listOf(linkedWeb, unlinkedWeb).sortedBy { web -> web.detail.title }.map { web -> web.id }
            }
        }

        test("TC-MEMO-WEB-DOMAIN-014 웹 연결은 태그로 메모를 조회한 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().visible().copy(primaryTagId = null)
            val web = web()
            val tag = tag()
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))
            webTransaction.upsert(
                accountId = accountId,
                webList = listOf(web),
                webTagList = listOf(webTag(webId = web.id, tagId = tag.id)),
            )

            val memoList =
                database
                    .accountTagMemoDao()
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF.queryValue, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DOMAIN-016 메모 연결은 태그로 웹 항목을 조회한 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().visible().copy(primaryTagId = null)
            val web = web()
            val tag = tag()
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = listOf(memoTag(memoId = memo.id, tagId = tag.id, memo = memo)),
                memoWebList = listOf(memoWeb(memoId = memo.id, webId = web.id, memo = memo)),
            )

            val webList =
                database
                    .accountTagWebDao()
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF.queryValue, sort = "title")
                    .loadAll()

            webList.shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DATA-010 연결을 하나 해제하면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val removedWeb = web()
            val keptWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(removedWeb, keptWeb))
            clearAllPending(accountId = accountId)

            accountMemoWebTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                webId = removedWeb.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            findPendingWebIdList(accountId = accountId) shouldBe listOf(removedWeb.id)
        }

        test("TC-MEMO-WEB-DATA-010 연결을 하나 만들면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptWeb = web()
            val addedWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(keptWeb))
            webTransaction.upsert(accountId = accountId, webList = listOf(addedWeb), webTagList = emptyList())
            clearAllPending(accountId = accountId)

            accountMemoWebTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                webId = addedWeb.id,
                isDeleted = false,
                updatedAt = instant(),
            )

            findPendingWebIdList(accountId = accountId) shouldBe listOf(addedWeb.id)
        }

        test("TC-MEMO-WEB-DATA-001 저장된 연결의 생성 시각과 수정 시각은 저장 시점으로 서로 같고 해제되지 않은 상태다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val memo = memo().copy(updatedAt = now, createdAt = now)
            val web = web()

            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            findMemoWebList(memoId = memo.id) shouldBe
                listOf(
                    MemoWebLocalEntity(
                        memoId = memo.id,
                        webId = web.id,
                        isDeleted = false,
                        updatedAt = now,
                        createdAt = now,
                    ),
                )
        }

        test("TC-MEMO-WEB-DATA-002 저장이 실패하면 메모와 계정 연결, 태그 연결, 웹 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            val memoSyncDataSource = AccountMemoSyncLocalDataSourceImpl(database = database)
            val failingDatabase = spyk(database)
            every { failingDatabase.memoWebDao() } throws MemoWebTestException()
            val failingTransaction = AccountMemoTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoWebTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                    memoWebList = listOf(memoWeb(memoId = memo.id, webId = web.id, memo = memo)),
                )
            }

            findMemo(accountId = accountId, memoId = memo.id).shouldBeNull()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
            findMemoWebList(memoId = memo.id).shouldBeEmpty()
            memoSyncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DATA-003 같은 연결을 다른 수정 시각으로 저장하면 마지막 내용으로 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(updatedAt = instant())
            val web = web()

            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))
            insertMemoWithWebList(accountId = accountId, memo = changedMemo, webList = listOf(web))

            findMemoWebList(memoId = memo.id) shouldBe
                listOf(
                    MemoWebLocalEntity(
                        memoId = memo.id,
                        webId = web.id,
                        isDeleted = false,
                        updatedAt = changedMemo.updatedAt,
                        createdAt = changedMemo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-WEB-DATA-004 해제되지 않은 연결의 웹 항목만 메모의 웹 항목으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptWeb = web()
            val removedWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(keptWeb, removedWeb))

            accountMemoWebTransaction.upsert(accountId = accountId, memoId = memo.id, webId = removedWeb.id, isDeleted = true, updatedAt = instant())

            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptWeb)
        }

        test("TC-MEMO-DETAIL-DATA-024 웹 항목을 선택하면 연결이 선택 시점으로 저장되고 메모의 다른 값은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            val updatedAt = instant()

            accountMemoWebTransaction.upsert(accountId = accountId, memoId = memo.id, webId = web.id, isDeleted = false, updatedAt = updatedAt)

            findMemoWebList(memoId = memo.id) shouldBe
                listOf(
                    MemoWebLocalEntity(
                        memoId = memo.id,
                        webId = web.id,
                        isDeleted = false,
                        updatedAt = updatedAt,
                        createdAt = updatedAt,
                    ),
                )
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(web)
        }

        test("TC-MEMO-DETAIL-DATA-028 복사본에 원본의 해제되지 않은 웹 연결만 복사 시점으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val keptWeb = web()
            val removedWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = source, webList = listOf(keptWeb, removedWeb))
            accountMemoWebTransaction.upsert(accountId = accountId, memoId = source.id, webId = removedWeb.id, isDeleted = true, updatedAt = instant())

            val copiedAt = instant()
            val copy = memo().copy(updatedAt = copiedAt, createdAt = copiedAt)
            val sourceWebIdSet =
                dataSource
                    .findWebIdList(accountId = accountId, memoId = source.id)
                    .toSet()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(copy),
                memoTagList = emptyList(),
                memoWebList =
                    sourceWebIdSet.map { webId ->
                        MemoWebLocalEntity(
                            memoId = copy.id,
                            webId = webId,
                            isDeleted = false,
                            updatedAt = copiedAt,
                            createdAt = copiedAt,
                        )
                    },
            )

            sourceWebIdSet shouldBe setOf(keptWeb.id)
            findMemoWebList(memoId = copy.id) shouldBe
                listOf(
                    MemoWebLocalEntity(
                        memoId = copy.id,
                        webId = keptWeb.id,
                        isDeleted = false,
                        updatedAt = copiedAt,
                        createdAt = copiedAt,
                    ),
                )
            getWebList(accountId = accountId, memoId = source.id) shouldBe listOf(keptWeb)
        }

        test("TC-MEMO-DETAIL-DATA-045 삭제된 웹 항목을 가리키는 원본 연결도 복사본에 만들어진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val deletedWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = source, webList = listOf(deletedWeb))
            webTransaction.upsert(accountId = accountId, webList = listOf(deletedWeb.copy(isDeleted = true)), webTagList = emptyList())

            val copiedAt = fixtureMonkey.giveMeOne<Instant>()
            val copy = memo().copy(updatedAt = copiedAt, createdAt = copiedAt)
            val sourceWebIdSet =
                dataSource
                    .findWebIdList(accountId = accountId, memoId = source.id)
                    .toSet()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(copy),
                memoTagList = emptyList(),
                memoWebList =
                    sourceWebIdSet.map { webId ->
                        MemoWebLocalEntity(
                            memoId = copy.id,
                            webId = webId,
                            isDeleted = false,
                            updatedAt = copiedAt,
                            createdAt = copiedAt,
                        )
                    },
            )

            sourceWebIdSet shouldBe setOf(deletedWeb.id)
            findMemoWebList(memoId = copy.id).map { memoWeb -> memoWeb.webId } shouldBe listOf(deletedWeb.id)
        }

        test("TC-MEMO-WEB-DOMAIN-015 연결된 웹 항목의 제목은 메모 검색에 쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val query = "query-${fixtureMonkey.giveMeOne<Uuid>()}"
            val memo = memo().visible().let { value -> value.copy(detail = value.detail.copy(title = "memo-title", description = "memo-description")) }
            val web = web().withTitle(title = "web-$query")
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            val memoList =
                database
                    .searchMemoDao()
                    .page(accountId = accountId, query = query, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DOMAIN-015 연결된 메모의 제목은 웹 검색에 쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val query = "query-${fixtureMonkey.giveMeOne<Uuid>()}"
            val memo = memo().visible().withTitle(title = "memo-$query")
            val web = web().let { value -> value.copy(detail = value.detail.copy(title = "web-title", description = "web-description", url = "https://example.com")) }
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            val webList =
                database
                    .searchWebDao()
                    .page(accountId = accountId, query = query, sort = "title")
                    .loadAll()

            webList.shouldBeEmpty()
        }

        listOf(
            "완료된" to { memo: MemoLocalEntity -> memo.copy(isFinished = true, isDeleted = false) },
            "삭제된" to { memo: MemoLocalEntity -> memo.copy(isFinished = false, isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-MEMO-DETAIL-DOMAIN-009 $label 메모도 웹 연결을 만들 수 있다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = change(memo())
                val web = web()
                webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
                memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

                accountMemoWebTransaction.upsert(accountId = accountId, memoId = memo.id, webId = web.id, isDeleted = false, updatedAt = instant())

                getWebList(accountId = accountId, memoId = memo.id) shouldBe listOf(web)
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun web(): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::detail, webDetail())
                .setExp(WebLocalEntity::isDeleted, false)
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun webDetail(): WebDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetailLocalEntity>()
                .setExp(WebDetailLocalEntity::headerList, emptyList<Nothing>())
                .sample()

        private fun WebLocalEntity.withTitle(title: String): WebLocalEntity = copy(detail = detail.copy(title = title))

        private fun MemoLocalEntity.withTitle(title: String): MemoLocalEntity = copy(detail = detail.copy(title = title))

        // 목록 조회는 완료되지 않고 삭제되지 않은 메모만 노출하므로, 노출 기준을 검증하는 메모는 두 상태를 고정한다.
        private fun MemoLocalEntity.visible(): MemoLocalEntity = copy(isFinished = false, isDeleted = false)

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun webTag(
            webId: Uuid,
            tagId: Uuid,
        ): WebTagLocalEntity =
            WebTagLocalEntity(
                webId = webId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private suspend fun <T : Any> PagingSource<Int, T>.loadAll(): List<T> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()
                .data

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun memoWeb(
            memoId: Uuid,
            webId: Uuid,
            memo: MemoLocalEntity,
        ): MemoWebLocalEntity =
            MemoWebLocalEntity(
                memoId = memoId,
                webId = webId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
            memo: MemoLocalEntity,
        ): MemoTagLocalEntity =
            MemoTagLocalEntity(
                memoId = memoId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toMemoWeb(): MemoWebLocalEntity =
            MemoWebLocalEntity(
                memoId = Uuid.parse(getText(0)),
                webId = Uuid.parse(getText(1)),
                isDeleted = getBoolean(2),
                updatedAt = Instant.fromEpochMilliseconds(getLong(3)),
                createdAt = Instant.fromEpochMilliseconds(getLong(4)),
            )
    }
}
