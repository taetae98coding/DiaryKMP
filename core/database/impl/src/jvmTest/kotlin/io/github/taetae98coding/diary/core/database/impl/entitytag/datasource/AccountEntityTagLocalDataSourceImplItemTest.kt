package io.github.taetae98coding.diary.core.database.impl.entitytag.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.datasource.AccountCalendarFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.datasource.AccountChecklistLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.datasource.AccountChecklistSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.datasource.AccountTagChecklistLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource.AccountChecklistItemLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountCalendarContactBirthdayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountContactLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountContactSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountTagContactLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountDDaySyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.datasource.AccountTagDDayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDaySyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountTagMemoLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memocontact.datasource.AccountMemoContactLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memocontact.transaction.AccountMemoContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountMusicLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountMusicSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.datasource.AccountTagMusicLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.music.transaction.AccountMusicSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.music.transaction.AccountMusicTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.datasource.AccountQrLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.qr.datasource.AccountQrSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.qr.datasource.AccountTagQrLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

// 연락처·체크리스트·곡·QR·디데이가 저마다 다른 목록, 태그 탭, 업로드 대기 조회를 쓰므로 종류마다 그 조회를 묶어 같은 시나리오로 확인한다.
class AccountEntityTagLocalDataSourceImplItemTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountEntityTagLocalDataSourceImpl
        lateinit var transaction: AccountEntityTagTransactionImpl
        lateinit var syncDataSource: AccountEntityTagSyncLocalDataSourceImpl
        lateinit var tagTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountEntityTagLocalDataSourceImpl(database = database)
            transaction = AccountEntityTagTransactionImpl(database = database)
            syncDataSource = AccountEntityTagSyncLocalDataSourceImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
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

        suspend fun findTag(
            accountId: Uuid,
            tagId: Uuid,
        ): TagLocalEntity? = database.accountTagDao().find(accountId = accountId, tagId = tagId).first()

        suspend fun ItemKind.link(
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

        suspend fun ItemKind.linkedTagIdList(
            accountId: Uuid,
            entityId: Uuid,
        ): List<Uuid> = dataSource.getTagList(accountId = accountId, kind = kind, entityId = entityId).first().map { tag -> tag.id }

        itemKindList.forEach { case ->
            context(case.label) {
                test("${case.tc(ItemTc.CONTENT)}연결과 해제는 내용과 수정 시각을 바꾸지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insert(database, accountId, false)
                    val before = case.snapshot(database, accountId, entityId).shouldNotBeNull()
                    val beforeTag = findTag(accountId = accountId, tagId = tag.id).shouldNotBeNull()

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.snapshot(database, accountId, entityId) shouldBe before
                    findTag(accountId = accountId, tagId = tag.id) shouldBe beforeTag

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id, isDeleted = true)
                    case.snapshot(database, accountId, entityId) shouldBe before
                    findTag(accountId = accountId, tagId = tag.id) shouldBe beforeTag
                }

                test("${case.tc(ItemTc.DELETED_ENTITY)}삭제 상태에서도 태그 연결을 바꿀 수 있다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insert(database, accountId, true)
                    val before = case.snapshot(database, accountId, entityId).shouldNotBeNull()

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(tag.id)
                    case.snapshot(database, accountId, entityId) shouldBe before

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id, isDeleted = true)
                    case.linkedTagIdList(accountId = accountId, entityId = entityId).shouldBeEmpty()
                    case.snapshot(database, accountId, entityId) shouldBe before
                }

                test("${case.tc(ItemTc.ENTITY_NOT_PENDING)}태그 연결의 저장은 항목을 업로드 대기로 만들지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insert(database, accountId, false)
                    case.clearEntityPending(database, accountId)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.pendingEntityIdList(database, accountId).shouldBeEmpty()
                    syncDataSource
                        .readPendingList(accountId = accountId, kind = case.kind)
                        .map { entityTag -> entityTag.tagId to entityTag.isDeleted } shouldBe listOf(tag.id to false)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id, isDeleted = true)
                    case.pendingEntityIdList(database, accountId).shouldBeEmpty()
                    syncDataSource
                        .readPendingList(accountId = accountId, kind = case.kind)
                        .map { entityTag -> entityTag.tagId to entityTag.isDeleted } shouldBe listOf(tag.id to true)
                }

                test("${case.tc(ItemTc.TAG_TAB)}태그 연결 결과가 그 태그의 탭에 반영된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insert(database, accountId, false)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)
                    case.tagTabIdList(database, accountId, tag.id) shouldBe listOf(entityId)

                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id, isDeleted = true)
                    case.tagTabIdList(database, accountId, tag.id).shouldBeEmpty()
                }

                test("${case.tc(ItemTc.HOME_LIST)}태그 연결은 목록의 노출과 순서를 바꾸지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityIdList = List(3) { case.insert(database, accountId, false) }
                    val before = case.homeIdLists(database, accountId)
                    before.first() shouldContainExactlyInAnyOrder entityIdList

                    case.link(accountId = accountId, entityId = entityIdList[1], tagId = tag.id)

                    case.homeIdLists(database, accountId) shouldBe before
                }

                test("${case.tc(ItemTc.TAG_LINK_NOT_FOLLOWED)}태그와 태그의 연결을 따라 조회되지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val fromTag = tag()
                    val toTag = tag()
                    insertTag(accountId, fromTag, toTag)
                    val entityId = case.insert(database, accountId, false)
                    case.link(accountId = accountId, entityId = entityId, tagId = toTag.id)
                    val before = case.tagTabIdList(database, accountId, toTag.id)

                    database.accountTagLinkDao().upsert(
                        AccountTagLinkLocalEntity(
                            accountId = accountId,
                            fromTagId = fromTag.id,
                            toTagId = toTag.id,
                            isDirty = true,
                        ),
                    )

                    case.tagTabIdList(database, accountId, fromTag.id).shouldBeEmpty()
                    case.tagTabIdList(database, accountId, toTag.id) shouldBe before
                    before shouldBe listOf(entityId)
                }

                test("${case.tc(ItemTc.NO_OTHER_LINK)}태그를 연결해도 다른 종류의 연결은 만들어지지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val firstTag = tag()
                    val secondTag = tag()
                    insertTag(accountId, firstTag, secondTag)
                    val entityId = case.insert(database, accountId, false)

                    case.link(accountId = accountId, entityId = entityId, tagId = firstTag.id)

                    case.linkedTagIdList(accountId = accountId, entityId = entityId) shouldBe listOf(firstTag.id)
                    database.tagLinkDao().findByFromTagIdList(listOf(firstTag.id, secondTag.id)).shouldBeEmpty()
                    case.tagTabIdList(database, accountId, secondTag.id).shouldBeEmpty()
                    database
                        .entityTagTable(kind = case.kind)
                        .findByEntityIdList(listOf(entityId))
                        .map { entityTag -> entityTag.tagId } shouldBe listOf(firstTag.id)
                }

                test("${case.tc(ItemTc.DELETE_UNDO)}삭제하고 실행 취소해도 태그 연결은 그대로 남는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val tag = tag()
                    insertTag(accountId, tag)
                    val entityId = case.insert(database, accountId, false)
                    case.link(accountId = accountId, entityId = entityId, tagId = tag.id)

                    case.updateDeleted(database, accountId, entityId, true)
                    database
                        .entityTagTable(kind = case.kind)
                        .findByEntityIdList(listOf(entityId))
                        .map { entityTag -> entityTag.tagId to entityTag.isDeleted } shouldBe listOf(tag.id to false)

                    case.updateDeleted(database, accountId, entityId, false)
                    database
                        .entityTagTable(kind = case.kind)
                        .findByEntityIdList(listOf(entityId))
                        .map { entityTag -> entityTag.tagId to entityTag.isDeleted } shouldBe listOf(tag.id to false)
                    case.tagTabIdList(database, accountId, tag.id) shouldBe listOf(entityId)
                }
            }
        }

        context("연락처") {
            val contactKind = itemKindList.single { case -> case.kind == EntityTagKindLocalEntity.CONTACT }

            test("TC-86e4971a-0d24-46a8-85ad-2053b7f89bff 즐겨찾기 변경은 태그 연결을 바꾸지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val firstTag = tag()
                val secondTag = tag()
                insertTag(accountId, firstTag, secondTag)
                val contactId = contactKind.insert(database, accountId, false)
                contactKind.link(accountId = accountId, entityId = contactId, tagId = firstTag.id)
                contactKind.link(accountId = accountId, entityId = contactId, tagId = secondTag.id)
                val before = contactKind.linkedTagIdList(accountId = accountId, entityId = contactId)
                val isFavorite =
                    AccountContactLocalDataSourceImpl(database = database)
                        .find(accountId = accountId, contactId = contactId)
                        .first()
                        .shouldNotBeNull()
                        .isFavorite

                AccountContactTransactionImpl(database = database).updateFavorite(
                    accountId = accountId,
                    contactId = contactId,
                    isFavorite = !isFavorite,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )

                contactKind.linkedTagIdList(accountId = accountId, entityId = contactId) shouldBe before
                before shouldContainExactlyInAnyOrder listOf(firstTag.id, secondTag.id)
            }

            test("TC-13e108be-8c25-41f3-99c1-74920a93c1ef 캘린더 태그 필터는 생일 표시에 적용되지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val linkedTag = tag()
                val otherTag = tag()
                insertTag(accountId, linkedTag, otherTag)
                val contact =
                    Arb.contactLocalEntity().next().let { contact ->
                        contact.copy(
                            isDeleted = false,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                            createdAt = Arb.secondPrecisionInstant().next(),
                            detail =
                                contact.detail.copy(
                                    birthday = BIRTHDAY,
                                    birthdayCalendar = ContactBirthdayCalendarLocalEntity.SOLAR,
                                    birthYear = BIRTHDAY.year,
                                ),
                        )
                    }
                AccountContactTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    contactList = listOf(contact),
                    contactTagList = emptyList(),
                )
                contactKind.link(accountId = accountId, entityId = contact.id, tagId = linkedTag.id)
                val filterDataSource = AccountCalendarFilterLocalDataSourceImpl(database = database)
                val birthdayDataSource = AccountCalendarContactBirthdayLocalDataSourceImpl(database = database)

                suspend fun birthdayList() =
                    birthdayDataSource
                        .get(accountId = accountId, dateRange = BIRTHDAY_RANGE)
                        .first()
                        .map { birthday -> birthday.contactId to birthday.birthdayDate }

                filterDataSource.upsert(accountId = accountId, tagId = otherTag.id)
                birthdayList() shouldBe listOf(contact.id to BIRTHDAY_IN_RANGE)

                filterDataSource.deleteAll(accountId = accountId)
                birthdayList() shouldBe listOf(contact.id to BIRTHDAY_IN_RANGE)
            }

            test("TC-a5114bfa-4e65-48d8-8bd9-fe55794236b9 연락처에 태그를 연결해도 연결된 메모에는 태그가 연결되지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val memo = Arb.localMemo(isFinished = false, isDeleted = false, primaryTagId = null).next()
                val tag = tag()
                insertTag(accountId, tag)
                val contactId = contactKind.insert(database, accountId, false)
                AccountMemoTransactionImpl(database = database).upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
                AccountMemoContactTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    memoId = memo.id,
                    contactId = contactId,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )

                contactKind.link(accountId = accountId, entityId = contactId, tagId = tag.id)

                AccountMemoTagLocalDataSourceImpl(database = database)
                    .getTagList(accountId = accountId, memoId = memo.id)
                    .first()
                    .shouldBeEmpty()
                AccountTagMemoLocalDataSourceImpl(database = database)
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                    .pagedIdList<MemoLocalEntity> { memo -> memo.id }
                    .shouldBeEmpty()
                AccountMemoContactLocalDataSourceImpl(database = database)
                    .getContactList(accountId = accountId, memoId = memo.id)
                    .first()
                    .map { contact -> contact.contact.id } shouldBe listOf(contactId)
            }
        }

        context("체크리스트") {
            test("TC-e7eb0dfb-6a0a-401e-bada-55f8b20b53c2 태그를 연결해도 체크 항목은 바뀌지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = tag()
                insertTag(accountId, tag)
                val checklist = Arb.checklistLocalEntity().next().copy(isDeleted = false, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                val itemList =
                    listOf(true, false, true).map { isChecked ->
                        Arb
                            .localChecklistItem(isChecked = isChecked, isDeleted = false, checklistId = checklist.id)
                            .next()
                            .copy(updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                    }
                AccountChecklistTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    checklist = checklist,
                    itemList = itemList,
                    checklistTagList = emptyList(),
                )
                val itemDataSource = AccountChecklistItemLocalDataSourceImpl(database = database)
                val before =
                    ChecklistItemSortLocalEntity.entries.associateWith { sort ->
                        itemDataSource.get(accountId = accountId, checklistId = checklist.id, sort = sort).first()
                    }
                before.values.forEach { list -> list shouldContainExactlyInAnyOrder itemList }

                itemKindList
                    .single { case -> case.kind == EntityTagKindLocalEntity.CHECKLIST }
                    .link(accountId = accountId, entityId = checklist.id, tagId = tag.id)

                ChecklistItemSortLocalEntity.entries.forEach { sort ->
                    itemDataSource.get(accountId = accountId, checklistId = checklist.id, sort = sort).first() shouldBe before.getValue(sort)
                }
            }
        }
    }) {
    private enum class ItemTc {
        CONTENT,
        DELETED_ENTITY,
        ENTITY_NOT_PENDING,
        TAG_TAB,
        HOME_LIST,
        TAG_LINK_NOT_FOLLOWED,
        NO_OTHER_LINK,
        DELETE_UNDO,
    }

    private class ItemKind(
        val kind: EntityTagKindLocalEntity,
        val label: String,
        private val tcMap: Map<ItemTc, List<String>>,
        val insert: suspend (database: DiaryDatabase, accountId: Uuid, isDeleted: Boolean) -> Uuid,
        val snapshot: suspend (database: DiaryDatabase, accountId: Uuid, entityId: Uuid) -> Any?,
        val homeIdLists: suspend (database: DiaryDatabase, accountId: Uuid) -> List<List<Uuid>>,
        val tagTabIdList: suspend (database: DiaryDatabase, accountId: Uuid, tagId: Uuid) -> List<Uuid>,
        val pendingEntityIdList: suspend (database: DiaryDatabase, accountId: Uuid) -> List<Uuid>,
        val clearEntityPending: suspend (database: DiaryDatabase, accountId: Uuid) -> Unit,
        val updateDeleted: suspend (database: DiaryDatabase, accountId: Uuid, entityId: Uuid, isDeleted: Boolean) -> Unit,
    ) {
        fun tc(key: ItemTc): String = tcMap[key]?.joinToString(separator = " ", postfix = " ").orEmpty()

        override fun toString(): String = label
    }

    private companion object {
        private val BIRTHDAY: LocalDate = LocalDate(1990, 7, 8)
        private val BIRTHDAY_IN_RANGE: LocalDate = LocalDate(2026, 7, 8)
        private val BIRTHDAY_RANGE = LocalDate(2026, 7, 5)..LocalDate(2026, 7, 11)

        private val itemKindList: List<ItemKind> =
            listOf(
                ItemKind(
                    kind = EntityTagKindLocalEntity.CONTACT,
                    label = "연락처",
                    tcMap =
                        mapOf(
                            ItemTc.CONTENT to listOf("TC-9c46918a-1428-4ecc-8d7a-7a2476fe487d", "TC-dc6d96c7-5add-40f2-839e-244d634013c7"),
                            ItemTc.DELETED_ENTITY to listOf("TC-eac8f07a-a01b-44a6-9a8e-e4e56ca38330"),
                            ItemTc.ENTITY_NOT_PENDING to listOf("TC-0b3d6f49-609c-475e-9e54-7ee75feacb7f"),
                            ItemTc.TAG_TAB to listOf("TC-3cd0110f-894c-41cb-8197-0f82d1958a05"),
                            ItemTc.HOME_LIST to listOf("TC-107b6fe9-f49f-4caa-8bd3-e02a73e2f26d", "TC-bfc00515-a530-4502-b102-5aa5dfc7dafd"),
                            ItemTc.TAG_LINK_NOT_FOLLOWED to listOf("TC-55e11262-33d0-45d7-a2f0-716d8e425876"),
                            ItemTc.NO_OTHER_LINK to listOf("TC-b2696757-654e-4a6e-b6ff-bed9db94aaf2"),
                            ItemTc.DELETE_UNDO to listOf("TC-bf942faf-8d62-4827-8e3f-891832ee72c2"),
                        ),
                    insert = { database, accountId, isDeleted ->
                        val contact = Arb.contactLocalEntity().next().copy(isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                        AccountContactTransactionImpl(database = database).upsert(
                            accountId = accountId,
                            contactList = listOf(contact),
                            contactTagList = emptyList(),
                        )
                        contact.id
                    },
                    snapshot = { database, accountId, entityId ->
                        AccountContactLocalDataSourceImpl(database = database).find(accountId = accountId, contactId = entityId).first()
                    },
                    homeIdLists = { database, accountId ->
                        val dataSource = AccountContactLocalDataSourceImpl(database = database)
                        val filterList = listOf(null, true, false)
                        ListSortLocalEntity.entries.flatMap { sort ->
                            filterList.flatMap { hasBirthday ->
                                filterList.flatMap { hasBirthYear ->
                                    filterList.map { hasPhoneNumber ->
                                        dataSource
                                            .page(
                                                accountId = accountId,
                                                sort = sort,
                                                hasBirthday = hasBirthday,
                                                hasBirthYear = hasBirthYear,
                                                hasPhoneNumber = hasPhoneNumber,
                                            ).pagedIdList { item -> item.contact.id }
                                    }
                                }
                            }
                        }
                    },
                    tagTabIdList = { database, accountId, tagId ->
                        AccountTagContactLocalDataSourceImpl(database = database)
                            .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                            .pagedIdList { item -> item.contact.id }
                    },
                    pendingEntityIdList = { database, accountId ->
                        AccountContactSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { contact -> contact.id }
                    },
                    clearEntityPending = { database, accountId ->
                        AccountContactSyncTransactionImpl(database = database).clearPending(
                            accountId = accountId,
                            contactList = AccountContactSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId),
                        )
                    },
                    updateDeleted = { database, accountId, entityId, isDeleted ->
                        AccountContactTransactionImpl(database = database).updateDeleted(
                            accountId = accountId,
                            contactId = entityId,
                            isDeleted = isDeleted,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                ),
                ItemKind(
                    kind = EntityTagKindLocalEntity.CHECKLIST,
                    label = "체크리스트",
                    tcMap =
                        mapOf(
                            ItemTc.CONTENT to listOf("TC-6fb0d924-dd02-442e-96d6-255756b462bf", "TC-102c1cbe-4717-4e34-99fa-f3c3af16ea46"),
                            ItemTc.DELETED_ENTITY to listOf("TC-df7e0a99-b09a-4f7d-bab5-781e3ed738be"),
                            ItemTc.ENTITY_NOT_PENDING to listOf("TC-26607e88-5084-4e42-b386-91100b2ab53b"),
                            ItemTc.TAG_TAB to listOf("TC-2bccedc8-4f4c-4e21-b2c4-6ccae67260ce"),
                            ItemTc.HOME_LIST to listOf("TC-f76c9b5e-0222-4946-a015-05c7b5108de8", "TC-fc1c5721-5734-4412-a21e-2ccd35826d48"),
                            ItemTc.TAG_LINK_NOT_FOLLOWED to listOf("TC-189715ec-43b0-46b7-a0f8-78b00299f258"),
                            ItemTc.NO_OTHER_LINK to listOf("TC-8f23d9c8-4dde-4669-bb2d-48235ebdcc26"),
                            ItemTc.DELETE_UNDO to listOf("TC-d023853e-b9cf-4497-8c60-10c4e60d3c4b"),
                        ),
                    insert = { database, accountId, isDeleted ->
                        val checklist = Arb.checklistLocalEntity().next().copy(isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                        val itemList =
                            listOf(true, false).map { isChecked ->
                                Arb
                                    .localChecklistItem(isChecked = isChecked, isDeleted = false, checklistId = checklist.id)
                                    .next()
                                    .copy(updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                            }
                        AccountChecklistTransactionImpl(database = database).upsert(
                            accountId = accountId,
                            checklist = checklist,
                            itemList = itemList,
                            checklistTagList = emptyList(),
                        )
                        checklist.id
                    },
                    snapshot = { database, accountId, entityId ->
                        val checklist = AccountChecklistLocalDataSourceImpl(database = database).find(accountId = accountId, checklistId = entityId).first()
                        checklist?.let {
                            checklist to
                                AccountChecklistItemLocalDataSourceImpl(database = database)
                                    .get(accountId = accountId, checklistId = entityId, sort = ChecklistItemSortLocalEntity.ADDED)
                                    .first()
                        }
                    },
                    homeIdLists = { database, accountId ->
                        listOf(
                            AccountChecklistLocalDataSourceImpl(database = database)
                                .page(accountId = accountId)
                                .pagedIdList { summary -> summary.checklist.id },
                        )
                    },
                    tagTabIdList = { database, accountId, tagId ->
                        AccountTagChecklistLocalDataSourceImpl(database = database)
                            .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF)
                            .pagedIdList { summary -> summary.checklist.id }
                    },
                    pendingEntityIdList = { database, accountId ->
                        AccountChecklistSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { checklist -> checklist.id }
                    },
                    clearEntityPending = { database, accountId ->
                        AccountChecklistSyncTransactionImpl(database = database).clearPending(
                            accountId = accountId,
                            checklistList = AccountChecklistSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId),
                        )
                    },
                    updateDeleted = { database, accountId, entityId, isDeleted ->
                        AccountChecklistTransactionImpl(database = database).updateDeleted(
                            accountId = accountId,
                            checklistId = entityId,
                            isDeleted = isDeleted,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                ),
                ItemKind(
                    kind = EntityTagKindLocalEntity.MUSIC,
                    label = "곡",
                    tcMap =
                        mapOf(
                            ItemTc.CONTENT to listOf("TC-159ffa93-8318-4d33-9da5-4b087b0f97d9", "TC-7d1baa05-7a1f-47dc-94b3-b48430aea162"),
                            ItemTc.DELETED_ENTITY to listOf("TC-0b57fd75-576e-4fe6-b23e-04ea10b3f56c"),
                            ItemTc.ENTITY_NOT_PENDING to listOf("TC-3fd24ff7-0bf8-43a1-8b3c-e6f10f3407ad"),
                            ItemTc.TAG_TAB to listOf("TC-14916f00-37ee-4cce-9621-a0ad1b726eff"),
                            ItemTc.HOME_LIST to listOf("TC-dff96fb4-d08c-495b-91cb-6c3439fb5f03", "TC-558548b5-8e91-4c33-b2f5-95a7201d6615"),
                            ItemTc.TAG_LINK_NOT_FOLLOWED to listOf("TC-9c0bb54e-449e-496e-a933-d1974f9b238d"),
                            ItemTc.NO_OTHER_LINK to listOf("TC-5248d30d-999c-4c0d-b5b7-e467cd4b9690"),
                            ItemTc.DELETE_UNDO to listOf("TC-bc4abf43-1c51-4527-bd76-dd29b5839df2"),
                        ),
                    insert = { database, accountId, isDeleted ->
                        val music = Arb.musicLocalEntity().next().copy(isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                        AccountMusicTransactionImpl(database = database).upsert(accountId = accountId, musicList = listOf(music), musicTagList = emptyList())
                        music.id
                    },
                    snapshot = { database, accountId, entityId ->
                        AccountMusicLocalDataSourceImpl(database = database).find(accountId = accountId, musicId = entityId).first()
                    },
                    homeIdLists = { database, accountId ->
                        val dataSource = AccountMusicLocalDataSourceImpl(database = database)
                        ListSortLocalEntity.entries.map { sort ->
                            dataSource.page(accountId = accountId, sort = sort).pagedIdList { music -> music.id }
                        }
                    },
                    tagTabIdList = { database, accountId, tagId ->
                        AccountTagMusicLocalDataSourceImpl(database = database)
                            .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, sort = ListSortLocalEntity.DEFAULT)
                            .pagedIdList { music -> music.id }
                    },
                    pendingEntityIdList = { database, accountId ->
                        AccountMusicSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { music -> music.id }
                    },
                    clearEntityPending = { database, accountId ->
                        AccountMusicSyncTransactionImpl(database = database).clearPending(
                            accountId = accountId,
                            musicList = AccountMusicSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId),
                        )
                    },
                    updateDeleted = { database, accountId, entityId, isDeleted ->
                        AccountMusicTransactionImpl(database = database).updateDeleted(
                            accountId = accountId,
                            musicId = entityId,
                            isDeleted = isDeleted,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                ),
                ItemKind(
                    kind = EntityTagKindLocalEntity.QR,
                    label = "QR",
                    tcMap =
                        mapOf(
                            ItemTc.CONTENT to listOf("TC-59a34aab-e140-42e0-81aa-ac03600644c8", "TC-28b4f77c-5d2f-4247-a08b-555473ffac8d"),
                            ItemTc.DELETED_ENTITY to listOf("TC-f47a7760-7942-4467-8ddb-5d909f4615a2"),
                            ItemTc.ENTITY_NOT_PENDING to listOf("TC-9898df2f-ba32-4479-8b91-bda582abfadc"),
                            ItemTc.TAG_TAB to listOf("TC-910cfe38-da82-4fde-9a60-0fb0bd12162d"),
                            ItemTc.HOME_LIST to listOf("TC-01994c62-8934-4b82-b522-f16464d9f1d1", "TC-85dcf937-82dc-43e4-ba64-4923b18cfcfe"),
                            ItemTc.TAG_LINK_NOT_FOLLOWED to listOf("TC-6e7a91e3-3424-4f8f-a916-697428bc6f10"),
                            ItemTc.NO_OTHER_LINK to listOf("TC-93f4c632-7cc9-4b58-a812-a3f1ee2beac4"),
                            ItemTc.DELETE_UNDO to listOf("TC-4f47fa46-f8f9-4ed3-ae21-b4f9da5ce523"),
                        ),
                    insert = { database, accountId, isDeleted ->
                        val qr = Arb.qrLocalEntity().next().copy(isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                        AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr), qrTagList = emptyList())
                        qr.id
                    },
                    snapshot = { database, accountId, entityId ->
                        AccountQrLocalDataSourceImpl(database = database).find(accountId = accountId, qrId = entityId).first()
                    },
                    homeIdLists = { database, accountId ->
                        listOf(AccountQrLocalDataSourceImpl(database = database).page(accountId = accountId).pagedIdList { qr -> qr.id })
                    },
                    tagTabIdList = { database, accountId, tagId ->
                        AccountTagQrLocalDataSourceImpl(database = database)
                            .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF)
                            .pagedIdList { qr -> qr.id }
                    },
                    pendingEntityIdList = { database, accountId ->
                        AccountQrSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { qr -> qr.id }
                    },
                    clearEntityPending = { database, accountId ->
                        AccountQrSyncTransactionImpl(database = database).clearPending(
                            accountId = accountId,
                            qrList = AccountQrSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId),
                        )
                    },
                    updateDeleted = { database, accountId, entityId, isDeleted ->
                        AccountQrTransactionImpl(database = database).updateDeleted(
                            accountId = accountId,
                            qrId = entityId,
                            isDeleted = isDeleted,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                ),
                ItemKind(
                    kind = EntityTagKindLocalEntity.DDAY,
                    label = "디데이",
                    tcMap =
                        mapOf(
                            ItemTc.CONTENT to listOf("TC-794a3da0-1cab-4ac5-aa97-a0761302730b"),
                            ItemTc.HOME_LIST to listOf("TC-b096dc1c-757d-4943-8500-8c566766cbdd"),
                            ItemTc.TAG_LINK_NOT_FOLLOWED to listOf("TC-e8b9ab06-a3bd-4e34-a611-8dcd6dd92cd9"),
                            ItemTc.NO_OTHER_LINK to listOf("TC-4b8f4fd2-fa10-43db-bee0-f093b7e9379f"),
                        ),
                    insert = { database, accountId, isDeleted ->
                        val dday = Arb.ddayLocalEntity().next().copy(isDeleted = isDeleted, updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                        AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())
                        dday.id
                    },
                    snapshot = { database, accountId, entityId ->
                        AccountDDayLocalDataSourceImpl(database = database).find(accountId = accountId, ddayId = entityId).first()
                    },
                    homeIdLists = { database, accountId ->
                        listOf(AccountDDayLocalDataSourceImpl(database = database).page(accountId = accountId).pagedIdList { dday -> dday.id })
                    },
                    tagTabIdList = { database, accountId, tagId ->
                        AccountTagDDayLocalDataSourceImpl(database = database)
                            .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF)
                            .pagedIdList { dday -> dday.id }
                    },
                    pendingEntityIdList = { database, accountId ->
                        AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId).map { dday -> dday.id }
                    },
                    clearEntityPending = { database, accountId ->
                        AccountDDaySyncTransactionImpl(database = database).clearPending(
                            accountId = accountId,
                            ddayList = AccountDDaySyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId),
                        )
                    },
                    updateDeleted = { database, accountId, entityId, isDeleted ->
                        AccountDDayTransactionImpl(database = database).updateDeleted(
                            accountId = accountId,
                            ddayId = entityId,
                            isDeleted = isDeleted,
                            updatedAt = Arb.secondPrecisionInstant().next(),
                        )
                    },
                ),
            )

        private fun tag(): TagLocalEntity =
            Arb
                .tagLocalEntity()
                .next()
                .copy(updatedAt = Arb.secondPrecisionInstant().next(), createdAt = Arb.secondPrecisionInstant().next())
                .let { tag -> tag.copy(detail = tag.detail.copy(title = "title-${Arb.string().next()}"), isFinished = false, isDeleted = false) }

        private suspend fun <T : Any> PagingSource<Int, T>.pagedIdList(id: (T) -> Uuid): List<Uuid> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()
                .data
                .map(id)
    }
}
