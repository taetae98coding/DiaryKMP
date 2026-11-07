package io.github.taetae98coding.diary.core.database.impl.tag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.factory.TestFactory
import io.kotest.core.spec.style.FunSpecTestFactoryConfiguration
import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlin.time.Instant
import kotlin.uuid.Uuid

// TagDetail의 항목 탭 조회를 종류와 무관한 시나리오로 확인하기 위해 탭의 항목 하나를 종류와 분리해 표현한다.
internal data class TagTabItem(
    val id: Uuid = Uuid.random(),
    val title: String = "title-${Arb.string().next()}",
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false,
    val updatedAt: Instant = Arb.secondPrecisionInstant().next(),
)

internal data class TagTabPage(
    val idList: List<Uuid>,
    val nextKey: Int?,
)

internal class TagTabTc(
    val exposure: String,
    val order: String,
    val paging: String,
    val change: String,
    val tagState: String,
    val saveSelection: String,
    val otherTag: String,
)

internal interface TagTabCase {
    val kind: EntityTagKindLocalEntity
    val label: String
    val tc: TagTabTc

    suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity> = emptyList(),
    )

    suspend fun page(
        database: DiaryDatabase,
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
        key: Int?,
        loadSize: Int,
    ): TagTabPage
}

internal suspend fun <T : Any> PagingSource<Int, T>.loadTagTabPage(
    key: Int?,
    loadSize: Int,
    idOf: (T) -> Uuid,
): TagTabPage {
    val page =
        load(
            if (key == null) {
                PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
            } else {
                PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
            },
        ).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()

    return TagTabPage(idList = page.data.map(idOf), nextKey = page.nextKey)
}

// 스펙 본문처럼 공통 시나리오를 한 람다에 담는 팩토리라 함수 길이 검사에서 제외한다.
@Suppress("LongMethod")
internal fun tagEntityTabLocalDataSourceTests(
    case: TagTabCase,
    body: FunSpecTestFactoryConfiguration.(TagTabContext) -> Unit = {},
): TestFactory =
    funSpec {
        val context = TagTabContext(case = case)

        beforeTest { context.open() }

        afterTest { context.close() }

        test("${case.tc.exposure} [${case.label}] 현재 계정의 대상 태그와 활성 연결을 가진 미삭제 ${case.label}만 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val targetTag = context.tag()
            val otherTag = context.tag()
            val linked = TagTabItem(title = "a-linked")
            val otherTagItem = TagTabItem(title = "b-other-tag")
            val unlinked = TagTabItem(title = "c-unlinked")
            val deleted = TagTabItem(title = "d-deleted", isDeleted = true)
            val otherAccountItem = TagTabItem(title = "e-other-account")
            context.insertTag(accountId, targetTag, otherTag)
            context.insertTag(otherAccountId, targetTag)
            listOf(linked, otherTagItem, unlinked, deleted).forEach { item -> context.upsert(accountId, item) }
            context.upsert(otherAccountId, otherAccountItem)

            context.link(accountId = accountId, entityId = linked.id, tagId = targetTag.id)
            context.link(accountId = accountId, entityId = otherTagItem.id, tagId = otherTag.id)
            context.link(accountId = accountId, entityId = unlinked.id, tagId = targetTag.id, isDeleted = true)
            context.link(accountId = accountId, entityId = deleted.id, tagId = targetTag.id)
            context.link(accountId = otherAccountId, entityId = otherAccountItem.id, tagId = targetTag.id)

            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(linked.id)
        }

        test("${case.tc.order} [${case.label}] 태그별 항목은 이름순 기본 정렬에서 제목 오름차순으로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = context.tag()
            val itemList = listOf("a", "b", "c").map { title -> TagTabItem(title = "$title-item") }
            context.insertTag(accountId, targetTag)
            itemList.reversed().forEach { item ->
                context.upsert(accountId, item)
                context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
            }

            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe itemList.map { item -> item.id }
        }

        test("${case.tc.paging} [${case.label}] 태그별 항목을 페이지 단위로 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = context.tag()
            val itemList = List(3) { index -> TagTabItem(title = "item-$index") }
            context.insertTag(accountId, targetTag)
            itemList.forEach { item ->
                context.upsert(accountId, item)
                context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
            }

            val firstPage = context.page(accountId = accountId, tagId = targetTag.id, key = null, loadSize = 2)
            firstPage.idList shouldBe itemList.take(2).map { item -> item.id }

            val secondPage = context.page(accountId = accountId, tagId = targetTag.id, key = firstPage.nextKey, loadSize = 2)
            secondPage.idList shouldBe itemList.drop(2).map { item -> item.id }
        }

        test("${case.tc.change} [${case.label}] 연결을 해제하거나 항목을 삭제하면 목록에서 사라지고 되돌리면 정렬 위치에 다시 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val targetTag = context.tag()
            val first = TagTabItem(title = "a-item")
            val target = TagTabItem(title = "b-item")
            val last = TagTabItem(title = "c-item")
            context.insertTag(accountId, targetTag)
            listOf(first, target, last).forEach { item ->
                context.upsert(accountId, item)
                context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
            }

            context.link(accountId = accountId, entityId = target.id, tagId = targetTag.id, isDeleted = true)
            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(first.id, last.id)

            context.link(accountId = accountId, entityId = target.id, tagId = targetTag.id)
            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(first.id, target.id, last.id)

            context.upsert(accountId, target.copy(isDeleted = true))
            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(first.id, last.id)

            context.upsert(accountId, target)
            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(first.id, target.id, last.id)

            context.upsert(accountId, target.copy(title = "z-item"))
            context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(first.id, last.id, target.id)
        }

        test("${case.tc.tagState} [${case.label}] 태그를 완료하거나 삭제해도 연결된 항목은 계속 조회된다") {
            listOf(true to false, false to true).forEach { (isFinished, isDeleted) ->
                val accountId = Arb.kotlinUuid().next()
                val targetTag = context.tag()
                val item = TagTabItem()
                context.insertTag(accountId, targetTag)
                context.upsert(accountId, item)
                context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
                context.insertTag(accountId, targetTag.copy(isFinished = isFinished, isDeleted = isDeleted))

                context.idList(accountId = accountId, tagId = targetTag.id) shouldBe listOf(item.id)
            }
        }

        test("${case.tc.saveSelection} [${case.label}] 항목은 저장 시점의 태그 선택으로 저장된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = context.tag()
            val tagB = context.tag()
            context.insertTag(accountId, tagA, tagB)

            listOf(
                listOf(tagA) to true,
                listOf(tagA, tagB) to true,
                listOf(tagB) to false,
            ).forEach { (selectedTagList, isShownInTagATab) ->
                val item = TagTabItem()

                context.upsert(
                    accountId = accountId,
                    item = item,
                    entityTagList =
                        selectedTagList.map { tag ->
                            EntityTagLocalEntity(
                                entityId = item.id,
                                tagId = tag.id,
                                isDeleted = false,
                                updatedAt = Arb.secondPrecisionInstant().next(),
                                createdAt = Arb.secondPrecisionInstant().next(),
                            )
                        },
                )

                context.idList(accountId = accountId, tagId = tagA.id).contains(item.id) shouldBe isShownInTagATab
                selectedTagList.forEach { tag ->
                    context.idList(accountId = accountId, tagId = tag.id).contains(item.id) shouldBe true
                }
            }
        }

        test("${case.tc.otherTag} [${case.label}] 태그마다 조회되는 항목이 다르다") {
            val accountId = Arb.kotlinUuid().next()
            val firstTag = context.tag()
            val secondTag = context.tag()
            val firstItem = TagTabItem()
            val secondItem = TagTabItem()
            context.insertTag(accountId, firstTag, secondTag)
            context.upsert(accountId, firstItem)
            context.upsert(accountId, secondItem)
            context.link(accountId = accountId, entityId = firstItem.id, tagId = firstTag.id)
            context.link(accountId = accountId, entityId = secondItem.id, tagId = secondTag.id)

            context.idList(accountId = accountId, tagId = firstTag.id) shouldBe listOf(firstItem.id)
            context.idList(accountId = accountId, tagId = secondTag.id) shouldBe listOf(secondItem.id)
        }

        test("TC-5a247ffc-c974-4880-9f26-ce05e62af7e0 [${case.label}] 표시 범위에 따라 ${case.label} 목록에 담기는 태그가 달라진다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = context.tag()
            val childTag = context.tag()
            val grandChildTag = context.tag()
            context.insertTag(accountId, rootTag, childTag, grandChildTag)
            context.tagLink(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)
            context.tagLink(accountId = accountId, fromTagId = childTag.id, toTagId = grandChildTag.id)
            val itemList =
                listOf(rootTag, childTag, grandChildTag).map { tag ->
                    TagTabItem().also { item ->
                        context.upsert(accountId, item)
                        context.link(accountId = accountId, entityId = item.id, tagId = tag.id)
                    }
                }

            listOf(
                TagScopeLocalEntity.SELF to 1,
                TagScopeLocalEntity.CHILD to 2,
                TagScopeLocalEntity.DESCENDANT to 3,
            ).forEach { (scope, count) ->
                context.idList(accountId = accountId, tagId = rootTag.id, scope = scope) shouldContainExactlyInAnyOrder
                    itemList.take(count).map { item -> item.id }
            }
        }

        test("TC-f22e32bd-afa2-4ac2-8ee6-2134fed88626 [${case.label}] 범위 안 여러 태그와 연결된 항목도 목록에 한 번만 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = context.tag()
            val childTag = context.tag()
            context.insertTag(accountId, rootTag, childTag)
            context.tagLink(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)
            val item = TagTabItem()
            context.upsert(accountId, item)
            context.link(accountId = accountId, entityId = item.id, tagId = rootTag.id)
            context.link(accountId = accountId, entityId = item.id, tagId = childTag.id)

            context.idList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.CHILD) shouldBe listOf(item.id)
        }

        test("TC-11a748b2-3712-4bb6-b382-9f0c78449aab [${case.label}] 표시 범위를 넓혀도 삭제된 항목은 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val rootTag = context.tag()
            val childTag = context.tag()
            context.insertTag(accountId, rootTag, childTag)
            context.tagLink(accountId = accountId, fromTagId = rootTag.id, toTagId = childTag.id)
            val item = TagTabItem(isDeleted = true)
            context.upsert(accountId, item)
            context.link(accountId = accountId, entityId = item.id, tagId = childTag.id)

            context.idList(accountId = accountId, tagId = rootTag.id, scope = TagScopeLocalEntity.DESCENDANT).shouldBeEmpty()
        }

        body(context)
    }

internal class TagTabContext(
    val case: TagTabCase,
) {
    lateinit var database: DiaryDatabase

    fun open() {
        database =
            Room
                .inMemoryDatabaseBuilder<DiaryDatabase>()
                .setDriver(BundledSQLiteDriver())
                .build()
    }

    fun close() {
        database.close()
    }

    fun tag(title: String = "title-${Arb.string().next()}"): TagLocalEntity =
        Arb
            .tagLocalEntity()
            .next()
            .copy(updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
            .let { tag -> tag.copy(detail = tag.detail.copy(title = title), isFinished = false, isDeleted = false) }

    suspend fun insertTag(
        accountId: Uuid,
        vararg tagList: TagLocalEntity,
    ) {
        AccountTagTransactionImpl(database = database).upsert(accountId = accountId, tagList = tagList.toList(), tagLinkList = emptyList())
    }

    suspend fun tagLink(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
    ) {
        AccountTagLinkTransactionImpl(database = database).upsert(
            accountId = accountId,
            fromTagId = fromTagId,
            toTagId = toTagId,
            isDeleted = false,
            updatedAt = Arb.secondPrecisionInstant().next(),
        )
    }

    suspend fun upsert(
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity> = emptyList(),
    ) {
        case.upsert(database = database, accountId = accountId, item = item, entityTagList = entityTagList)
    }

    suspend fun link(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean = false,
    ) {
        AccountEntityTagTransactionImpl(database = database).upsert(
            accountId = accountId,
            kind = case.kind,
            entityId = entityId,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
        )
    }

    suspend fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity = TagScopeLocalEntity.SELF,
        sort: ListSortLocalEntity = ListSortLocalEntity.DEFAULT,
        key: Int? = null,
        loadSize: Int = 100,
    ): TagTabPage =
        case.page(
            database = database,
            accountId = accountId,
            tagId = tagId,
            scope = scope,
            sort = sort,
            key = key,
            loadSize = loadSize,
        )

    suspend fun idList(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity = TagScopeLocalEntity.SELF,
        sort: ListSortLocalEntity = ListSortLocalEntity.DEFAULT,
    ): List<Uuid> = page(accountId = accountId, tagId = tagId, scope = scope, sort = sort).idList
}
