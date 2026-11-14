package io.github.taetae98coding.diary.core.database.impl.entitytag.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

// 연락처 추가·상세 문서가 정한 태그 연결 조회 결과를, 연락처를 실제로 저장한 뒤 연결된 태그를 조회해 확인한다.
class AccountEntityTagLocalDataSourceImplContactTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountEntityTagLocalDataSourceImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var contactTransaction: AccountContactTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountEntityTagLocalDataSourceImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            contactTransaction = AccountContactTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            tagTransaction.upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
        }

        suspend fun addContact(
            accountId: Uuid,
            contact: ContactLocalEntity,
            vararg tagList: TagLocalEntity,
        ) {
            contactTransaction.upsert(
                accountId = accountId,
                contactList = listOf(contact),
                contactTagList =
                    tagList.map { tag ->
                        EntityTagLocalEntity(
                            entityId = contact.id,
                            tagId = tag.id,
                            isDeleted = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                            createdAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
            )
        }

        suspend fun linkedTagIdList(
            accountId: Uuid,
            contactId: Uuid,
        ): List<Uuid> =
            dataSource
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.CONTACT, entityId = contactId)
                .first()
                .map { tag -> tag.id }

        test("TC-c1238327-db56-4e68-8db2-13bff6b592d4 고른 뒤 완료된 태그는 추가한 연락처와 연결되어 연결된 태그로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val tag = tag()
            insertTag(accountId, tag)
            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())

            addContact(accountId, contact, tag)

            linkedTagIdList(accountId = accountId, contactId = contact.id) shouldBe listOf(tag.id)
        }

        test("TC-c1238327-db56-4e68-8db2-13bff6b592d4 고른 뒤 삭제된 태그도 추가한 연락처와 연결되어 삭제를 되돌리면 연결된 태그로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val tag = tag()
            insertTag(accountId, tag)
            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())

            addContact(accountId, contact, tag)

            linkedTagIdList(accountId = accountId, contactId = contact.id).shouldBeEmpty()

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next())

            linkedTagIdList(accountId = accountId, contactId = contact.id) shouldBe listOf(tag.id)
        }

        test("TC-c7408895-4227-4d3e-b9ce-5293a99f8aa4 고른 두 태그만 추가한 연락처의 연결된 태그로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val selectedTagList = List(2) { tag() }
            val unselectedTag = tag()
            insertTag(accountId, *selectedTagList.toTypedArray(), unselectedTag)

            addContact(accountId, contact, *selectedTagList.toTypedArray())

            linkedTagIdList(accountId = accountId, contactId = contact.id) shouldContainExactlyInAnyOrder selectedTagList.map { tag -> tag.id }
        }

        test("TC-aec8ea46-3fa8-4477-9fa6-7cdaa5dd73e6 연결된 태그는 미완료·미삭제와 완료 상태에서 조회되고 삭제 상태에서 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact()
            val tag = tag()
            insertTag(accountId, tag)
            addContact(accountId, contact, tag)

            linkedTagIdList(accountId = accountId, contactId = contact.id) shouldBe listOf(tag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())

            linkedTagIdList(accountId = accountId, contactId = contact.id) shouldBe listOf(tag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())

            linkedTagIdList(accountId = accountId, contactId = contact.id).shouldBeEmpty()
        }
    }) {
    private companion object {
        fun tag(): TagLocalEntity = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)

        fun contact(): ContactLocalEntity = Arb.contactLocalEntity().next().copy(isDeleted = false)
    }
}
