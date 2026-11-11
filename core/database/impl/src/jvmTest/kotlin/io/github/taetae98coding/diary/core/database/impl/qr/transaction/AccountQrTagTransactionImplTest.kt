package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.AccountTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

// QR에 고른 태그가 완료·삭제로 바뀌었을 때, 기기에 저장된 내용으로 고를 수 있는 태그와 연결된 태그가 어떻게 조회되는지 본다.
class AccountQrTagTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountQrTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountQrTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertTag(
            accountId: Uuid,
            tag: TagLocalEntity,
        ) {
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
        }

        suspend fun addQr(
            accountId: Uuid,
            tag: TagLocalEntity,
        ): QrLocalEntity {
            val qr = Arb.localQr(isDeleted = false).next()

            transaction.upsert(accountId = accountId, qrList = listOf(qr), qrTagList = listOf(link(qr = qr, tag = tag)))

            return qr
        }

        suspend fun selectableTagIdList(
            accountId: Uuid,
            tagId: Uuid,
        ): List<Uuid> =
            AccountTagLocalDataSourceImpl(database = database)
                .get(accountId = accountId, tagIdSet = setOf(tagId))
                .first()
                .map { tag -> tag.id }

        suspend fun linkedTagIdList(
            accountId: Uuid,
            qrId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.QR, entityId = qrId)
                .first()
                .map { tag -> tag.id }

        test("TC-570fc7c4-fb6e-4878-a5d5-a0c83b9fd6da 고른 태그가 완료되면 연결 대상 조회에서 빠진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId = accountId, tag = tag)
            selectableTagIdList(accountId = accountId, tagId = tag.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())

            selectableTagIdList(accountId = accountId, tagId = tag.id).shouldBeEmpty()
        }

        test("TC-570fc7c4-fb6e-4878-a5d5-a0c83b9fd6da 고른 태그가 삭제되면 연결 대상 조회에서 빠진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId = accountId, tag = tag)
            selectableTagIdList(accountId = accountId, tagId = tag.id) shouldBe listOf(tag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())

            selectableTagIdList(accountId = accountId, tagId = tag.id).shouldBeEmpty()
        }

        test("TC-4eaac45d-26f2-4397-af48-a82d86675b92 고른 뒤 완료된 태그와의 연결도 저장되어 그 태그가 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId = accountId, tag = tag)
            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())

            val qr = addQr(accountId = accountId, tag = tag)

            linkedTagIdList(accountId = accountId, qrId = qr.id) shouldBe listOf(tag.id)
        }

        test("TC-4eaac45d-26f2-4397-af48-a82d86675b92 고른 뒤 삭제된 태그와의 연결도 저장되어 삭제를 되돌리면 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId = accountId, tag = tag)
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())

            val qr = addQr(accountId = accountId, tag = tag)

            linkedTagIdList(accountId = accountId, qrId = qr.id).shouldBeEmpty()
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, qrId = qr.id) shouldBe listOf(tag.id)
        }

        test("TC-357e7da5-e21a-48cb-8cb9-b47911ccb23a 연결된 태그는 완료되어도 조회되고 삭제되면 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId = accountId, tag = tag)
            val qr = addQr(accountId = accountId, tag = tag)
            linkedTagIdList(accountId = accountId, qrId = qr.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, qrId = qr.id) shouldBe listOf(tag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
            linkedTagIdList(accountId = accountId, qrId = qr.id).shouldBeEmpty()
        }
    }) {
    private companion object {
        fun tag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)

        fun link(
            qr: QrLocalEntity,
            tag: TagLocalEntity,
        ): EntityTagLocalEntity = EntityTagLocalEntity(entityId = qr.id, tagId = tag.id, isDeleted = false, updatedAt = qr.updatedAt, createdAt = qr.createdAt)
    }
}
