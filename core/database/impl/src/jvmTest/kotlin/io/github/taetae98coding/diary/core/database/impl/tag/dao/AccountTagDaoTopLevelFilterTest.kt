package io.github.taetae98coding.diary.core.database.impl.tag.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagDaoTopLevelFilterTest :
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
                updatedAt = instant(),
            )
        }

        suspend fun unlink(
            accountId: Uuid,
            fromTagId: Uuid,
            toTagId: Uuid,
        ) {
            tagLinkTransaction.upsert(
                accountId = accountId,
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDeleted = true,
                updatedAt = instant(),
            )
        }

        fun topLevelPagingSource(accountId: Uuid): PagingSource<Int, TagLocalEntity> = database.accountTagDao().pageTopLevel(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)

        suspend fun topLevelTagIdList(accountId: Uuid): List<Uuid> = topLevelPagingSource(accountId = accountId).pagedTagIdList()

        suspend fun tagIdList(accountId: Uuid): List<Uuid> =
            database
                .accountTagDao()
                .page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
                .pagedTagIdList()

        test("TC-TAG-HOME-DOMAIN-007 TC-TAG-LINK-DOMAIN-017 향해 오는 연결이 없는 태그만 최상위 태그로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val isolatedTag = tag(title = "alpha")
            val toOnlyTag = tag(title = "bravo")
            val fromOnlyTag = tag(title = "charlie")
            val bothTag = tag(title = "delta")
            insertTag(accountId, isolatedTag, toOnlyTag, fromOnlyTag, bothTag)
            link(accountId = accountId, fromTagId = fromOnlyTag.id, toTagId = bothTag.id)
            link(accountId = accountId, fromTagId = bothTag.id, toTagId = toOnlyTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(isolatedTag.id, fromOnlyTag.id)
        }

        test("저장되지 않은 태그에서 온 연결은 향해 오는 연결로 세지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha")
            val toTag = tag(title = "bravo")
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            tagIdList(accountId = accountId) shouldBe listOf(fromTag.id, toTag.id)
        }

        test("TC-TAG-HOME-DOMAIN-008 여러 단계로 이어지는 연결의 중간 태그는 최상위 태그가 아니다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val rootTag = tag(title = "alpha")
            val middleTag = tag(title = "bravo")
            val leafTag = tag(title = "charlie")
            insertTag(accountId, rootTag, middleTag, leafTag)
            link(accountId = accountId, fromTagId = rootTag.id, toTagId = middleTag.id)
            link(accountId = accountId, fromTagId = middleTag.id, toTagId = leafTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(rootTag.id)
        }

        test("TC-TAG-HOME-DOMAIN-009 모든 태그가 순환에 속하면 최상위 태그가 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag(title = "alpha")
            val secondTag = tag(title = "bravo")
            val thirdTag = tag(title = "charlie")
            insertTag(accountId, firstTag, secondTag, thirdTag)
            link(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)
            link(accountId = accountId, fromTagId = secondTag.id, toTagId = thirdTag.id)
            link(accountId = accountId, fromTagId = thirdTag.id, toTagId = firstTag.id)

            topLevelTagIdList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-TAG-HOME-DOMAIN-010 최상위 태그로 좁혀도 완료되거나 삭제된 태그는 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val activeTag = tag(title = "alpha")
            val finishedTag = tag(title = "bravo").copy(isFinished = true)
            val deletedTag = tag(title = "charlie").copy(isDeleted = true)
            insertTag(accountId, activeTag, finishedTag, deletedTag)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(activeTag.id)
        }

        test("TC-TAG-HOME-DATA-006 최상위 태그도 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val charlieTag = tag(title = "charlie")
            val alphaTag = tag(title = "alpha")
            val bravoTag = tag(title = "bravo")
            val linkedTag = tag(title = "delta")
            insertTag(accountId, charlieTag, alphaTag, bravoTag, linkedTag)
            link(accountId = accountId, fromTagId = alphaTag.id, toTagId = linkedTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(alphaTag.id, bravoTag.id, charlieTag.id)
        }

        test("TC-TAG-HOME-DATA-007 연결을 만들면 페이지를 무효화하고 최상위 태그 목록에서 도착 태그를 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha")
            val toTag = tag(title = "bravo")
            insertTag(accountId, fromTag, toTag)
            val pagingSource = topLevelPagingSource(accountId = accountId)
            pagingSource.pagedTagIdList() shouldBe listOf(fromTag.id, toTag.id)

            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-018 해제한 연결은 향해 오는 연결로 세지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha")
            val toTag = tag(title = "bravo")
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            unlink(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id, toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-019 출발 태그의 상태에 따라 향해 오는 연결을 세는 기준이 달라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha")
            val toTag = tag(title = "bravo")
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id)

            insertTag(accountId, fromTag.copy(isFinished = true))
            topLevelTagIdList(accountId = accountId).shouldBeEmpty()

            insertTag(accountId, fromTag.copy(isDeleted = true))
            topLevelTagIdList(accountId = accountId) shouldBe listOf(toTag.id)

            AccountTagSyncTransactionImpl(database = database).save(
                accountId = accountId,
                tagList = listOf(fromTag),
                cursor = fixtureMonkey.giveMeOne<Long>(),
            )
            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-020 계정과 연결되지 않은 출발 태그의 연결은 세지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val toTag = tag(title = "alpha")
            val otherAccountTag = tag(title = "bravo")
            insertTag(accountId, toTag)
            insertTag(otherAccountId, otherAccountTag)
            link(accountId = accountId, fromTagId = otherAccountTag.id, toTagId = toTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-021 기기에 없는 태그에서 오는 연결은 세지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val toTag = tag(title = "alpha")
            insertTag(accountId, toTag)
            link(accountId = accountId, fromTagId = fixtureMonkey.giveMeOne<Uuid>(), toTagId = toTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-HOME-DATA-012 필터 선택을 켜 두어도 검색, 메모·태그 연결·항목의 태그 선택 목록과 태그 필터의 선택할 수 있는 태그는 좁혀지지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha")
            val toTag = tag(title = "bravo")
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)
            database.accountTagFilterDao().upsert(
                TagFilterLocalEntity(accountId = accountId, isTopLevelOnly = true),
            )

            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id)
            database
                .searchTagDao()
                .page(accountId = accountId, query = "bravo", sort = ListSortLocalEntity.DEFAULT.queryValue)
                .pagedTagIdList() shouldBe listOf(toTag.id)
            database
                .accountMemoTagDao()
                .pageSelectableTag(accountId = accountId, memoId = fixtureMonkey.giveMeOne<Uuid>(), query = "")
                .pagedTagIdList() shouldBe listOf(fromTag.id, toTag.id)
            database
                .accountTagLinkDao()
                .pageSelectableTag(accountId = accountId, fromTagId = fromTag.id, query = "")
                .pagedTagIdList() shouldBe listOf(toTag.id)
            database
                .accountTagDao()
                .page(accountId = accountId, query = "bravo", sort = ListSortLocalEntity.DEFAULT.queryValue)
                .pagedTagIdList() shouldBe listOf(toTag.id)
            tagIdList(accountId = accountId) shouldBe listOf(fromTag.id, toTag.id)
        }

        test("TC-TAG-FINISHED-LIST-DOMAIN-009 최상위 태그 필터를 켜 두어도 완료된 태그 목록은 좁혀지지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha").copy(isFinished = true)
            val toTag = tag(title = "bravo").copy(isFinished = true)
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)
            database.accountTagFilterDao().upsert(
                TagFilterLocalEntity(accountId = accountId, isTopLevelOnly = true),
            )

            database
                .accountTagDao()
                .pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .pagedTagIdList() shouldBe listOf(fromTag.id, toTag.id)
        }

        test("다른 계정의 연결은 최상위 태그 판정에 쓰지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "alpha")
            val toTag = tag(title = "bravo")
            insertTag(accountId, fromTag, toTag)
            insertTag(otherAccountId, fromTag, toTag)
            link(accountId = otherAccountId, fromTagId = fromTag.id, toTagId = toTag.id)

            topLevelTagIdList(accountId = accountId) shouldBe listOf(fromTag.id, toTag.id)
            topLevelTagIdList(accountId = otherAccountId) shouldBe listOf(fromTag.id)
        }
    }) {
    public companion object {
        private const val INVALIDATION_TIMEOUT_MILLIS: Long = 5_000

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private suspend fun PagingSource<Int, TagLocalEntity>.pagedTagIdList(): List<Uuid> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>()
                .data
                .map { tag -> tag.id }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun tag(title: String): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>().copy(title = title))
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
    }
}
