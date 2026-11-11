package io.github.taetae98coding.diary.core.database.impl.music.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.music.transaction.AccountMusicTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabCase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabItem
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabTc
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.loadTagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.tagEntityTabLocalDataSourceTests
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant
import kotlin.uuid.Uuid

private object MusicTagTabCase : TagTabCase {
    override val kind = EntityTagKindLocalEntity.MUSIC
    override val label = "곡"
    override val tc =
        TagTabTc(
            exposure = "TC-b69fefe3-176e-4958-bd02-f98ca8e1894b TC-1b53a403-fc5f-4cfb-a5d2-e5c71210b8bf",
            order = "",
            paging = "TC-63ba164f-e932-4898-8061-9219868d1d90",
            change = "TC-819160de-2216-461d-88f5-f687fce88bef",
            tagState = "TC-7df2fe7a-d445-449d-ae69-8fc1a903afd9",
            saveSelection = "TC-f69e90b2-3bb0-49e5-9fa6-24283453d59f",
            otherTag = "TC-0bbaa7d1-74a9-48d7-92d6-b49ed5c2e162",
        )

    override suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        val music = Arb.musicLocalEntity().next()
        AccountMusicTransactionImpl(database = database).upsert(
            accountId = accountId,
            musicList =
                listOf(
                    music.copy(
                        id = item.id,
                        detail = music.detail.copy(title = item.title),
                        isDeleted = item.isDeleted,
                        updatedAt = item.updatedAt,
                        createdAt = Arb.secondPrecisionInstant().next(),
                    ),
                ),
            musicTagList = entityTagList,
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
        AccountTagMusicLocalDataSourceImpl(database = database)
            .page(accountId = accountId, tagId = tagId, scope = scope, sort = sort)
            .loadTagTabPage(key = key, loadSize = loadSize) { music -> music.id }
}

internal class AccountTagMusicLocalDataSourceImplTest :
    FunSpec({
        include(
            tagEntityTabLocalDataSourceTests(case = MusicTagTabCase) { context ->
                test("최근 수정순을 고르면 최근에 수정한 곡부터 조회한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    val old = TagTabItem(title = "a-old", updatedAt = Instant.fromEpochMilliseconds(1_000))
                    val new = TagTabItem(title = "b-new", updatedAt = Instant.fromEpochMilliseconds(2_000))
                    context.insertTag(accountId, targetTag)
                    listOf(old, new).forEach { item ->
                        context.upsert(accountId, item)
                        context.link(accountId = accountId, entityId = item.id, tagId = targetTag.id)
                    }

                    context.idList(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.TITLE) shouldBe listOf(old.id, new.id)
                    context.idList(accountId = accountId, tagId = targetTag.id, sort = ListSortLocalEntity.RECENTLY_UPDATED) shouldBe
                        listOf(new.id, old.id)
                }
            },
        )
    })
