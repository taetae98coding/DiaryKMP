package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountWebLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountWebLocalDataSourceImpl
        lateinit var webTransaction: AccountWebTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountWebLocalDataSourceImpl(database = database)
            webTransaction = AccountWebTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun PagingSource<Int, WebLocalEntity>.pagedWebs(loadSize: Int = 100): List<WebLocalEntity> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, WebLocalEntity>>().data
        }

        suspend fun pagedWebs(accountId: Uuid): List<WebLocalEntity> = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT).pagedWebs()

        test("TC-WEB-HOME-DOMAIN-001 TC-WEB-HOME-DOMAIN-003 삭제되지 않은 계정의 웹 항목만 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web(title = FIRST_WEB_TITLE)
            val lastWeb = web(title = LAST_WEB_TITLE)
            val deletedWeb = web().copy(isDeleted = true)
            val otherAccountWeb = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(lastWeb, firstWeb, deletedWeb), webTagList = emptyList())
            webTransaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            pagedWebs(accountId) shouldBe listOf(firstWeb, lastWeb)
        }

        test("TC-WEB-HOME-DOMAIN-002 저장된 웹 항목이 없는 계정은 빈 목록으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            webTransaction.upsert(accountId = otherAccountId, webList = listOf(web()), webTagList = emptyList())

            pagedWebs(accountId).shouldBeEmpty()
        }

        test("TC-WEB-HOME-DATA-001 요청한 크기만큼 페이지로 나누어 이어서 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val webList = List(3) { index -> web(title = "$index-${fixtureMonkey.giveMeOne<String>()}") }
            webTransaction.upsert(accountId = accountId, webList = webList, webTagList = emptyList())

            val pagingSource = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT)
            val firstPage =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            val nextPage =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = checkNotNull(firstPage.nextKey), loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page

            firstPage.data shouldBe webList.take(2)
            nextPage.data shouldBe webList.drop(2)
        }

        test("TC-WEB-HOME-DATA-002 TC-WEB-HOME-DOMAIN-004 저장된 웹 항목의 변화가 페이지 조회에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val renamedWeb = web.copy(detail = web.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))

            suspend fun assertPageInvalidated(
                expected: List<WebLocalEntity>,
                change: suspend () -> Unit,
            ) {
                val pagingSource = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT)
                pagingSource.pagedWebs()
                val invalidated = CompletableDeferred<Unit>()
                pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

                change()

                withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
                pagingSource.invalid.shouldBeTrue()
                pagedWebs(accountId) shouldBe expected
            }

            pagedWebs(accountId).shouldBeEmpty()
            assertPageInvalidated(expected = listOf(web)) {
                webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            }
            assertPageInvalidated(expected = listOf(renamedWeb)) {
                webTransaction.upsert(accountId = accountId, webList = listOf(renamedWeb), webTagList = emptyList())
            }
            assertPageInvalidated(expected = emptyList()) {
                webTransaction.upsert(accountId = accountId, webList = listOf(renamedWeb.copy(isDeleted = true)), webTagList = emptyList())
            }
        }

        test("TC-WEB-ADD-DATA-003 페이지 조회에도 요청 헤더가 저장한 순서 그대로 함께 전달된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            pagedWebs(accountId).single().detail.headerList shouldBe web.detail.headerList
        }

        test("TC-WEB-DETAIL-DATA-001 계정과 식별자로 웹 항목을 요청 헤더까지 함께 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            webTransaction.upsert(accountId = accountId, webList = listOf(web, web()), webTagList = emptyList())

            val found = dataSource.find(accountId = accountId, webId = web.id).first()

            found shouldBe web
            found?.detail?.headerList shouldBe web.detail.headerList
        }

        test("TC-WEB-DETAIL-DOMAIN-001 상세 대상이 될 수 없는 웹 항목은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountWeb = web()
            webTransaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            dataSource.find(accountId = accountId, webId = otherAccountWeb.id).first().shouldBeNull()
            dataSource.find(accountId = accountId, webId = fixtureMonkey.giveMeOne<Uuid>()).first().shouldBeNull()
        }

        test("TC-WEB-DETAIL-DOMAIN-019 삭제 여부와 관계없이 요청 헤더까지 함께 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val deletedWeb = web().copy(isDeleted = true)
            webTransaction.upsert(accountId = accountId, webList = listOf(web, deletedWeb), webTagList = emptyList())

            dataSource.find(accountId = accountId, webId = web.id).first() shouldBe web

            val foundDeleted = dataSource.find(accountId = accountId, webId = deletedWeb.id).first()
            foundDeleted shouldBe deletedWeb
            foundDeleted?.detail?.headerList shouldBe deletedWeb.detail.headerList
        }

        test("TC-WEB-DETAIL-DATA-002 저장된 웹 항목이 바뀌면 조회 결과에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val renamedWeb = web.copy(detail = web.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))
            webTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            dataSource.find(accountId = accountId, webId = web.id).test {
                awaitItem() shouldBe web

                webTransaction.upsert(accountId = accountId, webList = listOf(renamedWeb), webTagList = emptyList())

                awaitItem() shouldBe renamedWeb
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-WEB-HOME-DOMAIN-005 최근 수정순은 수정 시각 내림차순으로 조회하고 같으면 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val latestWeb = web(title = LAST_WEB_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            val sameUpdatedFirstWeb = web(title = FIRST_WEB_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val sameUpdatedLastWeb = web(title = MIDDLE_WEB_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            webTransaction.upsert(
                accountId = accountId,
                webList = listOf(sameUpdatedLastWeb, latestWeb, sameUpdatedFirstWeb),
                webTagList = emptyList(),
            )

            dataSource
                .page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED)
                .pagedWebs() shouldBe listOf(latestWeb, sameUpdatedFirstWeb, sameUpdatedLastWeb)
        }

        test("TC-WEB-HOME-DOMAIN-006 정렬을 바꿔도 노출하는 웹 항목은 달라지지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web(title = FIRST_WEB_TITLE)
            val lastWeb = web(title = LAST_WEB_TITLE)
            val deletedWeb = web().copy(isDeleted = true)
            webTransaction.upsert(accountId = accountId, webList = listOf(firstWeb, lastWeb, deletedWeb), webTagList = emptyList())

            val titleSorted = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.TITLE).pagedWebs()
            val recentlyUpdatedSorted = dataSource.page(accountId = accountId, sort = ListSortLocalEntity.RECENTLY_UPDATED).pagedWebs()

            titleSorted shouldBe listOf(firstWeb, lastWeb)
            recentlyUpdatedSorted.toSet() shouldBe titleSorted.toSet()
        }

        test("정렬을 고르지 않은 기본 순서는 제목순과 같다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstWeb = web(title = FIRST_WEB_TITLE)
            val lastWeb = web(title = LAST_WEB_TITLE)
            webTransaction.upsert(accountId = accountId, webList = listOf(lastWeb, firstWeb), webTagList = emptyList())

            dataSource.page(accountId = accountId, sort = ListSortLocalEntity.DEFAULT).pagedWebs() shouldBe
                dataSource.page(accountId = accountId, sort = ListSortLocalEntity.TITLE).pagedWebs()
        }
    }) {
    public companion object {
        private const val FIRST_WEB_TITLE = "AppleWeb"
        private const val MIDDLE_WEB_TITLE = "MangoWeb"
        private const val LAST_WEB_TITLE = "ZebraWeb"
        private const val INVALIDATION_TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::detail, detail(title = title))
                .setExp(WebLocalEntity::isDeleted, false)
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun detail(title: String): WebDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetailLocalEntity>()
                .setExp(WebDetailLocalEntity::title, title)
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
