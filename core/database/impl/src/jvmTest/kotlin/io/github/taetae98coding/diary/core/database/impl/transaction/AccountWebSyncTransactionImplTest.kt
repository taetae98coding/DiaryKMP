package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.withWriteTransaction
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.datasource.AccountWebSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class WebSyncTestException : RuntimeException()

class AccountWebSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountWebSyncTransactionImpl
        lateinit var syncDataSource: AccountWebSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl
        lateinit var accountTransaction: AccountWebTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountWebSyncTransactionImpl(database = database)
            syncDataSource = AccountWebSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
            accountTransaction = AccountWebTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            web: WebLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.webDao().upsert(listOf(web))
                database.accountWebDao().upsert(
                    listOf(
                        AccountWebLocalEntity(
                            accountId = accountId,
                            webId = web.id,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findWeb(webId: Uuid): WebLocalEntity? =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT id, title, description, url, header_list, is_deleted, updated_at, created_at
                    FROM web
                    WHERE id = '$webId'
                    """,
                ) { statement -> if (statement.step()) statement.toWeb() else null }
            }

        suspend fun isPending(
            accountId: Uuid,
            webId: Uuid,
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { web -> web.id == webId }

        test("TC-WEB-ADD-DATA-005 현재 계정의 업로드 대기 웹 항목만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPendingWeb = web()
            val secondPendingWeb = web()
            val syncedWeb = web()
            val otherAccountWeb = web()
            insertWithSyncState(accountId, firstPendingWeb, isDirty = true)
            insertWithSyncState(accountId, secondPendingWeb, isDirty = true)
            insertWithSyncState(accountId, syncedWeb, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountWeb, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPendingWeb, secondPendingWeb)
        }

        test("TC-WEB-ADD-DATA-005 저장한 웹 항목은 업로드 대기로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()

            accountTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            isPending(accountId = accountId, webId = web.id) shouldBe true
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWithSyncState(accountId, web, isDirty = true)

            transaction.clearPending(accountId = accountId, webList = listOf(web))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 웹 항목은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedWeb = web(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedWeb = pushedWeb.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changedWeb, isDirty = true)

            transaction.clearPending(accountId = accountId, webList = listOf(pushedWeb))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedWeb)
        }

        test("TC-DATA-SYNC-DATA-016 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstCursor = 3L
            val secondCursor = 11L

            transaction.save(accountId = accountId, webList = listOf(web()), cursor = firstCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB) shouldBe firstCursor

            transaction.save(accountId = accountId, webList = listOf(web()), cursor = secondCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB) shouldBe secondCursor
        }

        test("TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            transaction.save(accountId = otherAccountId, webList = listOf(web()), cursor = 9L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-028 웹 항목의 내려받기 위치는 다른 종류와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, webList = listOf(web()), cursor = 7L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB) shouldBe 7L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.PLACE) shouldBe 0L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localWeb = web(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteWeb =
                    localWeb.copy(
                        detail = detail(),
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId, localWeb, isDirty = false)

                transaction.save(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

                findWeb(webId = localWeb.id) shouldBe remoteWeb
            }
        }

        test("TC-DATA-SYNC-DATA-031 내려받은 웹 항목의 요청 헤더 목록이 기기의 목록을 대체한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localWeb =
                web(updatedAt = Instant.fromEpochMilliseconds(1_000)).let { web ->
                    web.copy(
                        detail =
                            web.detail.copy(
                                headerList =
                                    listOf(
                                        WebHeaderLocalEntity(name = "Authorization", value = "local"),
                                        WebHeaderLocalEntity(name = "X-Region", value = "local"),
                                    ),
                            ),
                    )
                }
            val remoteHeaderList =
                listOf(
                    WebHeaderLocalEntity(name = "X-Region", value = "remote"),
                    WebHeaderLocalEntity(name = "Authorization", value = "remote"),
                    WebHeaderLocalEntity(name = "Authorization", value = ""),
                )
            val remoteWeb =
                localWeb.copy(
                    detail = localWeb.detail.copy(headerList = remoteHeaderList),
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                )
            insertWithSyncState(accountId, localWeb, isDirty = false)

            transaction.save(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

            findWeb(webId = localWeb.id)?.detail?.headerList shouldBe remoteHeaderList
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localWeb = web(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteWeb =
                localWeb.copy(
                    detail = detail(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId, localWeb, isDirty = true)
            val cursor = 5L

            transaction.save(accountId = accountId, webList = listOf(remoteWeb), cursor = cursor)

            findWeb(webId = localWeb.id) shouldBe localWeb
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB) shouldBe cursor
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이름" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localWeb = web(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteWeb = localWeb.copy(detail = detail(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, localWeb, isDirty = true)

                transaction.save(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

                isPending(accountId = accountId, webId = localWeb.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 웹 항목은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteWeb = web()

            transaction.save(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

            findWeb(webId = remoteWeb.id) shouldBe remoteWeb
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 웹 항목과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteWeb = web()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws WebSyncTestException()
            val failingTransaction = AccountWebSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<WebSyncTestException> {
                failingTransaction.save(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)
            }

            findWeb(webId = remoteWeb.id).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.WEB) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-027 업로드한 웹 항목이 같은 내용으로 다시 내려와도 기기 내용은 그대로다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWithSyncState(accountId, web, isDirty = false)

            transaction.save(accountId = accountId, webList = listOf(web), cursor = 5L)

            findWeb(webId = web.id) shouldBe web
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWithSyncState(accountId, web, isDirty = true)
            insertWithSyncState(otherAccountId, web, isDirty = true)

            transaction.clearPending(accountId = accountId, webList = listOf(web))

            isPending(accountId = accountId, webId = web.id) shouldBe false
            isPending(accountId = otherAccountId, webId = web.id) shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(updatedAt: Instant = instant()): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::detail, detail())
                .setExp(WebLocalEntity::isDeleted, false)
                .setExp(WebLocalEntity::updatedAt, updatedAt)
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun detail(): WebDetailLocalEntity = fixtureMonkey.giveMeOne<WebDetailLocalEntity>()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

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
    }
}
