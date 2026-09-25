package io.github.taetae98coding.diary.core.database.impl.taglink.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.place.transaction.AccountPlaceTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.placetag.datasource.AccountPlaceTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.placetag.transaction.AccountPlaceTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.web.transaction.AccountWebTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.webtag.datasource.AccountWebTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.webtag.transaction.AccountWebTagTransactionImpl
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.core.testing.web.localWeb
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
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

        test("TC-TAG-LINK-DOMAIN-028 태그를 연결해도 메모에 연결된 태그는 늘어나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            val toTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            insertTag(accountId, fromTag, toTag)
            val memo = fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null)
            database.withWriteTransaction {
                database.memoDao().upsert(listOf(memo))
                database.accountMemoDao().upsert(listOf(AccountMemoLocalEntity(accountId = accountId, memoId = memo.id, isDirty = true)))
            }
            AccountMemoTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            AccountMemoTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, memoId = memo.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-028 태그를 연결해도 웹 항목에 연결된 태그는 늘어나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            val toTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            insertTag(accountId, fromTag, toTag)
            val web = fixtureMonkey.localWeb(isDeleted = false)
            AccountWebTransactionImpl(database = database).upsert(accountId = accountId, webList = listOf(web), webTagList = emptyList())
            AccountWebTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                webId = web.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            AccountWebTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, webId = web.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-028 태그를 연결해도 장소에 연결된 태그는 늘어나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            val toTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            insertTag(accountId, fromTag, toTag)
            val place = fixtureMonkey.localPlace(isDeleted = false)
            AccountPlaceTransactionImpl(database = database).upsert(accountId = accountId, placeList = listOf(place), placeTagList = emptyList())
            AccountPlaceTagTransactionImpl(database = database).upsert(
                accountId = accountId,
                placeId = place.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            AccountPlaceTagLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, placeId = place.id)
                .first()
                .map { tag -> tag.id } shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DATA-010 기기에 없던 출발 태그가 반영되면 향해 오는 연결의 존재 판정과 연결된 태그 조회에 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
            val toTag = fixtureMonkey.localTag(isFinished = false, isDeleted = false)
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
