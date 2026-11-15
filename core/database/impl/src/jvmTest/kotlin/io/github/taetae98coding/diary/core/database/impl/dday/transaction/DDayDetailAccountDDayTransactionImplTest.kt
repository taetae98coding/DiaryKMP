package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDaySyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.ddayDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class DDayDetailAccountDDayTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountDDayTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountDDayTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        suspend fun insertUploaded(
            accountId: Uuid,
            isDeleted: Boolean,
            tagList: List<TagLocalEntity> = emptyList(),
        ): DDayLocalEntity {
            val dday = Arb.ddayLocalEntity().next().copy(isDeleted = isDeleted)
            transaction.upsert(
                accountId = accountId,
                dday = dday,
                ddayTagList =
                    tagList.map { tag ->
                        EntityTagLocalEntity(entityId = dday.id, tagId = tag.id, isDeleted = false, updatedAt = dday.updatedAt, createdAt = dday.createdAt)
                    },
            )
            val syncDataSource = AccountDDaySyncLocalDataSourceImpl(database = database)
            AccountDDaySyncTransactionImpl(database = database).clearPending(accountId = accountId, ddayList = syncDataSource.readPendingList(accountId = accountId))
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            return dday
        }

        suspend fun find(
            accountId: Uuid,
            ddayId: Uuid,
        ): DDayLocalEntity? = AccountDDayLocalDataSourceImpl(database = database).find(accountId = accountId, ddayId = ddayId).first()

        suspend fun linkedTagIdList(
            accountId: Uuid,
            ddayId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.DDAY, entityId = ddayId)
                .first()
                .map { tag -> tag.id }

        suspend fun homeIdList(accountId: Uuid): List<Uuid> =
            AccountDDayLocalDataSourceImpl(database = database)
                .page(accountId = accountId)
                .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
                .data
                .map { dday -> dday.id }

        test("TC-c4c2466a-deac-4b32-892a-619e43947493 삭제 상태인 디데이도 상세 대상으로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, isDeleted = true)

            find(accountId = accountId, ddayId = dday.id) shouldBe dday
        }

        test("TC-e8e52e4a-5369-4c79-abe4-1617bc254ab2 다른 계정과만 연결된 디데이는 없고 현재 계정의 디데이는 반복 간격과 함께 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val mine = insertUploaded(accountId = accountId, isDeleted = false)
            val others = insertUploaded(accountId = otherAccountId, isDeleted = false)

            find(accountId = accountId, ddayId = others.id) shouldBe null
            find(accountId = accountId, ddayId = mine.id) shouldBe mine
            find(accountId = accountId, ddayId = mine.id)?.detail?.repeats shouldBe mine.detail.repeats
        }

        test("TC-1bb084c7-047c-40cc-9d22-ca00c1e3843e TC-36c2fd08-bb27-435f-92e0-cd08e282d8a3 TC-439ff23c-cd9a-42eb-9348-dcbfb47a7fc4 수정은 정보와 수정 시각만 바꾸고 업로드 대기로 기록한다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag))
            val detail = Arb.ddayDetailLocalEntity().next()
            val updatedAt = Arb.secondPrecisionInstant().next()

            transaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = detail, updatedAt = updatedAt) shouldBe 1

            find(accountId = accountId, ddayId = dday.id) shouldBe dday.copy(detail = detail, updatedAt = updatedAt)
            linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)
            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { value -> value.id } shouldBe listOf(dday.id)
        }

        test("TC-244a7551-a306-4742-8a65-39ddda6abed6 삭제 상태인 디데이를 수정해도 삭제 상태로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, isDeleted = true)
            val detail = Arb.ddayDetailLocalEntity().next()

            transaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = detail, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            find(accountId = accountId, ddayId = dday.id)?.isDeleted shouldBe true
            find(accountId = accountId, ddayId = dday.id)?.detail shouldBe detail
        }

        test("TC-c802b994-26d9-4e7f-ac93-7feea6e64b39 TC-439ff23c-cd9a-42eb-9348-dcbfb47a7fc4 삭제는 삭제 여부와 수정 시각만 바꾸고 목록에서 사라진다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag))
            homeIdList(accountId) shouldBe listOf(dday.id)
            val updatedAt = Arb.secondPrecisionInstant().next()

            transaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            find(accountId = accountId, ddayId = dday.id) shouldBe dday.copy(isDeleted = true, updatedAt = updatedAt)
            linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)
            homeIdList(accountId).shouldBeEmpty()
            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { value -> value.id } shouldBe listOf(dday.id)
        }

        test("TC-08dd945f-b0bd-415f-bcd8-9e814c3ed3e8 다른 계정과만 연결된 디데이는 수정하거나 삭제하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = otherAccountId, isDeleted = false)

            transaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = Arb.ddayDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0

            find(accountId = otherAccountId, ddayId = dday.id) shouldBe dday
            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = otherAccountId).shouldBeEmpty()
            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-08dd945f-b0bd-415f-bcd8-9e814c3ed3e8 기기에 없는 디데이는 수정하거나 삭제하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val stored = insertUploaded(accountId = accountId, isDeleted = false)
            val missingId = Arb.kotlinUuid().next()

            transaction.updateDetail(accountId = accountId, ddayId = missingId, detail = Arb.ddayDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0
            transaction.updateDeleted(accountId = accountId, ddayId = missingId, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 0

            find(accountId = accountId, ddayId = stored.id) shouldBe stored
            find(accountId = accountId, ddayId = missingId) shouldBe null
            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
        }

        listOf(true, false).forEach { isDelete ->
            test("TC-e16a87ea-a3e4-4cbe-9fce-6091637d1f84 ${if (isDelete) "삭제" else "내용 수정"} 저장에 실패하면 저장된 디데이와 태그 연결이 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = tag()
                insertTag(accountId, tag)
                val dday = insertUploaded(accountId = accountId, isDeleted = false, tagList = listOf(tag))
                val failingDao = spyk(database.accountDDayDao())
                coEvery { failingDao.markPending(accountId = any(), ddayId = any()) } throws DDayDetailTestException(Arb.string().next())
                val failingDatabase = spyk(database)
                every { failingDatabase.accountDDayDao() } returns failingDao
                val failingTransaction = AccountDDayTransactionImpl(database = failingDatabase)

                shouldThrowExactly<DDayDetailTestException> {
                    if (isDelete) {
                        failingTransaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
                    } else {
                        failingTransaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = Arb.ddayDetailLocalEntity().next(), updatedAt = Arb.secondPrecisionInstant().next())
                    }
                }

                find(accountId = accountId, ddayId = dday.id) shouldBe dday
                linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)
                AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            }
        }
    }) {
    private companion object {
        fun tag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)
    }
}

private class DDayDetailTestException(
    message: String,
) : RuntimeException(message)
