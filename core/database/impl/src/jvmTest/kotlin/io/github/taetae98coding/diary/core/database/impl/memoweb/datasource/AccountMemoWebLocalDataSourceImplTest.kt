package io.github.taetae98coding.diary.core.database.impl.memoweb.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memoweb.transaction.AccountMemoWebSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMemoWebLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountMemoWebLocalDataSourceImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var webTransaction: AccountWebTransactionImpl
        lateinit var syncTransaction: AccountMemoWebSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountMemoWebLocalDataSourceImpl(database = database)
            memoTransaction = AccountMemoTransactionImpl(database = database)
            webTransaction = AccountWebTransactionImpl(database = database)
            syncTransaction = AccountMemoWebSyncTransactionImpl(database = database)
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
                memoWebList =
                    webList.map { web ->
                        MemoWebLocalEntity(
                            memoId = memo.id,
                            webId = web.id,
                            isDeleted = false,
                            updatedAt = memo.updatedAt,
                            createdAt = memo.createdAt,
                        )
                    },
            )
        }

        suspend fun loadSelectableWeb(
            accountId: Uuid,
            query: String = "",
        ): List<WebLocalEntity> {
            val page =
                dataSource.pageSelectableWeb(accountId = accountId, query = query).load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = PAGE_SIZE,
                        placeholdersEnabled = false,
                    ),
                )

            return (page as PagingSource.LoadResult.Page).data
        }

        test("TC-MEMO-DETAIL-DOMAIN-010 삭제된 웹 항목은 연결이 남아 있어도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))

            webTransaction.upsert(accountId = accountId, webList = listOf(web.copy(isDeleted = true)), webTagList = emptyList())

            dataSource.getWebList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
            loadSelectableWeb(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-DETAIL-DATA-030 TC-MEMO-WEB-DOMAIN-011 조회한 웹 항목은 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val lastWeb = web(title = LAST_WEB_TITLE)
            val firstWeb = web(title = FIRST_WEB_TITLE)
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(lastWeb, firstWeb))

            dataSource.getWebList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(firstWeb, lastWeb)
        }

        test("TC-MEMO-WEB-DATA-009 가리키는 웹 항목이 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val missingWebId = fixtureMonkey.giveMeOne<Uuid>()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoWebList = emptyList(),
            )

            syncTransaction.save(
                accountId = accountId,
                memoWebList = listOf(memoWeb(memoId = memo.id, webId = missingWebId)),
                cursor = 1L,
            )

            database.memoWebDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getWebList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DATA-009 가리키는 메모가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val missingMemoId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            syncTransaction.save(
                accountId = accountId,
                memoWebList = listOf(memoWeb(memoId = missingMemoId, webId = web.id)),
                cursor = 1L,
            )

            database.memoWebDao().findByMemoIdList(listOf(missingMemoId)).size shouldBe 1
            dataSource.getWebList(accountId = accountId, memoId = missingMemoId).first().shouldBeEmpty()
        }

        test("메모가 내려받아지면 먼저 저장되어 있던 연결의 웹 항목이 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            syncTransaction.save(
                accountId = accountId,
                memoWebList = listOf(memoWeb(memoId = memo.id, webId = web.id)),
                cursor = 1L,
            )
            dataSource.getWebList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoWebList = emptyList(),
            )

            dataSource.getWebList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(web)
        }

        test("다른 계정의 웹 항목은 연결이 있어도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web()))

            dataSource.getWebList(accountId = otherAccountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-001 선택할 수 있는 웹 항목은 계정과 연결된 삭제되지 않은 웹 항목이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectableWeb = web()
            val deletedWeb = web().copy(isDeleted = true)
            val otherAccountWeb = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(selectableWeb, deletedWeb), webTagList = emptyList())
            webTransaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            loadSelectableWeb(accountId = accountId) shouldBe listOf(selectableWeb)
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-002 선택할 수 있는 웹 항목은 제목 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val lastWeb = web(title = LAST_WEB_TITLE)
            val firstWeb = web(title = FIRST_WEB_TITLE)
            webTransaction.upsert(accountId = accountId, webList = listOf(lastWeb, firstWeb), webTagList = emptyList())

            loadSelectableWeb(accountId = accountId) shouldBe listOf(firstWeb, lastWeb)
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-004 선택했더라도 삭제된 웹 항목은 선택 목록에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptWeb = web()
            val deletedWeb = web()
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(keptWeb, deletedWeb))

            webTransaction.upsert(accountId = accountId, webList = listOf(deletedWeb.copy(isDeleted = true)), webTagList = emptyList())

            loadSelectableWeb(accountId = accountId) shouldBe listOf(keptWeb)
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-015 TC-MEMO-WEB-INPUT-DATA-003 검색어는 제목과 설명으로 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val titleMatchedWeb = web(title = "$SEARCH_QUERY-title")
            val descriptionMatchedWeb =
                web(title = FIRST_WEB_TITLE).let { web ->
                    web.copy(detail = web.detail.copy(description = SEARCH_QUERY))
                }
            val urlMatchedWeb =
                web(title = LAST_WEB_TITLE).let { web ->
                    web.copy(detail = web.detail.copy(url = "https://$SEARCH_QUERY.example.com"))
                }
            val headerMatchedWeb =
                web(title = HEADER_WEB_TITLE).let { web ->
                    web.copy(detail = web.detail.copy(headerList = listOf(WebHeaderLocalEntity(name = SEARCH_QUERY, value = SEARCH_QUERY))))
                }
            webTransaction.upsert(
                accountId = accountId,
                webList = listOf(titleMatchedWeb, descriptionMatchedWeb, urlMatchedWeb, headerMatchedWeb),
                webTagList = emptyList(),
            )

            loadSelectableWeb(accountId = accountId, query = SEARCH_QUERY) shouldBe listOf(descriptionMatchedWeb, titleMatchedWeb)
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-009 빈 검색어는 선택 목록을 좁히지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web(title = FIRST_WEB_TITLE)
            val lastWeb = web(title = LAST_WEB_TITLE)
            webTransaction.upsert(accountId = accountId, webList = listOf(firstWeb, lastWeb), webTagList = emptyList())

            loadSelectableWeb(accountId = accountId, query = "") shouldBe listOf(firstWeb, lastWeb)
        }

        test("TC-MEMO-WEB-INPUT-DOMAIN-010 검색어는 선택한 웹 항목에도 같은 기준으로 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val matchedWeb = web(title = "$SEARCH_QUERY-title")
            val selectedWeb = web(title = FIRST_WEB_TITLE)
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(matchedWeb, selectedWeb))

            loadSelectableWeb(accountId = accountId, query = SEARCH_QUERY) shouldBe listOf(matchedWeb)
            dataSource.getWebList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(selectedWeb, matchedWeb)
        }

        test("TC-MEMO-WEB-INPUT-DATA-001 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val webList =
                List(WEB_COUNT) { index ->
                    web(title = "Web-${index.toString().padStart(length = 3, padChar = '0')}")
                }.reversed()
            val expected = webList.sortedBy { web -> web.detail.title }
            webTransaction.upsert(accountId = accountId, webList = webList, webTagList = emptyList())

            val firstPage = dataSource.pageSelectableWeb(accountId = accountId, query = "").loadPage(loadSize = SMALL_PAGE_SIZE)

            firstPage.data shouldBe expected.take(SMALL_PAGE_SIZE)
            firstPage.nextKey shouldBe SMALL_PAGE_SIZE

            val secondPage =
                dataSource
                    .pageSelectableWeb(accountId = accountId, query = "")
                    .loadPage(key = firstPage.nextKey, loadSize = SMALL_PAGE_SIZE)

            secondPage.data shouldBe expected.drop(SMALL_PAGE_SIZE).take(SMALL_PAGE_SIZE)
        }

        test("TC-MEMO-WEB-INPUT-DATA-005 웹 항목이 추가되면 선택 목록 조회 결과에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web(title = FIRST_WEB_TITLE)
            webTransaction.upsert(accountId = accountId, webList = listOf(firstWeb), webTagList = emptyList())
            loadSelectableWeb(accountId = accountId) shouldBe listOf(firstWeb)

            val addedWeb = web(title = LAST_WEB_TITLE)
            webTransaction.upsert(accountId = accountId, webList = listOf(addedWeb), webTagList = emptyList())

            loadSelectableWeb(accountId = accountId) shouldBe listOf(firstWeb, addedWeb)
        }

        test("TC-MEMO-DETAIL-DATA-031 TC-MEMO-WEB-INPUT-DATA-005 저장된 웹 항목의 제목이 바뀌면 연결된 웹 조회와 선택 목록에 함께 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val web = web(title = FIRST_WEB_TITLE)
            insertMemoWithWebList(accountId = accountId, memo = memo, webList = listOf(web))
            val renamedWeb = web.copy(detail = web.detail.copy(title = LAST_WEB_TITLE))

            webTransaction.upsert(accountId = accountId, webList = listOf(renamedWeb), webTagList = emptyList())

            dataSource.getWebList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(renamedWeb)
            loadSelectableWeb(accountId = accountId) shouldBe listOf(renamedWeb)
        }

        test("검색어의 대소문자는 판정에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web(title = "AndroidDeveloper")
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            loadSelectableWeb(accountId = accountId, query = "androidDEVELOPER") shouldBe listOf(web)
        }
    }) {
    public companion object {
        private const val FIRST_WEB_TITLE = "AppleWeb"
        private const val LAST_WEB_TITLE = "ZebraWeb"
        private const val HEADER_WEB_TITLE = "HeaderWeb"
        private const val SEARCH_QUERY = "searchable"
        private const val PAGE_SIZE = 20
        private const val SMALL_PAGE_SIZE = 10
        private const val WEB_COUNT = 25

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun web(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::detail, webDetail(title = title))
                .setExp(WebLocalEntity::isDeleted, false)
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun webDetail(title: String): WebDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetailLocalEntity>()
                .setExp(WebDetailLocalEntity::title, title)
                .setExp(WebDetailLocalEntity::headerList, emptyList<Nothing>())
                .sample()

        private fun memoWeb(
            memoId: Uuid,
            webId: Uuid,
        ): MemoWebLocalEntity =
            MemoWebLocalEntity(
                memoId = memoId,
                webId = webId,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private suspend fun PagingSource<Int, WebLocalEntity>.loadPage(
            key: Int? = null,
            loadSize: Int = PAGE_SIZE,
        ): PagingSource.LoadResult.Page<Int, WebLocalEntity> {
            val params: PagingSource.LoadParams<Int> =
                if (key == null) {
                    PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
                } else {
                    PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
                }

            return load(params).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, WebLocalEntity>>()
        }
    }
}
