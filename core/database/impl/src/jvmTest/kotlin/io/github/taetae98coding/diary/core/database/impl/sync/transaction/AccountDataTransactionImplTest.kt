package io.github.taetae98coding.diary.core.database.impl.sync.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
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
        "account_memo_tag",
        "account_memo_place",
        "account_memo_web",
        "account_memo_contact",
        "account_tag_link",
        "account_web_tag",
        "account_place_tag",
        "tag_filter",
        "memo_filter_tag",
        "calendar_filter_tag",
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
        "memo_tag",
        "memo_place",
        "memo_web",
        "memo_contact",
        "tag_link",
        "web_tag",
        "place_tag",
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

        /** 다른 종류를 참조하지 않는 일곱 종류를 한 계정 몫으로 채우고 만든 항목을 돌려준다. */
        suspend fun fillEntity(
            accountId: Uuid,
            isDirty: Boolean,
        ): FilledEntity {
            val memo = fixtureMonkey.giveMeOne<MemoLocalEntity>().copy(primaryTagId = null)
            val tag = fixtureMonkey.giveMeOne<TagLocalEntity>()
            val otherTag = fixtureMonkey.giveMeOne<TagLocalEntity>()
            val place = fixtureMonkey.giveMeOne<PlaceLocalEntity>()
            val web = fixtureMonkey.giveMeOne<WebLocalEntity>()
            val contact = fixtureMonkey.giveMeOne<ContactLocalEntity>()
            val music = fixtureMonkey.giveMeOne<MusicLocalEntity>()
            val qr = fixtureMonkey.giveMeOne<QrLocalEntity>()

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

            return FilledEntity(
                memoId = memo.id,
                tagId = tag.id,
                otherTagId = otherTag.id,
                placeId = place.id,
                webId = web.id,
                contactId = contact.id,
                musicId = music.id,
                qrId = qr.id,
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
        }

        suspend fun fillRelation(
            accountId: Uuid,
            isDirty: Boolean,
            entity: FilledEntity,
        ) {
            database.memoTagDao().upsert(
                fixtureMonkey.giveMeOne<MemoTagLocalEntity>().copy(memoId = entity.memoId, tagId = entity.tagId),
            )
            database.accountMemoTagDao().upsert(
                AccountMemoTagLocalEntity(accountId = accountId, memoId = entity.memoId, tagId = entity.tagId, isDirty = isDirty),
            )

            database.memoPlaceDao().upsert(
                fixtureMonkey.giveMeOne<MemoPlaceLocalEntity>().copy(memoId = entity.memoId, placeId = entity.placeId),
            )
            database.accountMemoPlaceDao().upsert(
                AccountMemoPlaceLocalEntity(accountId = accountId, memoId = entity.memoId, placeId = entity.placeId, isDirty = isDirty),
            )

            database.memoWebDao().upsert(
                fixtureMonkey.giveMeOne<MemoWebLocalEntity>().copy(memoId = entity.memoId, webId = entity.webId),
            )
            database.accountMemoWebDao().upsert(
                AccountMemoWebLocalEntity(accountId = accountId, memoId = entity.memoId, webId = entity.webId, isDirty = isDirty),
            )

            database.memoContactDao().upsert(
                fixtureMonkey.giveMeOne<MemoContactLocalEntity>().copy(memoId = entity.memoId, contactId = entity.contactId),
            )
            database.accountMemoContactDao().upsert(
                AccountMemoContactLocalEntity(accountId = accountId, memoId = entity.memoId, contactId = entity.contactId, isDirty = isDirty),
            )

            database.tagLinkDao().upsert(
                fixtureMonkey.giveMeOne<TagLinkLocalEntity>().copy(fromTagId = entity.tagId, toTagId = entity.otherTagId),
            )
            database.accountTagLinkDao().upsert(
                AccountTagLinkLocalEntity(
                    accountId = accountId,
                    fromTagId = entity.tagId,
                    toTagId = entity.otherTagId,
                    isDirty = isDirty,
                ),
            )

            database.webTagDao().upsert(
                fixtureMonkey.giveMeOne<WebTagLocalEntity>().copy(webId = entity.webId, tagId = entity.tagId),
            )
            database.accountWebTagDao().upsert(
                AccountWebTagLocalEntity(accountId = accountId, webId = entity.webId, tagId = entity.tagId, isDirty = isDirty),
            )

            database.placeTagDao().upsert(
                fixtureMonkey.giveMeOne<PlaceTagLocalEntity>().copy(placeId = entity.placeId, tagId = entity.tagId),
            )
            database.accountPlaceTagDao().upsert(
                AccountPlaceTagLocalEntity(accountId = accountId, placeId = entity.placeId, tagId = entity.tagId, isDirty = isDirty),
            )
        }

        suspend fun fillAccountState(
            accountId: Uuid,
            entity: FilledEntity,
        ) {
            database.accountTagFilterDao().upsert(TagFilterLocalEntity(accountId = accountId, isTopLevelOnly = true))
            database.memoFilterTagDao().upsert(MemoFilterTagLocalEntity(accountId = accountId, tagId = entity.tagId))
            database.calendarFilterTagDao().upsert(CalendarFilterTagLocalEntity(accountId = accountId, tagId = entity.tagId))

            SyncKind.entries.forEach { kind ->
                database.syncCursorDao().upsert(
                    SyncCursorLocalEntity(
                        accountId = accountId,
                        kind = SyncCursorLocalEntity.column(kind = kind),
                        usn = fixtureMonkey.giveMeOne<Long>(),
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

        test("TC-DATA-SYNC-DATA-035 강제 전체 재동기화는 그 계정의 항목과 연결, 업로드 대기 항목, 동기화 상태와 필터 선택을 지운다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            fill(accountId = accountId)

            transaction.delete(accountId = accountId)

            ACCOUNT_TABLE_LIST.forEach { table ->
                withClue(table) { countRow(table = table) shouldBe 0 }
            }
            ENTITY_TABLE_LIST.forEach { table ->
                withClue(table) { countRow(table = table) shouldBe 0 }
            }
        }

        test("TC-DATA-SYNC-DATA-036 강제 전체 재동기화는 게스트와 다른 계정의 데이터, 기기에 유지하는 설정을 지우지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-DATA-SYNC-DATA-051 강제 전체 재동기화는 다른 계정과도 연결된 항목과 연결을 남기고 그 계정과의 연결만 지운다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
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

        test("TC-DATA-SYNC-DATA-037 강제 전체 재동기화 뒤에는 열네 종류가 모두 기본 커서로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            fill(accountId = accountId)

            transaction.delete(accountId = accountId)

            SyncKind.entries.forEach { kind ->
                withClue(kind.name) {
                    syncCursorLocalDataSource.find(accountId = accountId, kind = kind) shouldBe SyncCursorLocalDataSource.DEFAULT_USN
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
