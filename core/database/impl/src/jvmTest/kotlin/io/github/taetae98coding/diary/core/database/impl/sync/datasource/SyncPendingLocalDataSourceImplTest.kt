package io.github.taetae98coding.diary.core.database.impl.sync.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.AccountChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.AccountContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.AccountDDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.AccountMusicTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.AccountQrTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.AccountRoutineTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import java.io.File
import kotlin.uuid.Uuid

private typealias PendingWriter = suspend (DiaryDatabase, Uuid, Boolean) -> Unit

private val pendingWriterMap: Map<String, PendingWriter> =
    mapOf(
        "태그" to { database, accountId, isDirty ->
            database.accountTagSyncDao().upsert(AccountTagLocalEntity(accountId = accountId, tagId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "장소" to { database, accountId, isDirty ->
            database.accountPlaceSyncDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "웹 항목" to { database, accountId, isDirty ->
            database.accountWebSyncDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "연락처" to { database, accountId, isDirty ->
            database.accountContactSyncDao().upsert(AccountContactLocalEntity(accountId = accountId, contactId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "곡" to { database, accountId, isDirty ->
            database.accountMusicSyncDao().upsert(AccountMusicLocalEntity(accountId = accountId, musicId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "QR" to { database, accountId, isDirty ->
            database.accountQrSyncDao().upsert(AccountQrLocalEntity(accountId = accountId, qrId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "체크리스트" to { database, accountId, isDirty ->
            database.accountChecklistSyncDao().upsert(
                AccountChecklistLocalEntity(accountId = accountId, checklistId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "체크 항목" to { database, accountId, isDirty ->
            database.accountChecklistItemSyncDao().upsert(
                AccountChecklistItemLocalEntity(accountId = accountId, checklistItemId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "메모" to { database, accountId, isDirty ->
            database.accountMemoSyncDao().upsert(AccountMemoLocalEntity(accountId = accountId, memoId = Arb.kotlinUuid().next(), isDirty = isDirty))
        },
        "메모와 태그의 연결" to { database, accountId, isDirty ->
            database.accountMemoTagSyncDao().upsert(
                AccountMemoTagLocalEntity(accountId = accountId, memoId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "메모와 장소의 연결" to { database, accountId, isDirty ->
            database.accountMemoPlaceSyncDao().upsert(
                AccountMemoPlaceLocalEntity(accountId = accountId, memoId = Arb.kotlinUuid().next(), placeId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "메모와 웹 항목의 연결" to { database, accountId, isDirty ->
            database.accountMemoWebSyncDao().upsert(
                AccountMemoWebLocalEntity(accountId = accountId, memoId = Arb.kotlinUuid().next(), webId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "메모와 연락처의 연결" to { database, accountId, isDirty ->
            database.accountMemoContactSyncDao().upsert(
                AccountMemoContactLocalEntity(accountId = accountId, memoId = Arb.kotlinUuid().next(), contactId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "태그와 태그의 연결" to { database, accountId, isDirty ->
            database.accountTagLinkSyncDao().upsert(
                AccountTagLinkLocalEntity(accountId = accountId, fromTagId = Arb.kotlinUuid().next(), toTagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "웹 항목과 태그의 연결" to { database, accountId, isDirty ->
            database.accountWebTagSyncDao().upsert(
                AccountWebTagLocalEntity(accountId = accountId, webId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "장소와 태그의 연결" to { database, accountId, isDirty ->
            database.accountPlaceTagSyncDao().upsert(
                AccountPlaceTagLocalEntity(accountId = accountId, placeId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "연락처와 태그의 연결" to { database, accountId, isDirty ->
            database.accountContactTagSyncDao().upsert(
                AccountContactTagLocalEntity(accountId = accountId, contactId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "체크리스트와 태그의 연결" to { database, accountId, isDirty ->
            database.accountChecklistTagSyncDao().upsert(
                AccountChecklistTagLocalEntity(accountId = accountId, checklistId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "곡과 태그의 연결" to { database, accountId, isDirty ->
            database.accountMusicTagSyncDao().upsert(
                AccountMusicTagLocalEntity(accountId = accountId, musicId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "QR과 태그의 연결" to { database, accountId, isDirty ->
            database.accountQrTagSyncDao().upsert(
                AccountQrTagLocalEntity(accountId = accountId, qrId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "디데이" to { database, accountId, isDirty ->
            database.accountDDaySyncDao().upsert(
                AccountDDayLocalEntity(accountId = accountId, ddayId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "디데이와 태그의 연결" to { database, accountId, isDirty ->
            database.accountDDayTagSyncDao().upsert(
                AccountDDayTagLocalEntity(accountId = accountId, ddayId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "루틴" to { database, accountId, isDirty ->
            database.accountRoutineSyncDao().upsert(
                AccountRoutineLocalEntity(accountId = accountId, routineId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "루틴와 태그의 연결" to { database, accountId, isDirty ->
            database.accountRoutineTagSyncDao().upsert(
                AccountRoutineTagLocalEntity(accountId = accountId, routineId = Arb.kotlinUuid().next(), tagId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
        "루틴 기록" to { database, accountId, isDirty ->
            database.accountRoutineRecordSyncDao().upsert(
                AccountRoutineRecordLocalEntity(accountId = accountId, routineRecordId = Arb.kotlinUuid().next(), isDirty = isDirty),
            )
        },
    )

class SyncPendingLocalDataSourceImplTest :
    FunSpec({
        test("업로드 대기 확인이 동기화 대상 스물다섯 종류를 모두 다룬다") {
            pendingWriterMap.size shouldBe 25
        }

        lateinit var database: DiaryDatabase
        lateinit var dataSource: SyncPendingLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = SyncPendingLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        test("TC-c5cbef72-54f8-4e27-addd-90d64fcaa210 동기화 대상 스물다섯 종류 중 어느 한 종류만 업로드 대기여도 대기 항목이 있다고 알린다") {
            pendingWriterMap.forEach { (kind, write) ->
                val accountId = Arb.kotlinUuid().next()

                write(database, accountId, true)

                withClue(kind) { dataSource.hasPending(accountId = accountId).first() shouldBe true }
            }
        }

        test("모든 종류가 업로드 대기가 아니면 대기 항목이 없다고 알린다") {
            val accountId = Arb.kotlinUuid().next()

            pendingWriterMap.forEach { (_, write) -> write(database, accountId, false) }

            dataSource.hasPending(accountId = accountId).first() shouldBe false
        }

        test("다른 계정의 업로드 대기 항목은 대기 항목으로 보지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()

            pendingWriterMap.forEach { (_, write) -> write(database, otherAccountId, true) }

            dataSource.hasPending(accountId = accountId).first() shouldBe false
        }

        test("아무 항목도 없으면 대기 항목이 없다고 알린다") {
            dataSource.hasPending(accountId = Arb.kotlinUuid().next()).first() shouldBe false
        }

        test("TC-d6a9c2f7-0d4b-4475-ac62-dce3d62f6f20 데이터베이스를 닫고 다시 열어도 업로드 대기 상태가 그대로 조회된다") {
            val file = File.createTempFile("sync-pending", ".db")

            try {
                pendingWriterMap.forEach { (kind, write) ->
                    val accountId = Arb.kotlinUuid().next()
                    val firstDatabase = fileDatabase(file)
                    write(firstDatabase, accountId, true)
                    firstDatabase.close()

                    val secondDatabase = fileDatabase(file)

                    try {
                        withClue(kind) {
                            SyncPendingLocalDataSourceImpl(database = secondDatabase).hasPending(accountId = accountId).first() shouldBe true
                        }
                    } finally {
                        secondDatabase.close()
                    }
                }
            } finally {
                file.delete()
            }
        }
    })

private fun fileDatabase(file: File): DiaryDatabase =
    Room
        .databaseBuilder<DiaryDatabase>(name = file.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .build()
