package io.github.taetae98coding.diary.core.database.impl.web.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.withWriteTransaction
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.web.datasource.AccountWebSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
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
        ): Boolean = syncDataSource.readPendingList(accountId = accountId).any { web -> web.id == webId }

        test("TC-861e6069-e620-46f7-82d9-4be6e396b1c1 현재 계정의 업로드 대기 웹 항목만 조회한다") {
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
                .readPendingList(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPendingWeb, secondPendingWeb)
        }

        test("TC-861e6069-e620-46f7-82d9-4be6e396b1c1 저장한 웹 항목은 업로드 대기로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()

            accountTransaction.upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())

            isPending(accountId = accountId, webId = web.id) shouldBe true
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 웹 항목은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = web()
            val accountEntity = web()
            insertWithSyncState(accountId = Uuid.NIL, web = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, web = accountEntity, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWithSyncState(accountId, web, isDirty = true)

            transaction.clearPending(accountId = accountId, webList = listOf(web))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 웹 항목은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedWeb = web(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedWeb = pushedWeb.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changedWeb, isDirty = true)

            transaction.clearPending(accountId = accountId, webList = listOf(pushedWeb))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changedWeb)
        }

        test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstCursor = 3L
            val secondCursor = 11L

            transaction.upsert(accountId = accountId, webList = listOf(web()), cursor = firstCursor)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.WEB) shouldBe firstCursor

            transaction.upsert(accountId = accountId, webList = listOf(web()), cursor = secondCursor)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.WEB) shouldBe secondCursor
        }

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 기록된 순번이 없으면 기본 커서를 사용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            transaction.upsert(accountId = otherAccountId, webList = listOf(web()), cursor = 9L)

            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.WEB) shouldBe 0L
        }

        test("TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 웹 항목의 내려받기 위치는 다른 종류와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.upsert(accountId = accountId, webList = listOf(web()), cursor = 7L)

            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.WEB) shouldBe 7L
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.PLACE) shouldBe 0L
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localWeb = web(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteWeb =
                    localWeb.copy(
                        detail = detail(),
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId, localWeb, isDirty = false)

                transaction.upsert(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

                findWeb(webId = localWeb.id) shouldBe remoteWeb
            }
        }

        test("TC-50c14ed3-6df2-4ce6-9bd8-9c1ac2c209ae 내려받은 웹 항목의 요청 헤더 목록이 기기의 목록을 대체한다") {
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

            transaction.upsert(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

            findWeb(webId = localWeb.id)?.detail?.headerList shouldBe remoteHeaderList
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localWeb = web(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteWeb =
                localWeb.copy(
                    detail = detail(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId, localWeb, isDirty = true)
            val cursor = 5L

            transaction.upsert(accountId = accountId, webList = listOf(remoteWeb), cursor = cursor)

            findWeb(webId = localWeb.id) shouldBe localWeb
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.WEB) shouldBe cursor
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localWeb = web(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteWeb = localWeb.copy(detail = detail(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, localWeb, isDirty = true)

                transaction.upsert(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

                isPending(accountId = accountId, webId = localWeb.id) shouldBe true
            }
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 웹 항목은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteWeb = web()

            transaction.upsert(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)

            findWeb(webId = remoteWeb.id) shouldBe remoteWeb
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 웹 항목과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteWeb = web()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws WebSyncTestException()
            val failingTransaction = AccountWebSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<WebSyncTestException> {
                failingTransaction.upsert(accountId = accountId, webList = listOf(remoteWeb), cursor = 5L)
            }

            findWeb(webId = remoteWeb.id).shouldBeNull()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.WEB) shouldBe 0L
        }

        test("TC-a748604b-2b82-4ee6-a15a-2185c71e18a0 업로드한 웹 항목이 같은 내용으로 다시 내려와도 기기 내용은 그대로다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val web = web()
            insertWithSyncState(accountId, web, isDirty = false)

            transaction.upsert(accountId = accountId, webList = listOf(web), cursor = 5L)

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

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()

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
