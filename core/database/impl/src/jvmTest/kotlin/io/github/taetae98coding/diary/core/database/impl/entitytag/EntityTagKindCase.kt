package io.github.taetae98coding.diary.core.database.impl.entitytag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.music.transaction.AccountMusicTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import kotlin.uuid.Uuid

// 여섯 종류를 같은 시나리오로 확인하도록 종류마다 항목 저장, 테이블 이름, 테스트 케이스 ID를 묶는다.
internal class EntityTagKindCase(
    val kind: EntityTagKindLocalEntity,
    val label: String,
    val entityTable: String,
    val accountEntityTable: String,
    val linkTable: String,
    val accountLinkTable: String,
    val failAccountLinkDao: (DiaryDatabase) -> Unit,
    private val tcMap: Map<EntityTagTc, String>,
    private val upsertEntity: suspend (database: DiaryDatabase, accountId: Uuid, entityId: Uuid, entityTagList: List<EntityTagLocalEntity>) -> Unit,
    private val updateDeletedEntity: suspend (database: DiaryDatabase, accountId: Uuid, entityId: Uuid, isDeleted: Boolean) -> Unit,
    private val updateDetailEntity: suspend (database: DiaryDatabase, accountId: Uuid, entityId: Uuid) -> Unit,
) {
    fun tc(key: EntityTagTc): String = tcMap[key]?.let { id -> "$id " }.orEmpty()

    suspend fun insertEntity(
        database: DiaryDatabase,
        accountId: Uuid,
        entityId: Uuid = Uuid.random(),
        entityTagList: List<EntityTagLocalEntity> = emptyList(),
    ): Uuid {
        upsertEntity(database, accountId, entityId, entityTagList)
        return entityId
    }

    suspend fun updateDeleted(
        database: DiaryDatabase,
        accountId: Uuid,
        entityId: Uuid,
        isDeleted: Boolean,
    ) {
        updateDeletedEntity(database, accountId, entityId, isDeleted)
    }

    suspend fun updateDetail(
        database: DiaryDatabase,
        accountId: Uuid,
        entityId: Uuid,
    ) {
        updateDetailEntity(database, accountId, entityId)
    }

    override fun toString(): String = label
}

internal enum class EntityTagTc {
    MULTI_TAG,
    MULTI_ENTITY,
    NO_DUPLICATE,
    NO_TAG,
    OTHER_ACCOUNT_TAG,
    OTHER_ACCOUNT_ENTITY,
    UNLINK_RESTORE,
    DELETED_FINISHED_TAG,
    TITLE_ORDER,
    MISSING_TAG,
    TAG_STATE_KEEPS_LINK,
    ENTITY_DELETE_KEEPS_LINK,
    ENTITY_DETAIL_KEEPS_LINK,
    ADD_WITH_TAG,
    ADD_FAILURE,
    OTHER_LINK_INDEPENDENT,
    PENDING_PER_ACCOUNT,
    PULLED_APPLIED,
    MISSING_ENTITY,
}

private fun tcMap(vararg idList: String): Map<EntityTagTc, String> = EntityTagTc.entries.zip(idList).toMap()

internal val entityTagKindCaseList: List<EntityTagKindCase> =
    listOf(
        EntityTagKindCase(
            kind = EntityTagKindLocalEntity.WEB,
            label = "웹 항목",
            entityTable = "web",
            accountEntityTable = "account_web",
            linkTable = "web_tag",
            accountLinkTable = "account_web_tag",
            failAccountLinkDao = { database -> every { database.accountWebTagDao() } throws IllegalStateException("save failed") },
            tcMap = emptyMap(),
            upsertEntity = { database, accountId, entityId, entityTagList ->
                val entity = Arb.webLocalEntity().next().copy(id = entityId, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                AccountWebTransactionImpl(database = database).upsert(accountId = accountId, webList = listOf(entity), webTagList = entityTagList)
            },
            updateDeletedEntity = { database, accountId, entityId, isDeleted ->
                AccountWebTransactionImpl(database = database).updateDeleted(accountId = accountId, webId = entityId, isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next())
            },
            updateDetailEntity = { database, accountId, entityId ->
                AccountWebTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    webId = entityId,
                    detail = Arb.webDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
        ),
        EntityTagKindCase(
            kind = EntityTagKindLocalEntity.PLACE,
            label = "장소",
            entityTable = "place",
            accountEntityTable = "account_place",
            linkTable = "place_tag",
            accountLinkTable = "account_place_tag",
            failAccountLinkDao = { database -> every { database.accountPlaceTagDao() } throws IllegalStateException("save failed") },
            tcMap = emptyMap(),
            upsertEntity = { database, accountId, entityId, entityTagList ->
                val entity = Arb.placeLocalEntity().next().copy(id = entityId, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                AccountPlaceTransactionImpl(database = database).upsert(accountId = accountId, placeList = listOf(entity), placeTagList = entityTagList)
            },
            updateDeletedEntity = { database, accountId, entityId, isDeleted ->
                AccountPlaceTransactionImpl(database = database).updateDeleted(
                    accountId = accountId,
                    placeId = entityId,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
            updateDetailEntity = { database, accountId, entityId ->
                AccountPlaceTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    placeId = entityId,
                    detail = Arb.placeDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
        ),
        EntityTagKindCase(
            kind = EntityTagKindLocalEntity.CONTACT,
            label = "연락처",
            entityTable = "contact",
            accountEntityTable = "account_contact",
            linkTable = "contact_tag",
            accountLinkTable = "account_contact_tag",
            failAccountLinkDao = { database -> every { database.accountContactTagDao() } throws IllegalStateException("save failed") },
            tcMap =
                tcMap(
                    "TC-7f6c44b9-1420-40bd-9fdc-57039b498d7d",
                    "TC-746bb976-0abb-4319-b9ca-30bd93059085",
                    "TC-85553a4d-a974-43ba-98b8-92da81cb6943",
                    "TC-4a088660-6162-4081-89b9-dc060efd6326",
                    "TC-6d94aaff-2f3b-4551-89dc-13111457edae",
                    "TC-e9cf3566-0dbe-4f1a-b9f0-bd82e143c06d",
                    "TC-53ae5a58-14d2-4f12-9ca3-8eebe89ab6a9",
                    "TC-9b2b6637-6842-41ff-b821-8273d32722ef",
                    "TC-27e308e2-1ca4-441a-9a0d-e8e142c9d5d3",
                    "TC-dba4c661-2726-4c95-b882-bcaaea7e8f9f",
                    "TC-8cd90813-6789-4675-8847-0df0e124d9ef",
                    "TC-0f081b32-3833-49bd-8ea6-6dbc9031ffee",
                    "TC-32a528e2-8af0-4200-93d5-33dc40477de3",
                    "TC-ce9f762e-bfe4-4400-8d48-5421eecf6c8f",
                    "TC-2af9ab54-37e2-4ce1-ad10-fc82095707d2",
                    "TC-133c1a69-e9f3-449d-8ba4-91ed3483ed77",
                    "TC-eb04c18b-de8c-4708-bb59-b5886c52d8e8",
                    "TC-bf88c8cb-9494-4bf7-a207-bc4d48a1fdb7",
                    "TC-506ded54-c910-4e6e-bfe0-fb856dcc22a5",
                ),
            upsertEntity = { database, accountId, entityId, entityTagList ->
                val entity = Arb.contactLocalEntity().next().copy(id = entityId, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                AccountContactTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    contactList = listOf(entity),
                    contactTagList = entityTagList,
                )
            },
            updateDeletedEntity = { database, accountId, entityId, isDeleted ->
                AccountContactTransactionImpl(database = database).updateDeleted(
                    accountId = accountId,
                    contactId = entityId,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
            updateDetailEntity = { database, accountId, entityId ->
                AccountContactTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    contactId = entityId,
                    detail = Arb.contactDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
        ),
        EntityTagKindCase(
            kind = EntityTagKindLocalEntity.CHECKLIST,
            label = "체크리스트",
            entityTable = "checklist",
            accountEntityTable = "account_checklist",
            linkTable = "checklist_tag",
            accountLinkTable = "account_checklist_tag",
            failAccountLinkDao = { database -> every { database.accountChecklistTagDao() } throws IllegalStateException("save failed") },
            tcMap =
                tcMap(
                    "TC-e6c08b62-d5cc-48b8-bf1b-3ddb529bf7fe",
                    "TC-af5c27ed-d555-45a5-b36c-4424d1442915",
                    "TC-5873c69b-af15-4ed2-a9b8-e3b244002362",
                    "TC-57541509-3bf1-4a3c-afa9-b28db22c93b5",
                    "TC-b55af264-d890-4a61-a31b-043b70815e05",
                    "TC-90b939db-28a4-4903-b25e-a151e4951125",
                    "TC-c78affc9-cb70-4ddd-a9db-c9dee7f33e4f",
                    "TC-559b06b4-4fe0-43c2-a58e-c7b3cfe0626b",
                    "TC-d64ceb6e-9e7d-4b92-8df2-b556f9ec210a",
                    "TC-fb87cb2c-a5cc-4e3e-9d0d-30dc2bf11798",
                    "TC-fa60afce-223e-45e2-8fde-cae0a2dc4416",
                    "TC-64e303b7-c173-4a54-b0d4-6698a7e6f477",
                    "TC-4ca6ba75-8126-4dc8-9df3-463625ac1f51",
                    "TC-a388b486-45c1-4ad0-bec2-ea1d46d75d3b",
                    "TC-61cb986a-c91e-4636-bff6-f80ce874b05f",
                    "TC-112a3302-04d6-4cbb-abdb-5af96acaa416",
                    "TC-bb4c290d-33fe-49c7-88a3-56f227a8bede",
                    "TC-a6fd5fa8-6296-445d-b8ac-7d06615f38db",
                    "TC-ca867d16-ea64-4aee-b234-57807d08df2d",
                ),
            upsertEntity = { database, accountId, entityId, entityTagList ->
                val entity = Arb.checklistLocalEntity().next().copy(id = entityId, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                AccountChecklistTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    checklist = entity,
                    itemList = emptyList(),
                    checklistTagList = entityTagList,
                )
            },
            updateDeletedEntity = { database, accountId, entityId, isDeleted ->
                AccountChecklistTransactionImpl(database = database).updateDeleted(
                    accountId = accountId,
                    checklistId = entityId,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
            updateDetailEntity = { database, accountId, entityId ->
                AccountChecklistTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    checklistId = entityId,
                    detail = Arb.checklistDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
        ),
        EntityTagKindCase(
            kind = EntityTagKindLocalEntity.MUSIC,
            label = "곡",
            entityTable = "music",
            accountEntityTable = "account_music",
            linkTable = "music_tag",
            accountLinkTable = "account_music_tag",
            failAccountLinkDao = { database -> every { database.accountMusicTagDao() } throws IllegalStateException("save failed") },
            tcMap =
                tcMap(
                    "TC-5f579e29-1b7d-426d-ae70-cbb8b9fcb481",
                    "TC-08c051d3-ffb7-435e-96ab-57bdb2d568ac",
                    "TC-8a99efbe-8373-47c8-8014-268216d09e10",
                    "TC-e7a9c687-7ca6-4912-9cc3-e2f14126618d",
                    "TC-280295fb-1dd8-485f-bc16-b42a7d37f8ca",
                    "TC-c1071524-e599-4dcd-bf45-297d9754b081",
                    "TC-2fc7a25c-6d14-40ec-a22f-c130ec4af82f",
                    "TC-4882dee7-46c5-4621-b6af-5da4cd5a4d2e",
                    "TC-92ad3a86-0168-4e77-815e-6749e160b070",
                    "TC-114faf05-d8ce-4ed9-aea6-54a2231bee23",
                    "TC-66f20a47-84c9-40ee-85c3-bf1c2339bb60",
                    "TC-44ea7304-ce0d-4eb6-9170-3d2744f9b12f",
                    "TC-9401f3e5-f64f-46bf-94ea-e2f393f10c65",
                    "TC-9e2caabc-7aa9-4a87-9a2e-56b004a54b92",
                    "TC-8033a203-67a3-4e1f-8e23-6c0ab2e4dfe5",
                    "TC-7fb81d1c-7ead-4617-b6ea-609fb440ed00",
                    "TC-f0bfbf65-533d-46a9-8cef-2c6c0ad4439f",
                    "TC-08270649-c0fd-4fa9-beff-db394a7c6f30",
                    "TC-916b67b2-d32c-482a-b6ce-35a143dc6ab7",
                ),
            upsertEntity = { database, accountId, entityId, entityTagList ->
                val entity = Arb.musicLocalEntity().next().copy(id = entityId, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                AccountMusicTransactionImpl(database = database).upsert(accountId = accountId, musicList = listOf(entity), musicTagList = entityTagList)
            },
            updateDeletedEntity = { database, accountId, entityId, isDeleted ->
                AccountMusicTransactionImpl(database = database).updateDeleted(
                    accountId = accountId,
                    musicId = entityId,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
            updateDetailEntity = { database, accountId, entityId ->
                AccountMusicTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    musicId = entityId,
                    detail = Arb.musicDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
        ),
        EntityTagKindCase(
            kind = EntityTagKindLocalEntity.QR,
            label = "QR",
            entityTable = "qr",
            accountEntityTable = "account_qr",
            linkTable = "qr_tag",
            accountLinkTable = "account_qr_tag",
            failAccountLinkDao = { database -> every { database.accountQrTagDao() } throws IllegalStateException("save failed") },
            tcMap =
                tcMap(
                    "TC-05690781-039c-44d0-ab0a-ae300b6860cc",
                    "TC-45b0d6ea-422f-4abf-82cd-83bdd562219a",
                    "TC-61e857b0-4f26-4a6e-8745-4cc00af83e99",
                    "TC-7bfb8186-7ef4-4d8e-85ca-0d9e2afcc4f6",
                    "TC-115c1666-890d-4a93-a3d8-8be55e5ebce7",
                    "TC-aefaa617-cb30-4e7f-9d74-6e905d652cba",
                    "TC-575d6b08-b05c-421f-94b1-83a5195114c2",
                    "TC-6d131b42-f9a1-4746-ac43-355b58e245db",
                    "TC-1e4a39ac-0082-48a4-b07c-60031d3e426b",
                    "TC-a1f9ee1a-45a4-47be-a78f-d94b586e2fea",
                    "TC-6ffe18a3-dbbc-4d06-8075-1d9c85631517",
                    "TC-51fafbd8-bca4-48c3-bb39-66ad02e6d9cb",
                    "TC-1e8c1ca6-ba94-4ff1-9631-b7bebf5fd407",
                    "TC-0b0983c1-4687-4d02-8d8a-fc02cbdf5e0b",
                    "TC-6bd39f8a-67bf-4097-97b1-9206cce0a313",
                    "TC-e0ca3090-8db3-45ae-b58f-bda8932944cd",
                    "TC-76368813-5e6e-45ce-ac4a-6db878d4eceb",
                    "TC-e6e6a0ab-99b7-446a-b6d5-0d03d0372fe1",
                    "TC-dd1380a1-edf9-410e-9919-23cc15f842b4",
                ),
            upsertEntity = { database, accountId, entityId, entityTagList ->
                val entity = Arb.qrLocalEntity().next().copy(id = entityId, isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(entity), qrTagList = entityTagList)
            },
            updateDeletedEntity = { database, accountId, entityId, isDeleted ->
                AccountQrTransactionImpl(database = database).updateDeleted(accountId = accountId, qrId = entityId, isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next())
            },
            updateDetailEntity = { database, accountId, entityId ->
                AccountQrTransactionImpl(database = database).updateDetail(
                    accountId = accountId,
                    qrId = entityId,
                    detail = Arb.qrDetailLocalEntity().next(),
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )
            },
        ),
    )
