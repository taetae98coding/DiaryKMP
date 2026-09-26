package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountQrLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountQrLocalDataSourceImpl
        lateinit var qrTransaction: AccountQrTransactionImpl
        lateinit var qrSyncTransaction: AccountQrSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountQrLocalDataSourceImpl(database = database)
            qrTransaction = AccountQrTransactionImpl(database = database)
            qrSyncTransaction = AccountQrSyncTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun PagingSource<Int, QrLocalEntity>.pagedQrs(loadSize: Int = 100): List<QrLocalEntity> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, QrLocalEntity>>().data
        }

        suspend fun pagedQrs(accountId: Uuid): List<QrLocalEntity> = dataSource.page(accountId = accountId).pagedQrs()

        suspend fun assertPageInvalidated(
            accountId: Uuid,
            expected: List<QrLocalEntity>,
            change: suspend () -> Unit,
        ) {
            val pagingSource = dataSource.page(accountId = accountId)
            pagingSource.pagedQrs()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            pagedQrs(accountId) shouldBe expected
        }

        test("TC-QR-HOME-DOMAIN-005 TC-QR-HOME-DOMAIN-007 삭제되지 않은 계정의 QR만 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE)
            val lastQr = qr(title = LAST_QR_TITLE)
            val deletedQr = qr().copy(isDeleted = true)
            val otherAccountQr = qr()
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, firstQr, deletedQr))
            qrTransaction.upsert(accountId = otherAccountId, qrList = listOf(otherAccountQr))

            pagedQrs(accountId) shouldBe listOf(firstQr, lastQr)
        }

        test("TC-QR-HOME-DOMAIN-007 설명과 QR 값은 정렬 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE, description = LAST_QR_TITLE, value = LAST_QR_TITLE)
            val lastQr = qr(title = LAST_QR_TITLE, description = FIRST_QR_TITLE, value = FIRST_QR_TITLE)
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, firstQr))

            pagedQrs(accountId) shouldBe listOf(firstQr, lastQr)
        }

        test("TC-QR-HOME-DOMAIN-007 수정 시각은 정렬 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val lastQr = qr(title = LAST_QR_TITLE).copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, firstQr))

            pagedQrs(accountId) shouldBe listOf(firstQr, lastQr)
        }

        test("TC-QR-HOME-DOMAIN-005 다른 계정의 QR만 저장되어 있으면 빈 목록으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            qrTransaction.upsert(accountId = otherAccountId, qrList = listOf(qr()))

            pagedQrs(accountId).shouldBeEmpty()
        }

        test("TC-QR-HOME-DATA-001 요청한 크기만큼 페이지로 나누어 이어서 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qrList = List(3) { index -> qr(title = "$index-${fixtureMonkey.giveMeOne<String>()}") }
            qrTransaction.upsert(accountId = accountId, qrList = qrList)

            val pagingSource = dataSource.page(accountId = accountId)
            val firstPage =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            val nextPage =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = checkNotNull(firstPage.nextKey), loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page

            firstPage.data shouldBe qrList.take(2)
            nextPage.data shouldBe qrList.drop(2)
        }

        test("TC-QR-HOME-DOMAIN-008 저장된 QR의 변화가 페이지 조회에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = qr()
            val renamedQr = qr.copy(detail = qr.detail.copy(title = "renamed-${fixtureMonkey.giveMeOne<String>()}"))

            pagedQrs(accountId).shouldBeEmpty()
            assertPageInvalidated(accountId = accountId, expected = listOf(qr)) {
                qrTransaction.upsert(accountId = accountId, qrList = listOf(qr))
            }
            assertPageInvalidated(accountId = accountId, expected = listOf(renamedQr)) {
                qrTransaction.upsert(accountId = accountId, qrList = listOf(renamedQr))
            }
            assertPageInvalidated(accountId = accountId, expected = emptyList()) {
                qrTransaction.updateDeleted(accountId = accountId, qrId = qr.id, isDeleted = true, updatedAt = instant())
            }
        }

        test("TC-QR-HOME-DOMAIN-008 노출 기준을 만족하는 QR을 추가하면 제목 순서에 맞는 자리에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE)
            val middleQr = qr(title = MIDDLE_QR_TITLE)
            val lastQr = qr(title = LAST_QR_TITLE)
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, firstQr))

            assertPageInvalidated(accountId = accountId, expected = listOf(firstQr, middleQr, lastQr)) {
                qrTransaction.upsert(accountId = accountId, qrList = listOf(middleQr))
            }
        }

        test("TC-QR-HOME-DOMAIN-008 목록에 있는 QR이 삭제 상태로 바뀌면 목록에서 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE)
            val middleQr = qr(title = MIDDLE_QR_TITLE)
            val lastQr = qr(title = LAST_QR_TITLE)
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, middleQr, firstQr))

            assertPageInvalidated(accountId = accountId, expected = listOf(firstQr, lastQr)) {
                qrTransaction.updateDeleted(accountId = accountId, qrId = middleQr.id, isDeleted = true, updatedAt = instant())
            }
        }

        test("TC-QR-HOME-DOMAIN-008 서버에서 내려받은 QR은 노출 기준과 정렬 기준에 따라 목록에 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE)
            val lastQr = qr(title = LAST_QR_TITLE)
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, firstQr))
            val downloadedQr = qr(title = MIDDLE_QR_TITLE)
            val downloadedDeletedQr = qr().copy(isDeleted = true)

            assertPageInvalidated(accountId = accountId, expected = listOf(firstQr, downloadedQr, lastQr)) {
                qrSyncTransaction.save(accountId = accountId, qrList = listOf(downloadedDeletedQr, downloadedQr), cursor = 1L)
            }
        }

        test("TC-QR-HOME-DOMAIN-008 실행 취소한 QR은 제목 오름차순에 맞는 자리로 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstQr = qr(title = FIRST_QR_TITLE)
            val middleQr = qr(title = MIDDLE_QR_TITLE)
            val lastQr = qr(title = LAST_QR_TITLE)
            qrTransaction.upsert(accountId = accountId, qrList = listOf(lastQr, middleQr, firstQr))
            qrTransaction.updateDeleted(accountId = accountId, qrId = middleQr.id, isDeleted = true, updatedAt = instant())
            pagedQrs(accountId) shouldBe listOf(firstQr, lastQr)
            val restoredAt = instant()

            qrTransaction.updateDeleted(accountId = accountId, qrId = middleQr.id, isDeleted = false, updatedAt = restoredAt)

            pagedQrs(accountId) shouldBe listOf(firstQr, middleQr.copy(updatedAt = restoredAt), lastQr)
        }
    }) {
    public companion object {
        private const val FIRST_QR_TITLE = "AppleQr"
        private const val MIDDLE_QR_TITLE = "MangoQr"
        private const val LAST_QR_TITLE = "ZebraQr"
        private const val INVALIDATION_TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun qr(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            description: String = fixtureMonkey.giveMeOne<String>(),
            value: String = "value-${fixtureMonkey.giveMeOne<String>()}",
        ): QrLocalEntity =
            fixtureMonkey
                .localQr(isDeleted = false)
                .copy(detail = QrDetailLocalEntity(title = title, description = description, value = value))

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
