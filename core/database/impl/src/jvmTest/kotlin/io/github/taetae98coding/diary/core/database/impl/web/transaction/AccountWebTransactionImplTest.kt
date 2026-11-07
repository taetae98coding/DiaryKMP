package io.github.taetae98coding.diary.core.database.impl.web.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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

        test("TC-fde9eef4-29df-45a0-a37c-b394f94df62f 웹 항목과 현재 계정의 연결을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
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

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 웹 항목 추가·수정·삭제·실행 취소는 업로드 대기 상태가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val web = web().copy(isDeleted = false)
            val pending = listOf(AccountWebLocalEntity(accountId = accountId, webId = web.id, isDirty = true))

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            findAccountWebList() shouldBe pending

            database.accountWebDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = web.id, isDirty = false))
            transaction.updateDetail(accountId = accountId, webId = web.id, detail = Arb.webDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next())
            findAccountWebList() shouldBe pending

            database.accountWebDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = web.id, isDirty = false))
            transaction.updateDeleted(accountId = accountId, webId = web.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            findAccountWebList() shouldBe pending

            database.accountWebDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = web.id, isDirty = false))
            transaction.updateDeleted(accountId = accountId, webId = web.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next())
            findAccountWebList() shouldBe pending
        }

        test("TC-a5fc06fe-a481-403c-9f17-056a028e49b9 TC-4923195f-f274-4a60-aa56-53f9bef6cde9 제목, 설명, URL과 미삭제 상태, 추가 시각을 그대로 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val now = Arb.secondPrecisionInstant().next()
            val web =
                web().copy(
                    detail =
                        WebDetailLocalEntity(
                            title = "title-${Arb.string().next()}",
                            description = "description-${Arb.string().next()}",
                            url = "https://example.com/${Arb.string().next()}",
                            headerList = emptyList(),
                        ),
                    isDeleted = false,
                    updatedAt = now,
                    createdAt = now,
                )

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList() shouldBe listOf(web)
        }

        test("TC-93bd05a6-5f77-4f2b-bd7e-05177feb4ea1 요청 헤더는 저장한 순서 그대로 함께 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val headerList =
                listOf(
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${Arb.string().next()}"),
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${Arb.string().next()}"),
                    WebHeaderLocalEntity(name = "X-Region", value = ""),
                )
            val web = web().let { web -> web.copy(detail = web.detail.copy(headerList = headerList)) }

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList().single().detail.headerList shouldBe headerList
        }

        test("TC-8a31aa2e-8927-4d2d-87ee-584f01b3d108 헤더가 없는 웹 항목은 빈 헤더 목록으로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val web = web().let { web -> web.copy(detail = web.detail.copy(description = "", headerList = emptyList())) }

            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            findWebList().single().detail.headerList shouldBe emptyList()
        }

        test("TC-3e5ef5ac-bc8f-4d12-814b-004499edb02d 웹 항목을 추가해도 이미 저장된 웹 항목은 덮어쓰이지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val savedWeb = web()
            val addedWeb =
                web().copy(
                    detail =
                        WebDetailLocalEntity(
                            title = "added-${savedWeb.detail.title}",
                            description = "added-${savedWeb.detail.description}",
                            url = "https://added.example.com/${Arb.string().next()}",
                            headerList = listOf(WebHeaderLocalEntity(name = "X-Added", value = "value-${Arb.string().next()}")),
                        ),
                )
            transaction.upsert(accountId = accountId, webList = listOf(savedWeb), webTagList = emptyList())

            transaction.upsert(accountId = accountId, webList = listOf(addedWeb), webTagList = emptyList())

            findWebList() shouldBe listOf(savedWeb, addedWeb).sortedBy { web -> web.id.toString() }
        }

        test("TC-7063aa40-a5ab-4f7c-9553-6f438d3affa6 수정은 제목, 설명, URL, 요청 헤더와 수정 시각을 덮어쓴다") {
            val accountId = Arb.kotlinUuid().next()
            val web = web().copy(isDeleted = false)
            val changedDetail =
                WebDetailLocalEntity(
                    title = "title-${Arb.string().next()}",
                    description = "description-${Arb.string().next()}",
                    url = "https://changed.example.com/${Arb.string().next()}",
                    headerList = listOf(WebHeaderLocalEntity(name = "Authorization", value = "token-${Arb.string().next()}")),
                )
            val updatedAt = Arb.secondPrecisionInstant().next()
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

        test("TC-f0fac02c-d9e0-4cb2-a37b-cf58f6f62d04 삭제 상태인 웹 항목을 수정해도 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val web = web().copy(isDeleted = true)
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            val changedDetail = Arb.webDetailLocalEntity().next()
            transaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = changedDetail,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            findWebList().single().detail shouldBe changedDetail
            findWebList().single().isDeleted shouldBe true
        }

        test("TC-fdef8ee2-2dde-49fe-941d-2b563150ec2a 헤더를 모두 지운 수정을 반영하면 요청 헤더가 남지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val headerList =
                listOf(
                    WebHeaderLocalEntity(name = "Authorization", value = "token-${Arb.string().next()}"),
                    WebHeaderLocalEntity(name = "Accept", value = "text/html"),
                )
            val web = web().let { web -> web.copy(detail = web.detail.copy(headerList = headerList)) }
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            transaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = web.detail.copy(headerList = emptyList()),
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            findWebList().single().detail.headerList shouldBe emptyList()
        }

        test("TC-e21780df-f80d-4cd1-9d5a-b621417cb573 계정과 식별자를 함께 만족하는 웹 항목에만 수정을 반영한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val web = web()
            val otherAccountWeb = web()
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            transaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            val changedDetail = Arb.webDetailLocalEntity().next()

            transaction.updateDetail(
                accountId = accountId,
                webId = otherAccountWeb.id,
                detail = changedDetail,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 0
            transaction.updateDetail(
                accountId = accountId,
                webId = Arb.kotlinUuid().next(),
                detail = changedDetail,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 0

            findWebList().map { stored -> stored.detail } shouldBe
                listOf(web, otherAccountWeb).sortedBy { stored -> stored.id.toString() }.map { stored -> stored.detail }

            transaction.updateDetail(
                accountId = accountId,
                webId = web.id,
                detail = changedDetail,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            findWebList().single { stored -> stored.id == web.id }.detail shouldBe changedDetail
            findWebList().single { stored -> stored.id == otherAccountWeb.id }.detail shouldBe otherAccountWeb.detail
        }

        test("TC-f29541ce-4f3d-4158-b103-8cb2996963e1 삭제는 삭제 여부와 수정 시각만 바꾸고 웹 항목과 계정 연결을 남긴다") {
            val accountId = Arb.kotlinUuid().next()
            val web = web().copy(isDeleted = false)
            val deletedAt = Arb.secondPrecisionInstant().next()
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

        test("TC-09965a51-889a-4d99-a260-a4349abc1a4c 계정과 식별자를 함께 만족하는 웹 항목에만 삭제를 반영한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val web = web().copy(isDeleted = false)
            val otherAccountWeb = web().copy(isDeleted = false)
            transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            transaction.upsert(accountId = otherAccountId, webList = listOf(otherAccountWeb), webTagList = emptyList())

            transaction.updateDeleted(
                accountId = accountId,
                webId = otherAccountWeb.id,
                isDeleted = true,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 0
            transaction.updateDeleted(
                accountId = accountId,
                webId = Arb.kotlinUuid().next(),
                isDeleted = true,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 0

            findWebList().none { stored -> stored.isDeleted } shouldBe true

            transaction.updateDeleted(
                accountId = accountId,
                webId = web.id,
                isDeleted = true,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            findWebList().filter { stored -> stored.isDeleted }.map { stored -> stored.id } shouldBe listOf(web.id)
        }

        test("TC-74d4a2ac-0065-4a70-ad58-dbc468f985c2 서로 다른 식별자의 웹 항목은 각각 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val webList = List(3) { web() }.sortedBy { web -> web.id.toString() }

            webList.forEach { web ->
                transaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            }

            findWebList() shouldBe webList
        }
    }) {
    public companion object {
        private fun web(): WebLocalEntity =
            Arb
                .webLocalEntity()
                .next()
                .copy(
                    detail = Arb.webDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

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
