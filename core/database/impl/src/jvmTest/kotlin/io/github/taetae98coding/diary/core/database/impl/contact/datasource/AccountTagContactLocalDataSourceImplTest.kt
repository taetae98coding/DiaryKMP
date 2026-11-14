package io.github.taetae98coding.diary.core.database.impl.contact.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabCase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabItem
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabTc
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.loadTagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.tagEntityTabLocalDataSourceTests
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant
import kotlin.uuid.Uuid

private object ContactTagTabCase : TagTabCase {
    override val kind = EntityTagKindLocalEntity.CONTACT
    override val label = "연락처"
    override val tc =
        TagTabTc(
            exposure = "TC-abaecd8e-df2a-4d8f-aa27-07fbf75f288e TC-e8ba66c1-2a33-41b6-843f-3d1021b92467",
            order = "",
            paging = "TC-5981abb2-3342-41bd-b031-b78c35dd92c4",
            change = "TC-a11a616d-5381-4360-8329-5e50b462f77a",
            tagState = "TC-e3c0fa4d-9b53-4d26-a37f-4fbdf8304577",
            saveSelection = "TC-36a54276-752b-47eb-8599-d172e6db8a1a",
            otherTag = "TC-6a3a8cbd-f292-4ba7-959b-09070c139824",
        )

    override suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        val contact = Arb.contactLocalEntity().next()
        AccountContactTransactionImpl(database = database).upsert(
            accountId = accountId,
            contactList =
                listOf(
                    contact.copy(
                        id = item.id,
                        detail = contact.detail.copy(name = item.title),
                        isFavorite = item.isFavorite,
                        isDeleted = item.isDeleted,
                        updatedAt = item.updatedAt,
                        createdAt = Arb.secondPrecisionInstant().next(),
                    ),
                ),
            contactTagList = entityTagList,
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
        AccountTagContactLocalDataSourceImpl(database = database)
            .page(accountId = accountId, tagId = tagId, scope = scope, sort = sort)
            .loadTagTabPage(key = key, loadSize = loadSize) { item -> item.contact.id }
}

internal class AccountTagContactLocalDataSourceImplTest :
    FunSpec({
        include(
            tagEntityTabLocalDataSourceTests(case = ContactTagTabCase) { context ->
                test("즐겨찾기인 연락처를 먼저 놓고 묶음 안에서 고른 정렬을 적용한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    val favoriteOld = TagTabItem(title = "b-favorite", isFavorite = true, updatedAt = Instant.fromEpochMilliseconds(1_000))
                    val favoriteNew = TagTabItem(title = "c-favorite", isFavorite = true, updatedAt = Instant.fromEpochMilliseconds(3_000))
                    val normalOld = TagTabItem(title = "a-normal", updatedAt = Instant.fromEpochMilliseconds(2_000))
                    val normalNew = TagTabItem(title = "d-normal", updatedAt = Instant.fromEpochMilliseconds(4_000))
                    context.insertTag(accountId, targetTag)
                    listOf(favoriteOld, favoriteNew, normalOld, normalNew).forEach { item ->
                        context.upsert(accountId, item)
                        context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
                    }

                    listOf(ListSortLocalEntity.DEFAULT, ListSortLocalEntity.NAME).forEach { sort ->
                        context.idList(accountId = accountId, tagId = targetTag.id, sort = sort) shouldBe
                            listOf(favoriteOld.id, favoriteNew.id, normalOld.id, normalNew.id)
                    }
                    context.idList(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe
                        listOf(favoriteNew.id, favoriteOld.id, normalNew.id, normalOld.id)
                }

                test("같은 이름의 연락처가 있으면 알게 된 경로로 구분한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    val first = TagTabItem(title = "same")
                    val second = TagTabItem(title = "same")
                    val unique = TagTabItem(title = "unique")
                    context.insertTag(accountId, targetTag)
                    listOf(first, second, unique).forEach { item ->
                        context.upsert(accountId, item)
                        context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
                    }

                    val itemList =
                        AccountTagContactLocalDataSourceImpl(database = context.database)
                            .page(accountId = accountId, tagId = targetTag.id, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                            .load(
                                androidx.paging.PagingSource.LoadParams
                                    .Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
                            ).let { result -> (result as androidx.paging.PagingSource.LoadResult.Page).data }

                    itemList.associate { item -> item.contact.id to item.nameQualifier } shouldBe
                        itemList.associate { item ->
                            item.contact.id to if (item.contact.id == unique.id) "" else item.contact.detail.howWeMet
                        }
                }
            },
        )
    })
