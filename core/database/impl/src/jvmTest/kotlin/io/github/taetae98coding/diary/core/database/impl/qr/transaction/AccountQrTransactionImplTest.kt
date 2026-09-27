package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountQrTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountQrTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountQrTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-QR-ADD-DATA-001 TC-QR-ADD-DATA-003 TC-DATA-SYNC-DOMAIN-001 QR과 현재 계정의 연결을 업로드 대기 상태로 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)

            transaction.upsert(accountId = accountId, qrList = listOf(qr))

            database.findQrList() shouldBe listOf(qr)
            database.findAccountQrList() shouldBe listOf(AccountQrLocalEntity(accountId = accountId, qrId = qr.id, isDirty = true))
        }

        test("TC-QR-ADD-DOMAIN-010 TC-QR-ADD-DATA-005 제목, 설명과 QR 값을 앞뒤 공백과 줄바꿈을 포함해 입력한 그대로 저장하고 함께 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr =
                fixtureMonkey.localQr(isDeleted = false).copy(
                    detail =
                        QrDetailLocalEntity(
                            title = "  title-${fixtureMonkey.giveMeOne<String>()}  ",
                            description = "  ${fixtureMonkey.giveMeOne<String>()}\n${fixtureMonkey.giveMeOne<String>()}  ",
                            value = "  ${fixtureMonkey.giveMeOne<String>()}\n${fixtureMonkey.giveMeOne<String>()}  ",
                        ),
                )

            transaction.upsert(accountId = accountId, qrList = listOf(qr))

            database.findQrList() shouldBe listOf(qr)
        }

        test("TC-QR-ADD-DOMAIN-009 공백만 있는 QR 값과 빈 설명도 그대로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false).let { qr -> qr.copy(detail = qr.detail.copy(description = "", value = " ")) }

            transaction.upsert(accountId = accountId, qrList = listOf(qr))

            database.findQrList() shouldBe listOf(qr)
        }

        test("TC-QR-ADD-DOMAIN-011 같은 내용의 QR을 추가해도 이미 저장된 QR을 덮어쓰지 않고 따로 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val savedQr = fixtureMonkey.localQr(isDeleted = false)
            val addedQr = fixtureMonkey.localQr(isDeleted = false).copy(detail = savedQr.detail)
            transaction.upsert(accountId = accountId, qrList = listOf(savedQr))

            transaction.upsert(accountId = accountId, qrList = listOf(addedQr))

            database.findQrList() shouldBe listOf(savedQr, addedQr).sortedBy { qr -> qr.id.toString() }
        }

        test("TC-QR-HOME-DOMAIN-009 TC-QR-HOME-DATA-004 TC-DATA-SYNC-DOMAIN-001 삭제는 그 QR의 삭제 여부와 수정 시각만 바꾸고 QR과 계정 연결을 남기며 업로드 대기로 기록한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)
            val otherQr = fixtureMonkey.localQr(isDeleted = false)
            transaction.upsert(accountId = accountId, qrList = listOf(qr, otherQr))
            database.markQrUploaded(accountId = accountId, qrId = qr.id)
            database.markQrUploaded(accountId = accountId, qrId = otherQr.id)
            val updatedAt = instant()

            transaction.updateDeleted(accountId = accountId, qrId = qr.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            database.findQrList() shouldBe listOf(qr.copy(isDeleted = true, updatedAt = updatedAt), otherQr).sortedBy { qr -> qr.id.toString() }
            database.findAccountQrList() shouldBe
                listOf(
                    AccountQrLocalEntity(accountId = accountId, qrId = qr.id, isDirty = true),
                    AccountQrLocalEntity(accountId = accountId, qrId = otherQr.id, isDirty = false),
                ).sortedBy { accountQr -> accountQr.qrId.toString() }
        }

        test("TC-QR-HOME-DOMAIN-010 TC-QR-HOME-DATA-004 TC-DATA-SYNC-DOMAIN-001 삭제의 실행 취소는 그 QR의 삭제 여부를 미삭제로 되돌리고 업로드 대기로 기록한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = true)
            val otherQr = fixtureMonkey.localQr(isDeleted = true)
            transaction.upsert(accountId = accountId, qrList = listOf(qr, otherQr))
            database.markQrUploaded(accountId = accountId, qrId = qr.id)
            database.markQrUploaded(accountId = accountId, qrId = otherQr.id)
            val updatedAt = instant()

            transaction.updateDeleted(accountId = accountId, qrId = qr.id, isDeleted = false, updatedAt = updatedAt) shouldBe 1

            database.findQrList() shouldBe listOf(qr.copy(isDeleted = false, updatedAt = updatedAt), otherQr).sortedBy { qr -> qr.id.toString() }
            database.findAccountQrList() shouldBe
                listOf(
                    AccountQrLocalEntity(accountId = accountId, qrId = qr.id, isDirty = true),
                    AccountQrLocalEntity(accountId = accountId, qrId = otherQr.id, isDirty = false),
                ).sortedBy { accountQr -> accountQr.qrId.toString() }
        }

        test("TC-QR-HOME-DOMAIN-009 TC-QR-HOME-DOMAIN-010 현재 계정과 연결된 QR이 아니면 삭제와 실행 취소가 아무것도 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)
            transaction.upsert(accountId = accountId, qrList = listOf(qr))
            database.markQrUploaded(accountId = accountId, qrId = qr.id)

            transaction.updateDeleted(accountId = otherAccountId, qrId = qr.id, isDeleted = true, updatedAt = instant()) shouldBe 0
            transaction.updateDeleted(accountId = otherAccountId, qrId = qr.id, isDeleted = false, updatedAt = instant()) shouldBe 0

            database.findQrList() shouldBe listOf(qr)
            database.findAccountQrList() shouldBe listOf(AccountQrLocalEntity(accountId = accountId, qrId = qr.id, isDirty = false))
        }

        test("TC-QR-ADD-DATA-002 저장에 실패하면 QR과 계정 연결 중 어느 것도 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountQrDao() } throws throwable
            val failingTransaction = AccountQrTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(accountId = accountId, qrList = listOf(fixtureMonkey.localQr(isDeleted = false)))
            }.message shouldBe throwable.message

            database.findQrList().shouldBeEmpty()
            database.findAccountQrList().shouldBeEmpty()
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
