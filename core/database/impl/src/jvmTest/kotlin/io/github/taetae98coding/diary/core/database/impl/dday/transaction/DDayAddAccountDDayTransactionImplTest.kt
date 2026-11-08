package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDaySyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.ddaytag.ddayTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlin.uuid.Uuid

class DDayAddAccountDDayTransactionImplTest :
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

        test("TC-5c49ab50-b5f1-497b-8540-8d7c1b32f4b9 디데이, 계정 연결과 고른 두 태그와의 연결을 함께 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagList = List(3) { tag() }
            insertTag(accountId, *tagList.toTypedArray())
            val dday = newDDay()

            transaction.upsert(accountId = accountId, dday = dday, ddayTagList = linkList(dday = dday, tagList = tagList.take(2)))

            find(accountId = accountId, ddayId = dday.id) shouldBe dday
            linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldContainExactlyInAnyOrder tagList.take(2).map { tag -> tag.id }
        }

        test("TC-0c20d174-5a50-4dce-8844-fdcd2b64e1e4 저장에 실패하면 디데이, 계정 연결과 태그 연결 중 어느 것도 남지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            val dday = newDDay()
            val failingDatabase = spyk(database)
            every { failingDatabase.accountDDayDao() } throws DDayAddTestException(Arb.string().next())

            shouldThrowExactly<DDayAddTestException> {
                AccountDDayTransactionImpl(database = failingDatabase).upsert(accountId = accountId, dday = dday, ddayTagList = linkList(dday = dday, tagList = listOf(tag)))
            }

            database.ddayDao().findUpdatedAt(listOf(dday.id)).shouldBeEmpty()
            find(accountId = accountId, ddayId = dday.id) shouldBe null
            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).shouldBeEmpty()
            database.ddayTagTable().findByEntityIdList(listOf(dday.id)).shouldBeEmpty()
        }

        test("TC-21d29a4d-2bf4-4c58-ac6c-d6844fc8cfc3 반복 간격은 디데이와 함께 저장되고 함께 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val repeats = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 3), DateTimeUnit.MonthBased(months = 12))
            val dday = newDDay().let { value -> value.copy(detail = value.detail.copy(repeats = repeats)) }

            transaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())

            val found = find(accountId = accountId, ddayId = dday.id)
            found shouldBe dday
            found?.detail?.repeats shouldBe repeats
        }

        test("TC-59804ef8-703f-41a2-b0fd-7747b4210ab8 저장한 디데이와 태그 연결이 각각 계정의 업로드 대기로 기록된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            insertTag(accountId, tag)
            val dday = newDDay()

            transaction.upsert(accountId = accountId, dday = dday, ddayTagList = linkList(dday = dday, tagList = listOf(tag)))

            AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId) shouldBe listOf(dday)
            AccountEntityTagSyncLocalDataSourceImpl(database = database)
                .readPendingList(accountId = accountId, kind = EntityTagKindLocalEntity.DDAY)
                .map { link -> link.entityId to link.tagId } shouldBe listOf(dday.id to tag.id)
        }

        test("TC-63fbc1a6-ba35-434e-968d-68b1cc998697 같은 정보의 디데이도 다른 식별 정보로 저장하면 두 디데이가 모두 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val first = newDDay()
            val second = first.copy(id = Arb.kotlinUuid().next())

            transaction.upsert(accountId = accountId, dday = first, ddayTagList = emptyList())
            transaction.upsert(accountId = accountId, dday = second, ddayTagList = emptyList())

            find(accountId = accountId, ddayId = first.id) shouldBe first
            find(accountId = accountId, ddayId = second.id) shouldBe second
        }
    }) {
    private companion object {
        fun newDDay(): DDayLocalEntity = Arb.ddayLocalEntity().next().copy(isDeleted = false)

        fun tag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)

        fun linkList(
            dday: DDayLocalEntity,
            tagList: List<TagLocalEntity>,
        ): List<EntityTagLocalEntity> =
            tagList.map { tag ->
                EntityTagLocalEntity(entityId = dday.id, tagId = tag.id, isDeleted = false, updatedAt = dday.updatedAt, createdAt = dday.createdAt)
            }
    }
}

private class DDayAddTestException(
    message: String,
) : RuntimeException(message)
