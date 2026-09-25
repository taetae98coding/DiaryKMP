package io.github.taetae98coding.diary.core.database.impl.sync.datasource

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

private typealias PendingWriter = suspend (DiaryDatabase, Uuid, Boolean) -> Unit

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private fun uuid(): Uuid = fixtureMonkey.giveMeOne<Uuid>()

private val pendingWriterMap: Map<String, PendingWriter> =
    mapOf(
        "태그" to { database, accountId, isDirty ->
            database.accountTagSyncDao().upsert(AccountTagLocalEntity(accountId = accountId, tagId = uuid(), isDirty = isDirty))
        },
        "장소" to { database, accountId, isDirty ->
            database.accountPlaceSyncDao().upsert(AccountPlaceLocalEntity(accountId = accountId, placeId = uuid(), isDirty = isDirty))
        },
        "웹 항목" to { database, accountId, isDirty ->
            database.accountWebSyncDao().upsert(AccountWebLocalEntity(accountId = accountId, webId = uuid(), isDirty = isDirty))
        },
        "연락처" to { database, accountId, isDirty ->
            database.accountContactSyncDao().upsert(AccountContactLocalEntity(accountId = accountId, contactId = uuid(), isDirty = isDirty))
        },
        "곡" to { database, accountId, isDirty ->
            database.accountMusicSyncDao().upsert(AccountMusicLocalEntity(accountId = accountId, musicId = uuid(), isDirty = isDirty))
        },
        "메모" to { database, accountId, isDirty ->
            database.accountMemoSyncDao().upsert(AccountMemoLocalEntity(accountId = accountId, memoId = uuid(), isDirty = isDirty))
        },
        "메모와 태그의 연결" to { database, accountId, isDirty ->
            database.accountMemoTagSyncDao().upsert(
                AccountMemoTagLocalEntity(accountId = accountId, memoId = uuid(), tagId = uuid(), isDirty = isDirty),
            )
        },
        "메모와 장소의 연결" to { database, accountId, isDirty ->
            database.accountMemoPlaceSyncDao().upsert(
                AccountMemoPlaceLocalEntity(accountId = accountId, memoId = uuid(), placeId = uuid(), isDirty = isDirty),
            )
        },
        "메모와 웹 항목의 연결" to { database, accountId, isDirty ->
            database.accountMemoWebSyncDao().upsert(
                AccountMemoWebLocalEntity(accountId = accountId, memoId = uuid(), webId = uuid(), isDirty = isDirty),
            )
        },
        "메모와 연락처의 연결" to { database, accountId, isDirty ->
            database.accountMemoContactSyncDao().upsert(
                AccountMemoContactLocalEntity(accountId = accountId, memoId = uuid(), contactId = uuid(), isDirty = isDirty),
            )
        },
        "태그와 태그의 연결" to { database, accountId, isDirty ->
            database.accountTagLinkSyncDao().upsert(
                AccountTagLinkLocalEntity(accountId = accountId, fromTagId = uuid(), toTagId = uuid(), isDirty = isDirty),
            )
        },
        "웹 항목과 태그의 연결" to { database, accountId, isDirty ->
            database.accountWebTagSyncDao().upsert(
                AccountWebTagLocalEntity(accountId = accountId, webId = uuid(), tagId = uuid(), isDirty = isDirty),
            )
        },
        "장소와 태그의 연결" to { database, accountId, isDirty ->
            database.accountPlaceTagSyncDao().upsert(
                AccountPlaceTagLocalEntity(accountId = accountId, placeId = uuid(), tagId = uuid(), isDirty = isDirty),
            )
        },
    )

class SyncPendingLocalDataSourceImplTest :
    FunSpec({
        test("업로드 대기 확인이 동기화 대상 열세 종류를 모두 다룬다") {
            pendingWriterMap.size shouldBe 13
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

        test("TC-MORE-HOME-DOMAIN-007 동기화 대상 열세 종류 중 어느 한 종류만 업로드 대기여도 대기 항목이 있다고 알린다") {
            pendingWriterMap.forEach { (kind, write) ->
                val accountId = uuid()

                write(database, accountId, true)

                withClue(kind) { dataSource.hasPending(accountId = accountId).first() shouldBe true }
            }
        }

        test("모든 종류가 업로드 대기가 아니면 대기 항목이 없다고 알린다") {
            val accountId = uuid()

            pendingWriterMap.forEach { (_, write) -> write(database, accountId, false) }

            dataSource.hasPending(accountId = accountId).first() shouldBe false
        }

        test("다른 계정의 업로드 대기 항목은 대기 항목으로 보지 않는다") {
            val accountId = uuid()
            val otherAccountId = uuid()

            pendingWriterMap.forEach { (_, write) -> write(database, otherAccountId, true) }

            dataSource.hasPending(accountId = accountId).first() shouldBe false
        }

        test("아무 항목도 없으면 대기 항목이 없다고 알린다") {
            dataSource.hasPending(accountId = uuid()).first() shouldBe false
        }
    })
