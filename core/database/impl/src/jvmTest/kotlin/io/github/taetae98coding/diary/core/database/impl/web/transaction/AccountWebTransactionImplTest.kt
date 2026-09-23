package io.github.taetae98coding.diary.core.database.impl.web.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountWebTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountWebTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountWebTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun findWebList(): List<WebLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT id, title, description, url, header_list, is_deleted, updated_at, created_at
                    FROM web
                    ORDER BY id ASC
                    """,
                ) { statement -> statement.readAll { it.toWeb() } }
            }

        suspend fun findAccountWebList(): List<AccountWebLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT account_id, web_id, is_dirty
                    FROM account_web
                    ORDER BY web_id ASC
                    """,
                ) { statement -> statement.readAll { it.toAccountWeb() } }
            }

        test("TC-WEB-ADD-DATA-001 웹 항목과 현재 계정의 연결을 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList() shouldBe listOf(web)
            findAccountWebList() shouldBe
                listOf(
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-WEB-ADD-DOMAIN-009 TC-WEB-ADD-DOMAIN-010 제목, 설명, URL과 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val web =
                web().copy(
                    detail =
                        WebDetailLocalEntity(
                            title = "title-${fixtureMonkey.giveMeOne<String>()}",
                            description = "description-${fixtureMonkey.giveMeOne<String>()}",
                            url = "https://example.com/${fixtureMonkey.giveMeOne<String>()}",
                            headerList = emptyList(),
                        ),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList() shouldBe listOf(web)
        }

        test("TC-WEB-ADD-DATA-003 요청 헤더는 저장한 순서 그대로 함께 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val headerList =
                listOf(
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeaderLocalEntity(name = "X-Region", value = ""),
                )
            val web = web().let { web -> web.copy(detail = web.detail.copy(headerList = headerList)) }

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList().single().detail.headerList shouldBe headerList
        }

        test("TC-WEB-ADD-DOMAIN-003 헤더가 없는 웹 항목은 빈 헤더 목록으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web().let { web -> web.copy(detail = web.detail.copy(description = "", headerList = emptyList())) }

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList().single().detail.headerList shouldBe emptyList()
        }

        test("TC-WEB-ADD-DATA-002 같은 식별자의 웹 항목을 다시 저장하면 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val changedWeb = web.copy(detail = detail())

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            transaction.upsert(accountId = accountId, webList = listOf(changedWeb), webTagList = emptyList())

            findWebList() shouldBe listOf(changedWeb)
            findAccountWebList() shouldBe
                listOf(
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-WEB-DETAIL-DATA-014 수정은 제목, 설명, URL, 요청 헤더와 수정 시각을 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web().copy(isDeleted = false)
            val changedDetail =
                WebDetailLocalEntity(
                    title = "title-${fixtureMonkey.giveMeOne<String>()}",
                    description = "description-${fixtureMonkey.giveMeOne<String>()}",
                    url = "https://changed.example.com/${fixtureMonkey.giveMeOne<String>()}",
                    headerList = listOf(WebHeaderLocalEntity(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}")),
                )
            val updatedAt = instant()
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            val updatedCount =
                transaction.updateDetail(
                    accountId = accountId,
                    webId = web.id,
                    detail = changedDetail,
                    updatedAt = updatedAt,
                )

            updatedCount shouldBe 1
            findWebList() shouldBe listOf(web.copy(detail = changedDetail, updatedAt = updatedAt))
            findAccountWebList() shouldBe
                listOf(
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-WEB-DETAIL-DOMAIN-027 삭제 상태인 웹 항목을 수정해도 삭제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web().copy(isDeleted = true)
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            val changedDetail = detail()
            transaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = changedDetail,
                updatedAt = instant(),
            ) shouldBe 1

            findWebList().single().detail shouldBe changedDetail
            findWebList().single().isDeleted shouldBe true
        }

        test("TC-WEB-DETAIL-DATA-015 헤더를 모두 지운 수정을 반영하면 요청 헤더가 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val headerList =
                listOf(
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${fixtureMonkey.giveMeOne<String>()}"),
                    WebHeaderLocalEntity(name = "Accept", value = "text/html"),
                )
            val web = web().let { web -> web.copy(detail = web.detail.copy(headerList = headerList)) }
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            transaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = web.detail.copy(headerList = emptyList()),
                updatedAt = instant(),
            ) shouldBe 1

            findWebList().single().detail.headerList shouldBe emptyList()
        }

        test("TC-WEB-DETAIL-DATA-016 계정과 식별자를 함께 만족하는 웹 항목에만 수정을 반영한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            val otherAccountWeb = web()
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            transaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            val changedDetail = detail()

            transaction.updateDetail(
                accountId = accountId,
                webId = otherAccountWeb.id,
                detail = changedDetail,
                updatedAt = instant(),
            ) shouldBe 0
            transaction.updateDetail(
                accountId = accountId,
                webId = fixtureMonkey.giveMeOne<Uuid>(),
                detail = changedDetail,
                updatedAt = instant(),
            ) shouldBe 0

            findWebList().map { stored -> stored.detail } shouldBe
                listOf(web, otherAccountWeb).sortedBy { stored -> stored.id.toString() }.map { stored -> stored.detail }

            transaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = changedDetail,
                updatedAt = instant(),
            ) shouldBe 1

            findWebList().single { stored -> stored.id == web.id }.detail shouldBe changedDetail
            findWebList().single { stored -> stored.id == otherAccountWeb.id }.detail shouldBe otherAccountWeb.detail
        }

        test("TC-WEB-DETAIL-DATA-008 삭제는 삭제 여부와 수정 시각만 바꾸고 웹 항목과 계정 연결을 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web().copy(isDeleted = false)
            val deletedAt = instant()
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            val updatedCount =
                transaction.updateDeleted(
                    accountId = accountId,
                    webId = web.id,
                    isDeleted = true,
                    updatedAt = deletedAt,
                )

            updatedCount shouldBe 1
            findWebList() shouldBe listOf(web.copy(isDeleted = true, updatedAt = deletedAt))
            findAccountWebList() shouldBe
                listOf(
                    AccountWebLocalEntity(
                        accountId = accountId,
                        webId = web.id,
                        isDirty = true,
                    ),
                )
        }

        test("TC-WEB-DETAIL-DATA-009 계정과 식별자를 함께 만족하는 웹 항목에만 삭제를 반영한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web().copy(isDeleted = false)
            val otherAccountWeb = web().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            transaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            transaction.updateDeleted(
                accountId = accountId,
                webId = otherAccountWeb.id,
                isDeleted = true,
                updatedAt = instant(),
            ) shouldBe 0
            transaction.updateDeleted(
                accountId = accountId,
                webId = fixtureMonkey.giveMeOne<Uuid>(),
                isDeleted = true,
                updatedAt = instant(),
            ) shouldBe 0

            findWebList().none { stored -> stored.isDeleted } shouldBe true

            transaction.updateDeleted(
                accountId = accountId,
                webId = web.id,
                isDeleted = true,
                updatedAt = instant(),
            ) shouldBe 1

            findWebList().filter { stored -> stored.isDeleted }.map { stored -> stored.id } shouldBe listOf(web.id)
        }

        test("TC-WEB-ADD-DOMAIN-008 서로 다른 식별자의 웹 항목은 각각 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val webList = List(3) { web() }.sortedBy { web -> web.id.toString() }

            webList.forEach { web ->
                transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            }

            findWebList() shouldBe webList
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::detail, detail())
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun detail(): WebDetailLocalEntity = fixtureMonkey.giveMeOne<WebDetailLocalEntity>()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toWeb(): WebLocalEntity =
            WebLocalEntity(
                id = Uuid.parse(getText(0)),
                detail =
                    WebDetailLocalEntity(
                        title = getText(1),
                        description = getText(2),
                        url = getText(3),
                        headerList = Json.decodeFromString(getText(4)),
                    ),
                isDeleted = getBoolean(5),
                updatedAt = Instant.fromEpochMilliseconds(getLong(6)),
                createdAt = Instant.fromEpochMilliseconds(getLong(7)),
            )

        private fun SQLiteStatement.toAccountWeb(): AccountWebLocalEntity =
            AccountWebLocalEntity(
                accountId = Uuid.parse(getText(0)),
                webId = Uuid.parse(getText(1)),
                isDirty = getBoolean(2),
            )
    }
}
