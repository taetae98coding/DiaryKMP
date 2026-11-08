package io.github.taetae98coding.diary.core.database.impl.entitytag

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklisttag.checklistTagTable
import io.github.taetae98coding.diary.core.database.impl.contacttag.contactTagTable
import io.github.taetae98coding.diary.core.database.impl.ddaytag.ddayTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import io.github.taetae98coding.diary.core.database.impl.musictag.musicTagTable
import io.github.taetae98coding.diary.core.database.impl.placetag.placeTagTable
import io.github.taetae98coding.diary.core.database.impl.qrtag.qrTagTable
import io.github.taetae98coding.diary.core.database.impl.webtag.webTagTable
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

// 종류별 Room 엔티티를 공통 모델로 바꿔, 공통 구현이 어느 종류의 테이블인지 모른 채 쓰게 한다.
internal class EntityTagTable<L : Any, A : Any>(
    private val linkDao: EntityTagDao<L>,
    private val accountLinkDao: AccountEntityTagDao<A>,
    private val syncDao: AccountEntityTagSyncDao<L, A>,
    private val toLink: (EntityTagLocalEntity) -> L,
    private val toEntityTag: (L) -> EntityTagLocalEntity,
    private val toAccountLink: (accountId: Uuid, entityId: Uuid, tagId: Uuid, isDirty: Boolean) -> A,
) {
    suspend fun upsert(entityTagList: List<EntityTagLocalEntity>) {
        linkDao.upsert(entityTagList.map(toLink))
    }

    suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<EntityTagLocalEntity> = linkDao.findByEntityIdList(entityIdList).map(toEntityTag)

    suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        linkDao.updateDeleted(
            entityId = entityId,
            tagId = tagId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )

    suspend fun upsertPending(
        accountId: Uuid,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        upsert(entityTagList = entityTagList)
        upsertAccountPending(accountId = accountId, entityTagList = entityTagList)
    }

    suspend fun upsertAccountPending(
        accountId: Uuid,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        accountLinkDao.upsert(
            entityTagList.map { entityTag ->
                toAccountLink(accountId, entityTag.entityId, entityTag.tagId, true)
            },
        )
    }

    suspend fun insertIgnoreAccountSynced(
        accountId: Uuid,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        syncDao.insertIgnore(
            entityTagList.map { entityTag ->
                toAccountLink(accountId, entityTag.entityId, entityTag.tagId, false)
            },
        )
    }

    suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int =
        accountLinkDao.markPending(
            accountId = accountId,
            entityId = entityId,
            tagId = tagId,
        )

    fun getTagList(
        accountId: Uuid,
        entityId: Uuid,
    ): Flow<List<TagLocalEntity>> =
        accountLinkDao.getTagList(
            accountId = accountId,
            entityId = entityId,
        )

    fun pageSelectableTag(
        accountId: Uuid,
        entityId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity> =
        accountLinkDao.pageSelectableTag(
            accountId = accountId,
            entityId = entityId,
            query = query,
        )

    suspend fun findPending(accountId: Uuid): List<EntityTagLocalEntity> = syncDao.findPending(accountId = accountId).map(toEntityTag)

    suspend fun clearPending(
        accountId: Uuid,
        entityTag: EntityTagLocalEntity,
    ): Int =
        syncDao.clearPending(
            accountId = accountId,
            entityId = entityTag.entityId,
            tagId = entityTag.tagId,
            updatedAt = entityTag.updatedAt,
        )
}

internal fun DiaryDatabase.entityTagTable(kind: EntityTagKindLocalEntity): EntityTagTable<*, *> =
    when (kind) {
        EntityTagKindLocalEntity.WEB -> webTagTable()
        EntityTagKindLocalEntity.PLACE -> placeTagTable()
        EntityTagKindLocalEntity.CONTACT -> contactTagTable()
        EntityTagKindLocalEntity.CHECKLIST -> checklistTagTable()
        EntityTagKindLocalEntity.MUSIC -> musicTagTable()
        EntityTagKindLocalEntity.QR -> qrTagTable()
        EntityTagKindLocalEntity.DDAY -> ddayTagTable()
    }
