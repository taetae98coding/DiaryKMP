package io.github.taetae98coding.diary.core.database.impl.spotlight.dao

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class SpotlightDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemo(
            accountId: Uuid,
            vararg entityList: MemoLocalEntity,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(entityList.toList())
                database.accountMemoDao().upsert(entityList.map { entity -> AccountMemoLocalEntity(accountId = accountId, memoId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertTag(
            accountId: Uuid,
            vararg entityList: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(entityList.toList())
                database.accountTagDao().upsert(entityList.map { entity -> AccountTagLocalEntity(accountId = accountId, tagId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertPlace(
            accountId: Uuid,
            vararg entityList: PlaceLocalEntity,
        ) {
            database.withWriteTransaction {
                database.placeDao().upsert(entityList.toList())
                database.accountPlaceDao().upsert(entityList.map { entity -> AccountPlaceLocalEntity(accountId = accountId, placeId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertWeb(
            accountId: Uuid,
            vararg entityList: WebLocalEntity,
        ) {
            database.withWriteTransaction {
                database.webDao().upsert(entityList.toList())
                database.accountWebDao().upsert(entityList.map { entity -> AccountWebLocalEntity(accountId = accountId, webId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertContact(
            accountId: Uuid,
            vararg entityList: ContactLocalEntity,
        ) {
            database.withWriteTransaction {
                database.contactDao().upsert(entityList.toList())
                database.accountContactDao().upsert(entityList.map { entity -> AccountContactLocalEntity(accountId = accountId, contactId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertChecklist(
            accountId: Uuid,
            vararg entityList: ChecklistLocalEntity,
        ) {
            database.withWriteTransaction {
                database.checklistDao().upsert(entityList.toList())
                database.accountChecklistDao().upsert(entityList.map { entity -> AccountChecklistLocalEntity(accountId = accountId, checklistId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertQr(
            accountId: Uuid,
            vararg entityList: QrLocalEntity,
        ) {
            database.withWriteTransaction {
                database.qrDao().upsert(entityList.toList())
                database.accountQrDao().upsert(entityList.map { entity -> AccountQrLocalEntity(accountId = accountId, qrId = entity.id, isDirty = true) })
            }
        }

        suspend fun insertMusic(
            accountId: Uuid,
            vararg entityList: MusicLocalEntity,
        ) {
            database.withWriteTransaction {
                database.musicDao().upsert(entityList.toList())
                database.accountMusicDao().upsert(entityList.map { entity -> AccountMusicLocalEntity(accountId = accountId, musicId = entity.id, isDirty = true) })
            }
        }

        test("TC-75a4449a-4607-4404-bee9-cdfb3d735a29 현재 계정의 삭제되지 않은 메모와 태그를 완료 여부와 목록 필터와 관계없이 돌려준다") {
            val dao = database.spotlightDao()
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val filterTag = Arb.tagLocalEntity().next().copy(isDeleted = false)
            val activeMemo = Arb.memoLocalEntity().next().copy(isFinished = false, isDeleted = false, primaryTagId = null)
            val finishedMemo = Arb.memoLocalEntity().next().copy(isFinished = true, isDeleted = false, primaryTagId = null)
            val deletedMemo = Arb.memoLocalEntity().next().copy(isDeleted = true)
            val otherAccountMemo = Arb.memoLocalEntity().next().copy(isDeleted = false)
            val activeTag = Arb.tagLocalEntity().next().copy(isFinished = false, isDeleted = false)
            val finishedTag = Arb.tagLocalEntity().next().copy(isFinished = true, isDeleted = false)
            val deletedTag = Arb.tagLocalEntity().next().copy(isDeleted = true)

            insertMemo(accountId, activeMemo, finishedMemo, deletedMemo)
            insertMemo(otherAccountId, otherAccountMemo)
            insertTag(accountId, filterTag, activeTag, finishedTag, deletedTag)
            // 두 메모 모두 이 태그와 연결되어 있지 않아 MemoHome 목록 필터에서는 보이지 않는다.
            database.memoFilterTagDao().upsert(entity = MemoFilterTagLocalEntity(accountId = accountId, tagId = filterTag.id))

            dao.getMemoList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(activeMemo.id, finishedMemo.id)
            dao.getTagList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(filterTag.id, activeTag.id, finishedTag.id)
        }

        test("TC-75a4449a-4607-4404-bee9-cdfb3d735a29 장소, 웹, 연락처, 체크리스트, QR, 곡은 현재 계정의 삭제되지 않은 항목을 돌려준다") {
            val dao = database.spotlightDao()
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()

            val place = Arb.placeLocalEntity().next().copy(isDeleted = false)
            val deletedPlace = Arb.placeLocalEntity().next().copy(isDeleted = true)
            insertPlace(accountId, place, deletedPlace)
            insertPlace(otherAccountId, Arb.placeLocalEntity().next().copy(isDeleted = false))
            dao.getPlaceList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(place.id)

            val web = Arb.webLocalEntity().next().copy(isDeleted = false)
            val deletedWeb = Arb.webLocalEntity().next().copy(isDeleted = true)
            insertWeb(accountId, web, deletedWeb)
            insertWeb(otherAccountId, Arb.webLocalEntity().next().copy(isDeleted = false))
            dao.getWebList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(web.id)

            val contact = Arb.contactLocalEntity().next().copy(isDeleted = false)
            val deletedContact = Arb.contactLocalEntity().next().copy(isDeleted = true)
            insertContact(accountId, contact, deletedContact)
            insertContact(otherAccountId, Arb.contactLocalEntity().next().copy(isDeleted = false))
            dao.getContactList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(contact.id)

            val checklist = Arb.checklistLocalEntity().next().copy(isDeleted = false)
            val deletedChecklist = Arb.checklistLocalEntity().next().copy(isDeleted = true)
            insertChecklist(accountId, checklist, deletedChecklist)
            insertChecklist(otherAccountId, Arb.checklistLocalEntity().next().copy(isDeleted = false))
            dao.getChecklistList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(checklist.id)

            val qr = Arb.qrLocalEntity().next().copy(isDeleted = false)
            val deletedQr = Arb.qrLocalEntity().next().copy(isDeleted = true)
            insertQr(accountId, qr, deletedQr)
            insertQr(otherAccountId, Arb.qrLocalEntity().next().copy(isDeleted = false))
            dao.getQrList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(qr.id)

            val music = Arb.musicLocalEntity().next().copy(isDeleted = false)
            val deletedMusic = Arb.musicLocalEntity().next().copy(isDeleted = true)
            insertMusic(accountId, music, deletedMusic)
            insertMusic(otherAccountId, Arb.musicLocalEntity().next().copy(isDeleted = false))
            dao.getMusicList(accountId = accountId).first().map { it.id } shouldContainExactlyInAnyOrder listOf(music.id)
        }
    })
