package io.github.taetae98coding.diary.core.database.impl.sync.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.entity.DDayFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.checklistitem.checklistItemLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.entitytag.entityTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.memocontact.memoContactLocalEntity
import io.github.taetae98coding.diary.core.testing.memoplace.memoPlaceLocalEntity
import io.github.taetae98coding.diary.core.testing.memotag.memoTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memoweb.memoWebLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.core.testing.taglink.tagLinkLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

private val ACCOUNT_TABLE_LIST =
    listOf(
        "account_memo",
        "account_tag",
        "account_place",
        "account_web",
        "account_contact",
        "account_music",
        "account_qr",
        "account_checklist",
        "account_checklist_item",
        "account_dday",
        "account_memo_tag",
        "account_memo_place",
        "account_memo_web",
        "account_memo_contact",
        "account_tag_link",
        "account_web_tag",
        "account_place_tag",
        "account_contact_tag",
        "account_checklist_tag",
        "account_music_tag",
        "account_qr_tag",
        "account_dday_tag",
        "tag_filter",
        "memo_filter_tag",
        "calendar_filter_tag",
        "dday_filter_tag",
        "sync_cursor",
    )

private val ENTITY_TABLE_LIST =
    listOf(
        "memo",
        "tag",
        "place",
        "web",
        "contact",
        "music",
        "qr",
        "checklist",
        "checklist_item",
        "dday",
        "memo_tag",
        "memo_place",
        "memo_web",
        "memo_contact",
        "tag_link",
        "web_tag",
        "place_tag",
        "contact_tag",
        "checklist_tag",
        "music_tag",
        "qr_tag",
        "dday_tag",
    )

private data class FilledEntity(
    val memoId: Uuid,
    val tagId: Uuid,
    val otherTagId: Uuid,
    val placeId: Uuid,
    val webId: Uuid,
    val contactId: Uuid,
    val musicId: Uuid,
    val qrId: Uuid,
    val checklistId: Uuid,
    val checklistItemId: Uuid,
    val ddayId: Uuid,
)

class AccountDataTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountDataTransactionImpl
        lateinit var syncCursorLocalDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountDataTransactionImpl(database = database)
            syncCursorLocalDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun countRow(
            table: String,
            where: String = "1",
        ): Int =
            database.useReaderConnection { transactor ->
                transactor.usePrepared("SELECT COUNT(*) FROM $table WHERE $where") { statement ->
                    statement.step()
                    statement.getInt(0)
                }
            }

        suspend fun fillEntity(
            accountId: Uuid,
            isDirty: Boolean,
        ): FilledEntity {
            val memo = Arb.memoLocalEntity().next().copy(primaryTagId = null)
            val tag = Arb.tagLocalEntity().next()
            val otherTag = Arb.tagLocalEntity().next()
            val place = Arb.placeLocalEntity().next()
            val web = Arb.webLocalEntity().next()
            val contact = Arb.contactLocalEntity().next()
            val music = Arb.musicLocalEntity().next()
            val qr = Arb.qrLocalEntity().next()
            val checklist = Arb.checklistLocalEntity().next()
            val checklistItem = Arb.checklistItemLocalEntity().next().copy(checklistId = checklist.id)
            val dday = Arb.ddayLocalEntity().next()

            database.memoDao().upsert(memo)
            database.accountMemoDao().upsert(AccountMemoLocalEntity(accountId = accountId, memoId = memo.id, isDirty = isDirty))

            listOf(tag, otherTag).forEach { target ->
                database.tagDao().upsert(target)
                database.accountTagDao().upsert(AccountTagLocalEntity(accountId = accountId, tagId = target.id, isDirty = isDirty))
            }

            database.placeDao().upsert(place)
            database.accountPlaceDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = place.id, isDirty = isDirty))

            database.webDao().upsert(web)
            database.accountWebDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = web.id, isDirty = isDirty))

            database.contactDao().upsert(contact)
            database.accountContactDao().upsert(
                AccountContactLocalEntity(accountId = accountId, contactId = contact.id, isDirty = isDirty),
            )

            database.musicDao().upsert(music)
            database.accountMusicDao().upsert(AccountMusicLocalEntity(accountId = accountId, musicId = music.id, isDirty = isDirty))

            database.qrDao().upsert(qr)
            database.accountQrDao().upsert(AccountQrLocalEntity(accountId = accountId, qrId = qr.id, isDirty = isDirty))

            database.checklistDao().upsert(checklist)
            database.accountChecklistDao().upsert(
                AccountChecklistLocalEntity(accountId = accountId, checklistId = checklist.id, isDirty = isDirty),
            )

            database.checklistItemDao().upsert(checklistItem)
            database.accountChecklistItemDao().upsert(
                AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = checklistItem.id, isDirty = isDirty),
            )

            database.ddayDao().upsert(dday)
            database.accountDDayDao().upsert(AccountDDayLocalEntity(accountId = accountId, ddayId = dday.id, isDirty = isDirty))

            return FilledEntity(
                memoId = memo.id,
                tagId = tag.id,
                otherTagId = otherTag.id,
                placeId = place.id,
                webId = web.id,
                contactId = contact.id,
                musicId = music.id,
                qrId = qr.id,
                checklistId = checklist.id,
                checklistItemId = checklistItem.id,
                ddayId = dday.id,
            )
        }

        suspend fun linkEntityToAccount(
            accountId: Uuid,
            entity: FilledEntity,
        ) {
            database.accountMemoDao().upsert(AccountMemoLocalEntity(accountId = accountId, memoId = entity.memoId, isDirty = false))
            listOf(entity.tagId, entity.otherTagId).forEach { tagId ->
                database.accountTagDao().upsert(AccountTagLocalEntity(accountId = accountId, tagId = tagId, isDirty = false))
            }
            database.accountPlaceDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = entity.placeId, isDirty = false))
            database.accountWebDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = entity.webId, isDirty = false))
            database.accountContactDao().upsert(
                AccountContactLocalEntity(accountId = accountId, contactId = entity.contactId, isDirty = false),
            )
            database.accountMusicDao().upsert(AccountMusicLocalEntity(accountId = accountId, musicId = entity.musicId, isDirty = false))
            database.accountQrDao().upsert(AccountQrLocalEntity(accountId = accountId, qrId = entity.qrId, isDirty = false))
            database.accountChecklistDao().upsert(
                AccountChecklistLocalEntity(accountId = accountId, checklistId = entity.checklistId, isDirty = false),
            )
            database.accountChecklistItemDao().upsert(
                AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = entity.checklistItemId, isDirty = false),
            )
            database.accountDDayDao().upsert(AccountDDayLocalEntity(accountId = accountId, ddayId = entity.ddayId, isDirty = false))
        }

        suspend fun fillRelation(
            accountId: Uuid,
            isDirty: Boolean,
            entity: FilledEntity,
        ) {
            database.memoTagDao().upsert(
                Arb.memoTagLocalEntity().next().copy(memoId = entity.memoId, tagId = entity.tagId),
            )
            database.accountMemoTagDao().upsert(
                AccountMemoTagLocalEntity(accountId = accountId, memoId = entity.memoId, tagId = entity.tagId, isDirty = isDirty),
            )

            database.memoPlaceDao().upsert(
                Arb.memoPlaceLocalEntity().next().copy(memoId = entity.memoId, placeId = entity.placeId),
            )
            database.accountMemoPlaceDao().upsert(
                AccountMemoPlaceLocalEntity(accountId = accountId, memoId = entity.memoId, placeId = entity.placeId, isDirty = isDirty),
            )

            database.memoWebDao().upsert(
                Arb.memoWebLocalEntity().next().copy(memoId = entity.memoId, webId = entity.webId),
            )
            database.accountMemoWebDao().upsert(
                AccountMemoWebLocalEntity(accountId = accountId, memoId = entity.memoId, webId = entity.webId, isDirty = isDirty),
            )

            database.memoContactDao().upsert(
                Arb.memoContactLocalEntity().next().copy(memoId = entity.memoId, contactId = entity.contactId),
            )
            database.accountMemoContactDao().upsert(
                AccountMemoContactLocalEntity(accountId = accountId, memoId = entity.memoId, contactId = entity.contactId, isDirty = isDirty),
            )

            database.tagLinkDao().upsert(
                Arb.tagLinkLocalEntity().next().copy(fromTagId = entity.tagId, toTagId = entity.otherTagId),
            )
            database.accountTagLinkDao().upsert(
                AccountTagLinkLocalEntity(
                    accountId = accountId,
                    fromTagId = entity.tagId,
                    toTagId = entity.otherTagId,
                    isDirty = isDirty,
                ),
            )

            EntityTagKindLocalEntity.entries.forEach { kind ->
                val entityTag =
                    Arb.entityTagLocalEntity().next().copy(
                        entityId =
                            when (kind) {
                                EntityTagKindLocalEntity.WEB -> entity.webId
                                EntityTagKindLocalEntity.PLACE -> entity.placeId
                                EntityTagKindLocalEntity.CONTACT -> entity.contactId
                                EntityTagKindLocalEntity.CHECKLIST -> entity.checklistId
                                EntityTagKindLocalEntity.MUSIC -> entity.musicId
                                EntityTagKindLocalEntity.QR -> entity.qrId
                                EntityTagKindLocalEntity.DDAY -> entity.ddayId
                            },
                        tagId = entity.tagId,
                    )
                val table = database.entityTagTable(kind = kind)

                table.upsert(entityTagList = listOf(entityTag))
                if (isDirty) {
                    table.upsertAccountPending(accountId = accountId, entityTagList = listOf(entityTag))
                } else {
                    table.insertIgnoreAccountSynced(accountId = accountId, entityTagList = listOf(entityTag))
                }
            }
        }

        suspend fun fillAccountState(
            accountId: Uuid,
            entity: FilledEntity,
        ) {
            database.accountTagFilterDao().upsert(TagFilterLocalEntity(accountId = accountId, isTopLevelOnly = true))
            database.memoFilterTagDao().upsert(MemoFilterTagLocalEntity(accountId = accountId, tagId = entity.tagId))
            database.calendarFilterTagDao().upsert(CalendarFilterTagLocalEntity(accountId = accountId, tagId = entity.tagId))
            database.ddayFilterTagDao().upsert(DDayFilterTagLocalEntity(accountId = accountId, tagId = entity.tagId))

            SyncKindLocalEntity.entries.forEach { kind ->
                database.syncCursorDao().upsert(
                    SyncCursorLocalEntity(
                        accountId = accountId,
                        kind = kind.persistentValue,
                        usn = Arb.long().next(),
                    ),
                )
            }
        }

        /** 업로드 대기 항목을 함께 두기 위해 계정 연결은 모두 [isDirty]로 기록한다. */
        suspend fun fill(
            accountId: Uuid,
            isDirty: Boolean = true,
        ) {
            val entity = fillEntity(accountId = accountId, isDirty = isDirty)
            fillRelation(accountId = accountId, isDirty = isDirty, entity = entity)
            fillAccountState(accountId = accountId, entity = entity)
        }

        test("TC-ce8de4c6-cb52-4b84-a7d8-ef0fbd8517b7 TC-e64cb655-d2fc-4b69-942a-20e3ad112f67 강제 전체 재동기화는 그 계정의 항목과 연결, 업로드 대기 항목, 동기화 상태와 필터 선택을 지운다") {
            val accountId = Arb.kotlinUuid().next()
            fill(accountId = accountId)

            transaction.delete(accountId = accountId)

            ACCOUNT_TABLE_LIST.forEach { table ->
                withClue(table) { countRow(table = table) shouldBe 0 }
            }
            ENTITY_TABLE_LIST.forEach { table ->
                withClue(table) { countRow(table = table) shouldBe 0 }
            }
        }

        test("TC-fb5b77ea-f160-4081-baed-4c400465f7ea TC-e64cb655-d2fc-4b69-942a-20e3ad112f67 강제 전체 재동기화는 게스트와 다른 계정의 데이터, 기기에 유지하는 설정을 지우지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            fill(accountId = accountId)
            fill(accountId = Uuid.NIL)
            fill(accountId = otherAccountId)
            database.memoExistenceFilterDao().upsert(
                MemoExistenceFilterLocalEntity(hasDate = true, hasTag = true, hasPlace = true),
            )

            transaction.delete(accountId = accountId)

            ACCOUNT_TABLE_LIST.forEach { table ->
                withClue(table) {
                    countRow(table = table, where = "account_id = '$accountId'") shouldBe 0
                    countRow(table = table, where = "account_id = '${Uuid.NIL}'") shouldBeGreaterThan 0
                    countRow(table = table, where = "account_id = '$otherAccountId'") shouldBeGreaterThan 0
                }
            }
            ENTITY_TABLE_LIST.forEach { table ->
                withClue(table) { countRow(table = table) shouldBeGreaterThan 0 }
            }
            countRow(table = "memo_existence_filter") shouldBe 1
        }

        test("TC-775f15b7-762d-4817-ad69-41b01e6aa9c4 강제 전체 재동기화는 다른 계정과도 연결된 항목과 연결을 남기고 그 계정과의 연결만 지운다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val entity = fillEntity(accountId = accountId, isDirty = true)
            fillRelation(accountId = accountId, isDirty = true, entity = entity)
            linkEntityToAccount(accountId = otherAccountId, entity = entity)
            fillRelation(accountId = otherAccountId, isDirty = false, entity = entity)

            transaction.delete(accountId = accountId)

            ENTITY_TABLE_LIST.forEach { table ->
                withClue(table) { countRow(table = table) shouldBeGreaterThan 0 }
            }
            ACCOUNT_TABLE_LIST.filter { table -> table.startsWith("account_") }.forEach { table ->
                withClue(table) {
                    countRow(table = table, where = "account_id = '$accountId'") shouldBe 0
                    countRow(table = table, where = "account_id = '$otherAccountId'") shouldBeGreaterThan 0
                }
            }
        }

        test("TC-c246624e-aaa2-445e-9493-99a81ffdb6a2 강제 전체 재동기화 뒤에는 스물두 종류가 모두 기본 커서로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            fill(accountId = accountId)

            transaction.delete(accountId = accountId)

            SyncKindLocalEntity.entries.forEach { kind ->
                withClue(kind.name) {
                    syncCursorLocalDataSource.read(accountId = accountId, kind = kind) shouldBe SyncCursorLocalDataSource.DEFAULT_USN
                }
            }
        }
    })
