package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagEntityTabLocalDataSourceSpec
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabCase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabItem
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabTc
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.loadTagTabPage
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

private object QrTagTabCase : TagTabCase(
    kind = EntityTagKindLocalEntity.QR,
    label = "QR",
    tc =
        TagTabTc(
            exposure = "TC-5fff354a-21c6-411a-b65b-7f197f29cc97 TC-735f667d-e3f5-4345-8d75-50bd9d75be44",
            order = "TC-f05da0f4-a05a-4132-b0f2-d5ffaf5d75dc",
            paging = "TC-af1f610d-12c2-456a-9631-5a345d9b8807",
            change = "TC-29b7441c-c239-44c3-b5f4-e8f65b031e39",
            tagState = "TC-594d6131-802d-49c5-b4c0-7bc7b637090b",
            saveSelection = "TC-e01c55e0-8cd3-4bd0-8a75-2d313a4d991e",
            otherTag = "TC-e60cad61-9194-44dc-9a62-9e41bf7edd10",
        ),
) {
    override suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        val qr = Arb.qrLocalEntity().next()
        AccountQrTransactionImpl(database = database).upsert(
            accountId = accountId,
            qrList =
                listOf(
                    qr.copy(
                        id = item.id,
                        detail = qr.detail.copy(title = item.title),
                        isDeleted = item.isDeleted,
                        updatedAt = item.updatedAt,
                        createdAt = Arb.secondPrecisionInstant().next(),
                    ),
                ),
            qrTagList = entityTagList,
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
        AccountTagQrLocalDataSourceImpl(database = database)
            .page(accountId = accountId, tagId = tagId, scope = scope)
            .loadTagTabPage(key = key, loadSize = loadSize) { qr -> qr.id }
}

internal class AccountTagQrLocalDataSourceImplTest : TagEntityTabLocalDataSourceSpec(case = QrTagTabCase)
