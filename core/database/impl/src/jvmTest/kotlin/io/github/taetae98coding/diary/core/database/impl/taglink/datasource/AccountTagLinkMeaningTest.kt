package io.github.taetae98coding.diary.core.database.impl.taglink.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entitytag.transaction.AccountEntityTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.music.transaction.AccountMusicTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.core.testing.web.localWeb
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagLinkMeaningTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var tagLinkTransaction: AccountTagLinkTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            tagTransaction = AccountTagTransactionImpl(database = database)
            tagLinkTransaction = AccountTagLinkTransactionImpl(database = database)
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

        suspend fun link(
            accountId: Uuid,
            fromTagId: Uuid,
            toTagId: Uuid,
        ) {
            tagLinkTransaction.upsert(
                accountId = accountId,
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )
        }

        suspend fun topLevelTagIdList(accountId: Uuid): List<Uuid> {
            val result =
                database
                    .accountTagDao()
                    .pageTopLevel(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>().data.map { tag -> tag.id }
        }

        test("TC-87c521ec-13a9-459d-b94d-ddb6823c5a3c 태그를 연결해도 메모에 연결된 태그는 늘어나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val fromTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            val toTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, fromTag, toTag)
            val memo = Arb.localMemo(isFinished = false, isDeleted = false, primaryTagId = null).next()
            database.withWriteTransaction {
                database.memoDao().upsert(listOf(memo))
                database.accountMemoDao().upsert(listOf(AccountMemoLocalEntity(accountId = accountId, memoId = memo.id, isDirty = true)))
            }
            AccountMemoTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            AccountMemoTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, memoId = memo.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }

        test("TC-87c521ec-13a9-459d-b94d-ddb6823c5a3c 태그를 연결해도 웹 항목에 연결된 태그는 늘어나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val fromTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            val toTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, fromTag, toTag)
            val web = Arb.localWeb(isDeleted = false).next()
            AccountWebTransactionImpl(database = database).upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.WEB,
                entityId = web.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.WEB, entityId = web.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }

        test("TC-87c521ec-13a9-459d-b94d-ddb6823c5a3c 태그를 연결해도 장소에 연결된 태그는 늘어나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val fromTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            val toTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, fromTag, toTag)
            val place = Arb.localPlace(isDeleted = false).next()
            AccountPlaceTransactionImpl(database = database).upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            AccountEntityTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                kind = EntityTagKindLocalEntity.PLACE,
                entityId = place.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            AccountEntityTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.PLACE, entityId = place.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }

        test("TC-87c521ec-13a9-459d-b94d-ddb6823c5a3c 태그를 연결해도 연락처, 체크리스트, 곡, QR에 연결된 태그는 늘어나지 않는다") {
            val insertEntityList: List<Pair<EntityTagKindLocalEntity, suspend (Uuid) -> Uuid>> =
                listOf(
                    EntityTagKindLocalEntity.CONTACT to { accountId ->
                        val contact = Arb.contactLocalEntity().next().copy(isDeleted = false)
                        AccountContactTransactionImpl(database = database).upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
                        contact.id
                    },
                    EntityTagKindLocalEntity.CHECKLIST to { accountId ->
                        val checklist = Arb.checklistLocalEntity().next().copy(isDeleted = false)
                        AccountChecklistTransactionImpl(database = database).upsert(
                            accountId = accountId,
                            checklist = checklist,
                            itemList = emptyList(),
                            checklistTagList = emptyList(),
                        )
                        checklist.id
                    },
                    EntityTagKindLocalEntity.MUSIC to { accountId ->
                        val music = Arb.musicLocalEntity().next().copy(isDeleted = false)
                        AccountMusicTransactionImpl(database = database).upsert(accountId = accountId, musicList = listOf(music), musicTagList = emptyList())
                        music.id
                    },
                    EntityTagKindLocalEntity.QR to { accountId ->
                        val qr = Arb.qrLocalEntity().next().copy(isDeleted = false)
                        AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr), qrTagList = emptyList())
                        qr.id
                    },
                )

            insertEntityList.forEach { (kind, insertEntity) ->
                val accountId = Arb.kotlinUuid().next()
                val fromTag = Arb.localTag(isFinished = false, isDeleted = false).next()
                val toTag = Arb.localTag(isFinished = false, isDeleted = false).next()
                insertTag(accountId, fromTag, toTag)
                val entityId = insertEntity(accountId)
                AccountEntityTagTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    kind = kind,
                    entityId = entityId,
                    tagId = toTag.id,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                )

                link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

                AccountEntityTagLocalDataSourceImpl(database = database)
                    .getTagList(accountId = accountId, kind = kind, entityId = entityId)
                    .first()
                    .map { tag -> tag.id } shouldBe listOf(toTag.id)
            }
        }

        test("TC-b6500fde-cc1e-437b-a1fc-5e353c42f27a 기기에 없던 출발 태그가 반영되면 향해 오는 연결의 존재 판정과 연결된 태그 조회에 함께 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val fromTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            val toTag = Arb.localTag(isFinished = false, isDeleted = false).next()
            insertTag(accountId, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(toTag.id)

            insertTag(accountId, fromTag)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id)
            AccountTagLinkLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, fromTagId = fromTag.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }
    })
