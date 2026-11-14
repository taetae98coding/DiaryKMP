package io.github.taetae98coding.diary.core.database.impl.entitytag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagKindCase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTc
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagKindCaseList
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.spyk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class AccountEntityTagLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountEntityTagLocalDataSourceImpl
        lateinit var syncDataSource: AccountEntityTagSyncLocalDataSourceImpl
        lateinit var transaction: AccountEntityTagTransactionImpl
        lateinit var syncTransaction: AccountEntityTagSyncTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var tagSyncTransaction: AccountTagSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountEntityTagLocalDataSourceImpl(database = database)
            syncDataSource = AccountEntityTagSyncLocalDataSourceImpl(database = database)
            transaction = AccountEntityTagTransactionImpl(database = database)
            syncTransaction = AccountEntityTagSyncTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            tagSyncTransaction = AccountTagSyncTransactionImpl(database = database)
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

        suspend fun EntityTagKindCase.link(
            accountId: Uuid,
            entityId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            transaction.upsert(
                accountId = accountId,
                kind = kind,
                entityId = entityId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        suspend fun EntityTagKindCase.linkedTagIdList(
            accountId: Uuid,
            entityId: Uuid,
        ): List<Uuid> = dataSource.getTagList(accountId = accountId, kind = kind, entityId = entityId).first().map { tag -> tag.id }

        suspend fun EntityTagKindCase.selectableTagIdList(
            accountId: Uuid,
            entityId: Uuid,
            query: String = "",
        ): List<Uuid> =
            dataSource
                .pageSelectableTag(accountId = accountId, kind = kind, entityId = entityId, query = query)
                .pagedTagIdList()

        suspend fun countRow(table: String): Long =
            database.useReaderConnection { transactor ->
                transactor.usePrepared("SELECT COUNT(*) FROM $table") { statement ->
                    statement.step()
                    statement.getLong(0)
                }
            }

        entityTagKindCaseList.forEach { case ->
            context(case.label) {
                test("${case.tc(EntityTagTc.MULTI_TAG)}하나의 ${case.label}에 여러 태그를 연결할 수 있다") {
                    val accountId = Arb.kotlinUuid().next()
                    val firstTag = tag()
                    val secondTag = tag()
                    insertTag(accountId, firstTag, secondTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = firstTag.id)
                    case.link(accountId = accountId, entityId = entityId, tagId = secondTag.id)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldContainExactlyInAnyOrder
                        listOf(firstTag.id, secondTag.id)
                }

                test("${case.tc(EntityTagTc.MULTI_ENTITY)}하나의 태그를 여러 ${case.label}에 연결할 수 있다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val firstEntityId = case.insertEntity(database = database, accountId = accountId)
                    val secondEntityId = case.insertEntity(database = database, accountId = accountId)

                    case.link(accountId = accountId, entityId = firstEntityId, tagId = tag.id)
                    case.link(accountId = accountId, entityId = secondEntityId, tagId = tag.id)

                    case.linkedTagIdList(accountId = accountId, entityId = firstEntityId) shouldBe listOf(tag.id)
                    case.linkedTagIdList(accountId = accountId, entityId = secondEntityId) shouldBe listOf(tag.id)
                }

                test("${case.tc(EntityTagTc.NO_DUPLICATE)}같은 ${case.label}와 태그의 연결은 중복되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                    countRow(table = case.linkTable) shouldBe 1L
                }

                test("${case.tc(EntityTagTc.NO_TAG)}연결된 태그가 없는 ${case.label}도 조회된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                }

                test("${case.tc(EntityTagTc.OTHER_ACCOUNT_TAG)}계정과 연결되지 않은 태그는 연결된 태그와 연결할 수 있는 태그로 조회되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val otherAccountId = Arb.kotlinUuid().next()
                    val otherAccountTag = tag()
                    insertTag(otherAccountId, otherAccountTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = otherAccountTag.id)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                    case.selectableTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                }

                test("${case.tc(EntityTagTc.OTHER_ACCOUNT_ENTITY)}계정과 연결되지 않은 ${case.label}에는 태그를 연결해도 조회되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val otherAccountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = otherAccountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                }

                test("${case.tc(EntityTagTc.UNLINK_RESTORE)}연결을 해제하면 조회되지 않고 다시 만들면 복구된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id, isDeleted = true)
                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                }

                test("${case.tc(EntityTagTc.DELETED_FINISHED_TAG)}삭제된 태그는 조회되지 않고 완료된 태그는 조회된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    withClue("미완료·미삭제") {
                        case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                    }

                    tagTransaction.updateFinished(accountId = accountId, tagId = tag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
                    withClue("완료됨") {
                        case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                    }

                    val deletedAt = Arb.secondPrecisionInstant().next()
                    tagTransaction.updateDeleted(accountId = accountId, tagId = tag.id, isDeleted = true, updatedAt = deletedAt)
                    withClue("삭제됨") {
                        case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                    }

                    tagSyncTransaction.upsert(
                        accountId = accountId,
                        tagList = listOf(tag.copy(isDeleted = false, updatedAt = deletedAt + 1.seconds)),
                        cursor = Arb.long().next(),
                    )
                    withClue("삭제 후 다른 기기에서 받은 내용으로 삭제가 풀림") {
                        case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                    }
                }

                test("${case.tc(EntityTagTc.TITLE_ORDER)}연결된 태그는 제목 오름차순으로 조회된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val firstTag = tag(title = "a")
                    val secondTag = tag(title = "b")
                    val thirdTag = tag(title = "c")
                    insertTag(accountId, thirdTag, firstTag, secondTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    listOf(thirdTag, firstTag, secondTag).forEach { tag ->
                        case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    }

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(firstTag.id, secondTag.id, thirdTag.id)
                }

                test("${case.tc(EntityTagTc.MISSING_TAG)}기기에 없는 태그를 가리키는 연결은 조회되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = Arb.kotlinUuid().next())

                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                }

                test("${case.tc(EntityTagTc.TAG_STATE_KEEPS_LINK)}태그를 완료하거나 삭제해도 연결은 해제되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val changeMap: Map<String, suspend (Uuid) -> Unit> =
                        mapOf(
                            "완료" to { tagId ->
                                tagTransaction.updateFinished(accountId = accountId, tagId = tagId, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
                            },
                            "삭제" to { tagId ->
                                tagTransaction.updateDeleted(accountId = accountId, tagId = tagId, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
                            },
                        )

                    changeMap.forEach { (label, change) ->
                        val tag = tag()
                        insertTag(accountId, tag)
                        val entityId = case.insertEntity(database = database, accountId = accountId)
                        case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                        change(tag.id)

                        withClue(label) {
                            database
                                .entityTagTable(kind = case.kind)
                                .findByEntityIdList(listOf(entityId))
                                .map { entityTag -> entityTag.tagId to entityTag.isDeleted } shouldBe listOf(tag.id to false)
                        }
                    }
                }

                test("${case.tc(EntityTagTc.ENTITY_DELETE_KEEPS_LINK)}${case.label}을 삭제해도 태그 연결은 해제되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    case.updateDeleted(database = database, accountId = accountId, entityId = entityId, isDeleted = true)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                }

                test("${case.tc(EntityTagTc.ENTITY_DETAIL_KEEPS_LINK)}${case.label}의 내용 수정은 태그 연결을 바꾸지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val firstTag = tag()
                    val secondTag = tag()
                    insertTag(accountId, firstTag, secondTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    case.link(accountId = accountId, entityId = entityId, tagId = firstTag.id)
                    case.link(accountId = accountId, entityId = entityId, tagId = secondTag.id)

                    case.updateDetail(database = database, accountId = accountId, entityId = entityId)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldContainExactlyInAnyOrder
                        listOf(firstTag.id, secondTag.id)
                }

                test("${case.tc(EntityTagTc.ADD_WITH_TAG)}${case.label} 추가와 태그 연결이 하나의 저장 작업으로 반영된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val selectedTagList = List(2) { tag() }
                    val unselectedTag = tag()
                    insertTag(accountId, *selectedTagList.toTypedArray(), unselectedTag)
                    val entityId = Arb.kotlinUuid().next()

                    case.insertEntity(
                        database = database,
                        accountId = accountId,
                        entityId = entityId,
                        entityTagList = selectedTagList.map { tag -> entityTag(entityId = entityId, tagId = tag.id) },
                    )

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldContainExactlyInAnyOrder
                        selectedTagList.map { tag -> tag.id }
                    syncDataSource
                        .readPendingList(accountId = accountId, kind = case.kind)
                        .map { entityTag -> entityTag.tagId } shouldContainExactlyInAnyOrder selectedTagList.map { tag -> tag.id }
                }

                test("${case.tc(EntityTagTc.ADD_FAILURE)}저장이 실패하면 ${case.label}과 태그 연결이 모두 남지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = tag()
                    insertTag(accountId, targetTag)
                    val entityId = Arb.kotlinUuid().next()
                    val failingDatabase = spyk(database)
                    case.failAccountLinkDao(failingDatabase)

                    shouldThrow<IllegalStateException> {
                        case.insertEntity(
                            database = failingDatabase,
                            accountId = accountId,
                            entityId = entityId,
                            entityTagList = listOf(entityTag(entityId = entityId, tagId = targetTag.id)),
                        )
                    }

                    listOf(case.entityTable, case.accountEntityTable, case.linkTable, case.accountLinkTable).forEach { table ->
                        withClue(table) { countRow(table = table) shouldBe 0L }
                    }
                }

                test("${case.tc(EntityTagTc.OTHER_LINK_INDEPENDENT)}연결과 해제는 같은 ${case.label}의 다른 연결을 바꾸지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val removedTag = tag()
                    val keptTag = tag()
                    insertTag(accountId, removedTag, keptTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    case.link(accountId = accountId, entityId = entityId, tagId = removedTag.id)
                    case.link(accountId = accountId, entityId = entityId, tagId = keptTag.id)

                    case.link(accountId = accountId, entityId = entityId, tagId = removedTag.id, isDeleted = true)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(keptTag.id)
                }

                test("${case.tc(EntityTagTc.PENDING_PER_ACCOUNT)}연결의 업로드 대기 여부가 계정별로 기록된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val otherAccountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    syncDataSource
                        .readPendingList(accountId = accountId, kind = case.kind)
                        .map { entityTag -> entityTag.tagId } shouldBe listOf(tag.id)
                    syncDataSource.readPendingList(accountId = otherAccountId, kind = case.kind).shouldBeEmpty()

                    syncTransaction.clearPending(
                        accountId = accountId,
                        kind = case.kind,
                        entityTagList = syncDataSource.readPendingList(accountId = accountId, kind = case.kind),
                    )
                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id, isDeleted = true)

                    syncDataSource
                        .readPendingList(accountId = accountId, kind = case.kind)
                        .map { entityTag -> entityTag.tagId to entityTag.isDeleted } shouldBe listOf(tag.id to true)
                    syncDataSource.readPendingList(accountId = otherAccountId, kind = case.kind).shouldBeEmpty()
                }

                test("${case.tc(EntityTagTc.MISSING_ENTITY)}가리키는 ${case.label}이나 태그가 기기에 없는 연결도 저장은 성공한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    val entityId = Arb.kotlinUuid().next()

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()

                    case.insertEntity(database = database, accountId = accountId, entityId = entityId)
                    insertTag(accountId, tag)
                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                }

                test("연결할 수 있는 태그는 계정의 미완료·미삭제 태그와 이미 연결된 태그다") {
                    val accountId = Arb.kotlinUuid().next()
                    val selectableTag = tag(title = "a")
                    val linkedFinishedTag = tag(title = "b-$SEARCH_ONLY_QUERY")
                    val finishedTag = tag(title = "c").copy(isFinished = true)
                    val deletedTag = tag(title = "d").copy(isDeleted = true)
                    insertTag(accountId, selectableTag, linkedFinishedTag, finishedTag, deletedTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    case.link(accountId = accountId, entityId = entityId, tagId = linkedFinishedTag.id)
                    tagTransaction.updateFinished(accountId = accountId, tagId = linkedFinishedTag.id, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())

                    case.selectableTagIdList(accountId = accountId, entityId = entityId) shouldBe
                        listOf(selectableTag.id, linkedFinishedTag.id)
                    case.selectableTagIdList(accountId = accountId, entityId = entityId, query = SEARCH_ONLY_QUERY) shouldBe
                        listOf(linkedFinishedTag.id)
                }

                test("TC-ffbbae91-e3fd-4e4a-96e0-641a0831e4e4 새로 저장된 태그가 연결할 수 있는 태그의 정렬 위치에 나타난다") {
                    val accountId = Arb.kotlinUuid().next()
                    val firstTag = tag(title = "a")
                    val lastTag = tag(title = "e")
                    val addedTag = tag(title = "b")
                    val pulledTag = tag(title = "c")
                    insertTag(accountId, firstTag, lastTag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)

                    suspend fun assertListRefreshed(
                        expected: List<Uuid>,
                        change: suspend () -> Unit,
                    ) {
                        val pagingSource = dataSource.pageSelectableTag(accountId = accountId, kind = case.kind, entityId = entityId, query = "")
                        pagingSource.pagedTagIdList()
                        val invalidated = CompletableDeferred<Unit>()
                        pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

                        change()

                        withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
                        case.selectableTagIdList(accountId = accountId, entityId = entityId) shouldBe expected
                    }

                    case.selectableTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(firstTag.id, lastTag.id)
                    assertListRefreshed(expected = listOf(firstTag.id, addedTag.id, lastTag.id)) {
                        insertTag(accountId, addedTag)
                    }
                    assertListRefreshed(expected = listOf(firstTag.id, addedTag.id, pulledTag.id, lastTag.id)) {
                        tagSyncTransaction.upsert(accountId = accountId, tagList = listOf(pulledTag), cursor = Arb.long().next())
                    }
                }

                test("다른 종류의 연결은 이 종류의 연결된 태그로 조회되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insertEntity(database = database, accountId = accountId)
                    entityTagKindCaseList
                        .filterNot { other -> other.kind == case.kind }
                        .forEach { other -> other.link(accountId = accountId, entityId = entityId, tagId = tag.id) }

                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                    countRow(table = case.linkTable) shouldBe 0L
                }
            }
        }
    }) {
    companion object {
        private const val SEARCH_ONLY_QUERY: String = "EntityTagSearchOnly"
        private const val INVALIDATION_TIMEOUT_MILLIS: Long = 30_000L

        private suspend fun PagingSource<Int, TagLocalEntity>.pagedTagIdList(loadSize: Int = 100): List<Uuid> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>()
                .data
                .map { tag -> tag.id }

        private fun entityTag(
            entityId: Uuid,
            tagId: Uuid,
        ): EntityTagLocalEntity =
            EntityTagLocalEntity(
                entityId = entityId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )

        private fun tag(title: String = "title-${Arb.string().next()}"): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                .let { tag -> tag.copy(detail = tag.detail.copy(title = title), isFinished = false, isDeleted = false) }
    }
}
