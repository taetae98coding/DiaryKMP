package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabCase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabItem
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabTc
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.loadTagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.tagEntityTabLocalDataSourceTests
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

private object ChecklistTagTabCase : TagTabCase {
    override val kind = EntityTagKindLocalEntity.CHECKLIST
    override val label = "체크리스트"
    override val tc =
        TagTabTc(
            exposure = "TC-c2a90aea-0184-45eb-90f7-ac9326d34dfa TC-d28ab57d-cc71-4cb4-9bea-c381d6657d66",
            order = "TC-f7d0f491-98c6-4b46-a9a9-764d162c736c",
            paging = "TC-84eb6c59-fcef-4789-9479-4e6582cdaf4a",
            change = "TC-1e90a1a8-cda8-46f9-860a-a984599c5f8c",
            tagState = "TC-54a11a31-ae25-4d67-ba63-741086653084",
            saveSelection = "TC-8d10a902-1b15-4b62-883e-48f78806f104",
            otherTag = "TC-d94ab6a2-043c-4ce2-b8c6-85c6b0ad96bd",
        )

    override suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        val checklist = Arb.checklistLocalEntity().next()
        AccountChecklistTransactionImpl(database = database).upsert(
            accountId = accountId,
            checklist =
                checklist.copy(
                    id = item.id,
                    detail = checklist.detail.copy(title = item.title),
                    isDeleted = item.isDeleted,
                    updatedAt = item.updatedAt,
                    createdAt = Arb.secondPrecisionInstant().next(),
                ),
            itemList = emptyList(),
            checklistTagList = entityTagList,
        )
    }

    override suspend fun page(
        database: DiaryDatabase,
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
        key: Int?,
        loadSize: Int,
    ): TagTabPage =
        AccountTagChecklistLocalDataSourceImpl(database = database)
            .page(accountId = accountId, tagId = tagId, scope = scope)
            .loadTagTabPage(key = key, loadSize = loadSize) { summary -> summary.checklist.id }
}

internal class AccountTagChecklistLocalDataSourceImplTest :
    FunSpec({
        include(
            tagEntityTabLocalDataSourceTests(case = ChecklistTagTabCase) { context ->
                test("TC-5355fbe4-edb6-4a79-911a-c7504715b547 각 체크리스트의 진행 정도를 함께 조회한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    val checklistItem = TagTabItem()
                    context.insertTag(accountId, targetTag)
                    context.upsert(accountId, checklistItem)
                    context.link(accountId = accountId, entityId = checklistItem.id, tagId = targetTag.id)
                    val itemList =
                        listOf(true, true, false).map { isChecked ->
                            Arb.localChecklistItem(isChecked = isChecked, isDeleted = false, checklistId = checklistItem.id).next()
                        } + Arb.localChecklistItem(isChecked = true, isDeleted = true, checklistId = checklistItem.id).next()
                    context.database.checklistItemDao().upsert(itemList)
                    context.database.accountChecklistItemDao().upsert(
                        itemList.map { item ->
                            io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity(
                                accountId = accountId,
                                checklistItemId = item.id,
                                isDirty = false,
                            )
                        },
                    )

                    val summary =
                        AccountTagChecklistLocalDataSourceImpl(database = context.database)
                            .page(accountId = accountId, tagId = targetTag.id, scope = TagScopeLocalEntity.SELF)
                            .load(
                                androidx.paging.PagingSource.LoadParams
                                    .Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
                            ).let { result -> (result as androidx.paging.PagingSource.LoadResult.Page).data.single() }

                    (summary.checkedCount to summary.totalCount) shouldBe (2 to 3)
                }
            },
        )
    })
