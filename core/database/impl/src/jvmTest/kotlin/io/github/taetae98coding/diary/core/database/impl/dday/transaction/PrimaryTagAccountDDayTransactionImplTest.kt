package io.github.taetae98coding.diary.core.database.impl.dday.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.data.testing.dday.ddayDetailLocalEntity
import io.github.taetae98coding.diary.core.data.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.data.testing.tag.localTag
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDaySyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.ddayTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class DDayPrimaryTagTestException : RuntimeException()

private class LinkPrimaryCase(
    val label: String,
    val isLinkedBefore: Boolean,
    val isLinkedAfter: Boolean,
    val isPrimaryAfter: Boolean,
    val action: suspend (accountId: Uuid, ddayId: Uuid, tagId: Uuid, updatedAt: Instant) -> Unit,
)

class PrimaryTagAccountDDayTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountDDayTransactionImpl
        lateinit var syncDataSource: AccountDDaySyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountDDayTransactionImpl(database = database)
            syncDataSource = AccountDDaySyncLocalDataSourceImpl(database = database)
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

        // 태그 연결과 함께 저장하고 업로드를 마친 상태로 만든다.
        suspend fun insertUploaded(
            accountId: Uuid,
            dday: DDayLocalEntity,
            tagIdList: List<Uuid> = emptyList(),
        ): DDayLocalEntity {
            transaction.upsert(
                accountId = accountId,
                dday = dday,
                ddayTagList =
                    tagIdList.map { tagId ->
                        EntityTagLocalEntity(entityId = dday.id, tagId = tagId, isDeleted = false, updatedAt = dday.updatedAt, createdAt = dday.createdAt)
                    },
            )
            AccountDDaySyncTransactionImpl(database = database).clearPending(accountId = accountId, ddayList = syncDataSource.readPendingList(accountId = accountId))
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            return dday
        }

        suspend fun find(
            accountId: Uuid,
            ddayId: Uuid,
        ): DDayLocalEntity? = AccountDDayLocalDataSourceImpl(database = database).find(accountId = accountId, ddayId = ddayId).first()

        suspend fun storedLinkTagIdList(ddayId: Uuid): List<Uuid> =
            database
                .ddayTagTable()
                .findByEntityIdList(listOf(ddayId))
                .filterNot { link -> link.isDeleted }
                .map { link -> link.tagId }

        suspend fun linkedTagIdList(
            accountId: Uuid,
            ddayId: Uuid,
        ): List<Uuid> =
            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.DDAY, entityId = ddayId)
                .first()
                .map { tag -> tag.id }

        suspend fun unlink(
            database: DiaryDatabase,
            accountId: Uuid,
            ddayId: Uuid,
            tagId: Uuid,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ) {
            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.DDAY,
                entityId = ddayId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = updatedAt,
            )
        }

        suspend fun isPending(
            accountId: Uuid,
            ddayId: Uuid,
        ): Boolean = syncDataSource.readPendingList(accountId = accountId).any { dday -> dday.id == ddayId }

        listOf(
            "대표 태그가 없으면" to false,
            "대표 태그가 있으면" to true,
        ).forEach { (label, hasPrimaryTag) ->
            test("TC-9bbc126e-567d-4127-bf53-5e3fa0866223 $label 저장한 대표 태그가 그대로 조회된다") {
                val accountId = Arb.kotlinUuid().next()
                val primaryTagId = if (hasPrimaryTag) Arb.kotlinUuid().next() else null
                val dday = Arb.ddayLocalEntity().next().copy(primaryTagId = primaryTagId)

                transaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())

                find(accountId = accountId, ddayId = dday.id)?.primaryTagId shouldBe primaryTagId
            }
        }

        test("TC-53f5d5aa-6030-49f9-b2bb-007ee763c988 대표 태그가 기기에 없어도 디데이가 저장되고 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val unknownTagId = Arb.kotlinUuid().next()
            val dday = Arb.ddayLocalEntity().next().copy(primaryTagId = unknownTagId)

            transaction.upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())

            database.tagDao().findUpdatedAt(listOf(unknownTagId)).shouldBeEmpty()
            find(accountId = accountId, ddayId = dday.id) shouldBe dday
        }

        test("TC-72fa9eb5-2247-4221-81d4-f5a4b9e51b58 대표 태그를 다시 지정하면 이전 대표 태그를 대체한다") {
            val accountId = Arb.kotlinUuid().next()
            val previousTagId = Arb.kotlinUuid().next()
            val newTagId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = previousTagId), tagIdList = listOf(previousTagId))

            transaction.updatePrimaryTagId(accountId = accountId, ddayId = dday.id, primaryTagId = newTagId, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            find(accountId = accountId, ddayId = dday.id).shouldNotBeNull().primaryTagId.let { primaryTagId ->
                primaryTagId shouldBe newTagId
                primaryTagId shouldNotBe previousTagId
            }
        }

        test("TC-8f14801a-aca6-4d7f-a364-db3f26520a76 태그를 삭제해도 대표 태그 지정이 유지된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = tag.id), tagIdList = listOf(tag.id))

            AccountTagTransactionImpl(database = database).updateDeleted(
                accountId = accountId,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1

            find(accountId = accountId, ddayId = dday.id)?.primaryTagId shouldBe tag.id
        }

        test("TC-977201e0-4f84-4a0f-ab67-6b049a42de83 삭제한 태그의 삭제가 다른 기기에서 받은 내용으로 풀려도 대표 태그 지정이 유지된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = true).next()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = tag.id), tagIdList = listOf(tag.id))

            AccountTagSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                tagList = listOf(tag.copy(isDeleted = false, updatedAt = tag.updatedAt + 1.seconds)),
                cursor = Arb.long().next(),
            )

            database.tagDao().findUpdatedAt(listOf(tag.id)) shouldBe mapOf(tag.id to tag.updatedAt + 1.seconds)
            find(accountId = accountId, ddayId = dday.id)?.primaryTagId shouldBe tag.id
        }

        listOf(
            "삭제해도" to false,
            "삭제 실행 취소해도" to true,
        ).forEach { (label, isDeleted) ->
            test("TC-2aac0bb9-20fb-46b0-a644-a7739f7922ba 디데이를 $label 대표 태그 지정이 유지된다") {
                val accountId = Arb.kotlinUuid().next()
                val primaryTagId = Arb.kotlinUuid().next()
                val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = primaryTagId, isDeleted = isDeleted))

                transaction.updateDeleted(accountId = accountId, ddayId = dday.id, isDeleted = !isDeleted, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

                find(accountId = accountId, ddayId = dday.id).shouldNotBeNull().let { stored ->
                    stored.isDeleted shouldBe !isDeleted
                    stored.primaryTagId shouldBe primaryTagId
                }
            }
        }

        test("TC-48e8d8c4-9c0c-4c09-a6cc-5df5f0e48740 TC-08728f13-3dcb-42f3-a3d1-e3b9940df925 정보를 수정해도 대표 태그가 바뀌지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = Arb.kotlinUuid().next()))
            val detail = Arb.ddayDetailLocalEntity().next()
            val updatedAt = Arb.secondPrecisionInstant().next()

            transaction.updateDetail(accountId = accountId, ddayId = dday.id, detail = detail, updatedAt = updatedAt) shouldBe 1

            find(accountId = accountId, ddayId = dday.id) shouldBe dday.copy(detail = detail, updatedAt = updatedAt)
        }

        test("TC-02f2c52b-1b4f-4f78-b80d-d083b2b6ceb7 삭제된 디데이의 대표 태그도 바꿀 수 있다") {
            val accountId = Arb.kotlinUuid().next()
            val newTagId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = Arb.kotlinUuid().next(), isDeleted = true))

            transaction.updatePrimaryTagId(accountId = accountId, ddayId = dday.id, primaryTagId = newTagId, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            find(accountId = accountId, ddayId = dday.id).shouldNotBeNull().let { stored ->
                stored.primaryTagId shouldBe newTagId
                stored.isDeleted shouldBe true
            }
        }

        listOf<Pair<String, (Uuid) -> Uuid?>>(
            "대표 태그를 다른 태그로 지정하면" to { newTagId -> newTagId },
            "대표 태그 지정을 해제하면" to { _ -> null },
        ).forEach { (label, nextPrimaryTagId) ->
            test("TC-1f34bedd-d25e-4188-b198-36016b363549 $label 디데이의 정보와 삭제 여부는 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val dday =
                    insertUploaded(
                        accountId = accountId,
                        dday = Arb.ddayLocalEntity().next().copy(primaryTagId = Arb.kotlinUuid().next(), isDeleted = Arb.boolean().next()),
                    )

                transaction.updatePrimaryTagId(
                    accountId = accountId,
                    ddayId = dday.id,
                    primaryTagId = nextPrimaryTagId(Arb.kotlinUuid().next()),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                ) shouldBe 1

                find(accountId = accountId, ddayId = dday.id).shouldNotBeNull().let { stored ->
                    stored.detail shouldBe dday.detail
                    stored.isDeleted shouldBe dday.isDeleted
                }
            }
        }

        listOf<Triple<String, suspend (Uuid, Uuid, Uuid, Instant) -> Unit, Boolean>>(
            Triple("대표 태그를 다른 태그로 바꾸면", { accountId, ddayId, _, updatedAt ->
                transaction.updatePrimaryTagId(accountId = accountId, ddayId = ddayId, primaryTagId = Arb.kotlinUuid().next(), updatedAt = updatedAt)
            }, true),
            Triple("대표 태그 지정을 해제하면", { accountId, ddayId, _, updatedAt ->
                transaction.updatePrimaryTagId(accountId = accountId, ddayId = ddayId, primaryTagId = null, updatedAt = updatedAt)
            }, true),
            Triple("대표 태그의 연결을 해제하면", { accountId, ddayId, tagId, updatedAt ->
                unlink(database = database, accountId = accountId, ddayId = ddayId, tagId = tagId, updatedAt = updatedAt)
            }, true),
            Triple("대표 태그를 처음 지정하면", { accountId, ddayId, tagId, updatedAt ->
                transaction.updatePrimaryTagId(accountId = accountId, ddayId = ddayId, primaryTagId = tagId, updatedAt = updatedAt)
            }, false),
        ).forEach { (label, action, hasPrimaryTag) ->
            test("TC-6187aa14-e358-46ab-b4bd-93e2676974cb $label 수정 시각이 갱신되고 업로드 대기가 된다") {
                val accountId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val dday =
                    insertUploaded(
                        accountId = accountId,
                        dday = Arb.ddayLocalEntity().next().copy(primaryTagId = if (hasPrimaryTag) tagId else null),
                        tagIdList = listOf(tagId),
                    )
                val updatedAt = Arb.secondPrecisionInstant().next()

                action(accountId, dday.id, tagId, updatedAt)

                find(accountId = accountId, ddayId = dday.id)?.updatedAt shouldBe updatedAt
                isPending(accountId = accountId, ddayId = dday.id) shouldBe true
            }
        }

        listOf(
            "삭제되지 않은" to false,
            "삭제된" to true,
        ).forEach { (label, isDeleted) ->
            test("TC-96026bfa-e6a7-460d-b3a5-2a376c27a0fd $label 디데이의 대표 태그를 지정하면 대표 태그만 바뀐다") {
                val accountId = Arb.kotlinUuid().next()
                val newTagId = Arb.kotlinUuid().next()
                val updatedAt = Arb.secondPrecisionInstant().next()
                val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = null, isDeleted = isDeleted))

                transaction.updatePrimaryTagId(accountId = accountId, ddayId = dday.id, primaryTagId = newTagId, updatedAt = updatedAt) shouldBe 1

                find(accountId = accountId, ddayId = dday.id) shouldBe dday.copy(primaryTagId = newTagId, updatedAt = updatedAt)
            }
        }

        listOf(
            LinkPrimaryCase("연결되지 않은 태그를 대표 태그로 지정하면", isLinkedBefore = false, isLinkedAfter = true, isPrimaryAfter = true) { accountId, ddayId, tagId, updatedAt ->
                transaction.updatePrimaryTagId(accountId = accountId, ddayId = ddayId, primaryTagId = tagId, updatedAt = updatedAt)
            },
            LinkPrimaryCase("대표 태그인 태그의 연결을 해제하면", isLinkedBefore = true, isLinkedAfter = false, isPrimaryAfter = false) { accountId, ddayId, tagId, updatedAt ->
                unlink(database = database, accountId = accountId, ddayId = ddayId, tagId = tagId, updatedAt = updatedAt)
            },
            LinkPrimaryCase("대표 태그 지정을 해제하면", isLinkedBefore = true, isLinkedAfter = true, isPrimaryAfter = false) { accountId, ddayId, _, updatedAt ->
                transaction.updatePrimaryTagId(accountId = accountId, ddayId = ddayId, primaryTagId = null, updatedAt = updatedAt)
            },
        ).forEach { case ->
            test("TC-0aab5436-4878-4cee-8289-21cc89dd3c79 ${case.label} 연결과 대표 태그, 수정 시각이 함께 반영된다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                insertTag(accountId, tag)
                val dday =
                    insertUploaded(
                        accountId = accountId,
                        dday = Arb.ddayLocalEntity().next().copy(primaryTagId = if (case.isLinkedBefore) tag.id else null),
                        tagIdList = if (case.isLinkedBefore) listOf(tag.id) else emptyList(),
                    )
                val updatedAt = Arb.secondPrecisionInstant().next()

                case.action(accountId, dday.id, tag.id, updatedAt)

                linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe if (case.isLinkedAfter) listOf(tag.id) else emptyList()
                find(accountId = accountId, ddayId = dday.id).shouldNotBeNull().let { stored ->
                    stored.primaryTagId shouldBe if (case.isPrimaryAfter) tag.id else null
                    stored.updatedAt shouldBe updatedAt
                }
            }
        }

        test("TC-0298ccbb-3953-4f5c-a1ce-a184189a92fa 연결되지 않은 태그를 대표 태그로 지정하면 연결되고, 대표 태그를 해제해도 연결은 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = null))
            val setAt = Arb.secondPrecisionInstant().next()

            transaction.updatePrimaryTagId(accountId = accountId, ddayId = dday.id, primaryTagId = tag.id, updatedAt = setAt) shouldBe 1

            find(accountId = accountId, ddayId = dday.id)?.primaryTagId shouldBe tag.id
            linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)

            transaction.updatePrimaryTagId(accountId = accountId, ddayId = dday.id, primaryTagId = null, updatedAt = setAt + 1.seconds) shouldBe 1

            find(accountId = accountId, ddayId = dday.id)?.primaryTagId.shouldBeNull()
            linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)
        }

        test("TC-db01c56c-7d0c-4a24-b133-c60cc14f75ef 연결된 태그를 대표 태그로 지정하면 디데이가 업로드 대기가 된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = null), tagIdList = listOf(tag.id))

            transaction.updatePrimaryTagId(accountId = accountId, ddayId = dday.id, primaryTagId = tag.id, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1

            syncDataSource.readPendingList(accountId = accountId).map { value -> value.id to value.primaryTagId } shouldBe listOf(dday.id to tag.id)
        }

        test("TC-bbf76861-fe0e-47f7-abba-480567b0377e TC-65a5d581-ce08-4cfe-8856-4459a9918eb0 대표 태그의 연결을 해제하면 대표 태그도 해제된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, tag)
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = tag.id), tagIdList = listOf(tag.id))
            linkedTagIdList(accountId = accountId, ddayId = dday.id) shouldBe listOf(tag.id)

            unlink(database = database, accountId = accountId, ddayId = dday.id, tagId = tag.id)

            linkedTagIdList(accountId = accountId, ddayId = dday.id).shouldBeEmpty()
            find(accountId = accountId, ddayId = dday.id)?.primaryTagId.shouldBeNull()
        }

        // 추가 테스트: 대표 태그가 아닌 태그의 연결 해제는 대표 태그와 디데이를 건드리지 않는다.
        test("대표 태그가 아닌 태그의 연결을 해제하면 디데이는 바뀌지 않고 업로드 대기도 되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val primaryTagId = Arb.kotlinUuid().next()
            val otherTagId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = primaryTagId), tagIdList = listOf(primaryTagId, otherTagId))

            unlink(database = database, accountId = accountId, ddayId = dday.id, tagId = otherTagId)

            find(accountId = accountId, ddayId = dday.id) shouldBe dday
            isPending(accountId = accountId, ddayId = dday.id) shouldBe false
        }

        test("TC-7c0d8e38-54b4-40be-bff1-fa47bfc000de 연결되지 않은 태그를 대표 태그로 지정하는 저장이 실패하면 연결과 지정 모두 그대로다") {
            val accountId = Arb.kotlinUuid().next()
            val tagId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = null))
            val failingDatabase = spyk(database)
            every { failingDatabase.accountDDayDao() } throws DDayPrimaryTagTestException()

            shouldThrowExactly<DDayPrimaryTagTestException> {
                AccountDDayTransactionImpl(database = failingDatabase).updatePrimaryTagId(
                    accountId = accountId,
                    ddayId = dday.id,
                    primaryTagId = tagId,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            }

            database.ddayTagTable().findByEntityIdList(listOf(dday.id)).shouldBeEmpty()
            find(accountId = accountId, ddayId = dday.id) shouldBe dday
        }

        test("TC-7c0d8e38-54b4-40be-bff1-fa47bfc000de 대표 태그인 태그의 연결 해제 저장이 실패하면 연결과 지정 모두 그대로다") {
            val accountId = Arb.kotlinUuid().next()
            val tagId = Arb.kotlinUuid().next()
            val dday = insertUploaded(accountId = accountId, dday = Arb.ddayLocalEntity().next().copy(primaryTagId = tagId), tagIdList = listOf(tagId))
            val failingDatabase = spyk(database)
            every { failingDatabase.accountDDayDao() } throws DDayPrimaryTagTestException()

            shouldThrowExactly<DDayPrimaryTagTestException> {
                unlink(database = failingDatabase, accountId = accountId, ddayId = dday.id, tagId = tagId)
            }

            storedLinkTagIdList(ddayId = dday.id) shouldBe listOf(tagId)
            find(accountId = accountId, ddayId = dday.id) shouldBe dday
        }

        listOf(
            "대표 태그가 없으면" to false,
            "대표 태그가 있으면" to true,
        ).forEach { (label, hasRemotePrimaryTag) ->
            test("TC-2566329d-2444-42ed-847f-02e8ebadca54 내려받은 디데이에 $label 그 대표 태그가 기기에 반영된다") {
                val accountId = Arb.kotlinUuid().next()
                val remotePrimaryTagId = if (hasRemotePrimaryTag) Arb.kotlinUuid().next() else null
                val localUpdatedAt = Arb.secondPrecisionInstant().next()
                val local = Arb.ddayLocalEntity().next().copy(primaryTagId = Arb.kotlinUuid().next(), updatedAt = localUpdatedAt)
                val remote = local.copy(primaryTagId = remotePrimaryTagId, updatedAt = localUpdatedAt + 1.seconds)
                database.withWriteTransaction {
                    database.ddayDao().upsert(listOf(local))
                    database.accountDDaySyncDao().upsert(AccountDDayLocalEntity(accountId = accountId, ddayId = local.id, isDirty = false))
                }

                AccountDDaySyncTransactionImpl(database = database).upsert(accountId = accountId, ddayList = listOf(remote), cursor = 5L)

                find(accountId = accountId, ddayId = local.id)?.primaryTagId shouldBe remotePrimaryTagId
            }
        }
    })
