package io.github.taetae98coding.diary.core.database.impl.taglink.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.transaction.AccountMemoTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.transaction.AccountTagLinkTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagLinkLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountTagLinkLocalDataSourceImpl
        lateinit var tagLinkTransaction: AccountTagLinkTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var memoTagTransaction: AccountMemoTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountTagLinkLocalDataSourceImpl(database = database)
            tagLinkTransaction = AccountTagLinkTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            memoTransaction = AccountMemoTransactionImpl(database = database)
            memoTagTransaction = AccountMemoTagTransactionImpl(database = database)
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

        suspend fun linkedTagList(
            accountId: Uuid,
            fromTagId: Uuid,
        ): List<TagLocalEntity> =
            dataSource
                .getTagList(
                    accountId = accountId,
                    fromTagId = fromTagId,
                ).first()

        suspend fun linkedTagIdList(
            accountId: Uuid,
            fromTagId: Uuid,
        ): List<Uuid> = linkedTagList(accountId = accountId, fromTagId = fromTagId).map { tag -> tag.id }

        suspend fun selectableTagIdList(
            accountId: Uuid,
            fromTagId: Uuid,
            query: String = "",
        ): List<Uuid> =
            dataSource
                .pageSelectableTag(
                    accountId = accountId,
                    fromTagId = fromTagId,
                    query = query,
                ).pagedTagIdList()

        test("TC-TAG-LINK-DOMAIN-001 한 태그에서 여러 태그로 향하는 연결을 만들 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val firstToTag = tag()
            val secondToTag = tag()
            insertTag(accountId, fromTag, firstToTag, secondToTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = firstToTag.id)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = secondToTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldContainExactlyInAnyOrder
                listOf(firstToTag.id, secondToTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-002 여러 태그가 같은 태그를 향하는 연결을 가질 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstFromTag = tag()
            val secondFromTag = tag()
            val toTag = tag()
            insertTag(accountId, firstFromTag, secondFromTag, toTag)

            link(accountId = accountId, fromTagId = firstFromTag.id, toTagId = toTag.id)
            link(accountId = accountId, fromTagId = secondFromTag.id, toTagId = toTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = firstFromTag.id) shouldBe listOf(toTag.id)
            linkedTagIdList(accountId = accountId, fromTagId = secondFromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-003 같은 방향의 연결은 중복되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-004 반대 방향의 연결은 별개의 연결이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            insertTag(accountId, firstTag, secondTag)

            link(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)
            link(accountId = accountId, fromTagId = secondTag.id, toTagId = firstTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = firstTag.id) shouldBe listOf(secondTag.id)
            linkedTagIdList(accountId = accountId, fromTagId = secondTag.id) shouldBe listOf(firstTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-005 한 방향을 해제해도 반대 방향은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            insertTag(accountId, firstTag, secondTag)
            link(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)
            link(accountId = accountId, fromTagId = secondTag.id, toTagId = firstTag.id)

            unlink(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)

            linkedTagList(accountId = accountId, fromTagId = firstTag.id).shouldBeEmpty()
            linkedTagIdList(accountId = accountId, fromTagId = secondTag.id) shouldBe listOf(firstTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-007 계정과 연결되지 않은 태그는 연결된 태그로 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val accountTag = tag()
            val otherAccountTag = tag()
            insertTag(accountId, accountTag)
            insertTag(otherAccountId, otherAccountTag)

            link(accountId = accountId, fromTagId = accountTag.id, toTagId = otherAccountTag.id)
            link(accountId = accountId, fromTagId = otherAccountTag.id, toTagId = accountTag.id)

            linkedTagList(accountId = accountId, fromTagId = accountTag.id).shouldBeEmpty()
            linkedTagList(accountId = accountId, fromTagId = otherAccountTag.id).shouldBeEmpty()
        }

        test("TC-TAG-LINK-DOMAIN-008 연결은 태그의 내용을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            database.accountTagDao().find(accountId = accountId, tagId = fromTag.id).first() shouldBe fromTag
            database.accountTagDao().find(accountId = accountId, tagId = toTag.id).first() shouldBe toTag
        }

        test("TC-TAG-LINK-DOMAIN-009 연결은 태그별 메모 조회 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)
            val memo = memo()
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            memoTagTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                tagId = toTag.id,
                isDeleted = false,
                updatedAt = instant(),
            )

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            database
                .accountTagMemoDao()
                .page(
                    accountId = accountId,
                    tagId = fromTag.id,
                    scope = TagScopeLocalEntity.SELF.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIdList()
                .shouldBeEmpty()
            database
                .accountTagMemoDao()
                .page(
                    accountId = accountId,
                    tagId = toTag.id,
                    scope = TagScopeLocalEntity.SELF.queryValue,
                    sort = ListSortLocalEntity.DEFAULT.queryValue,
                ).pagedIdList() shouldBe listOf(memo.id)
        }

        test("TC-TAG-LINK-DOMAIN-010 순환하는 연결도 만들 수 있다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag()
            val secondTag = tag()
            val thirdTag = tag()
            insertTag(accountId, firstTag, secondTag, thirdTag)

            link(accountId = accountId, fromTagId = firstTag.id, toTagId = secondTag.id)
            link(accountId = accountId, fromTagId = secondTag.id, toTagId = thirdTag.id)
            link(accountId = accountId, fromTagId = thirdTag.id, toTagId = firstTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = firstTag.id) shouldBe listOf(secondTag.id)
            linkedTagIdList(accountId = accountId, fromTagId = secondTag.id) shouldBe listOf(thirdTag.id)
            linkedTagIdList(accountId = accountId, fromTagId = thirdTag.id) shouldBe listOf(firstTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-011 연결을 해제하면 조회되지 않고 다시 만들면 복구된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            unlink(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)
            linkedTagList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)
            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-012 태그를 완료하거나 삭제해도 연결은 해제되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = fromTag.id, isFinished = true, updatedAt = instant())
            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)

            tagTransaction.updateDeleted(accountId = accountId, tagId = fromTag.id, isDeleted = true, updatedAt = instant())
            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = toTag.id, isFinished = true, updatedAt = instant())
            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-013 삭제된 도착 태그는 조회되지 않고 다른 기기에서 받은 내용으로 삭제가 풀리면 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            val deletedAt = instant()
            tagTransaction.updateDeleted(accountId = accountId, tagId = toTag.id, isDeleted = true, updatedAt = deletedAt)
            linkedTagList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()

            AccountTagSyncTransactionImpl(database = database).save(
                accountId = accountId,
                tagList = listOf(toTag.copy(isDeleted = false, updatedAt = deletedAt)),
                cursor = fixtureMonkey.giveMeOne<Long>(),
            )
            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-014 완료된 도착 태그는 연결된 태그로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            tagTransaction.updateFinished(accountId = accountId, tagId = toTag.id, isFinished = true, updatedAt = instant())

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-015 연결된 태그는 제목 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val firstTag = tag(title = "a")
            val secondTag = tag(title = "b")
            val thirdTag = tag(title = "c")
            insertTag(accountId, fromTag, thirdTag, firstTag, secondTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = thirdTag.id)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = firstTag.id)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = secondTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe
                listOf(firstTag.id, secondTag.id, thirdTag.id)
        }

        test("TC-TAG-LINK-DOMAIN-016 기기에 없는 태그를 가리키는 연결은 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            insertTag(accountId, fromTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = fixtureMonkey.giveMeOne<Uuid>())

            linkedTagList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()
        }

        test("TC-TAG-LINK-DATA-009 가리키는 태그가 기기에 없는 연결도 저장은 성공한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()

            insertTag(accountId, toTag)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(toTag.id)
        }

        test("TC-TAG-LINK-DATA-001 연결 해제는 같은 출발 태그의 다른 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val removedTag = tag()
            val keptTag = tag()
            insertTag(accountId, fromTag, removedTag, keptTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = removedTag.id)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = keptTag.id)

            unlink(accountId = accountId, fromTagId = fromTag.id, toTagId = removedTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(keptTag.id)
        }

        test("TC-TAG-DETAIL-DOMAIN-006 연결된 완료된 태그는 조회되고 삭제된 태그는 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            val deletedTag = tag()
            insertTag(accountId, fromTag, finishedTag, deletedTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = finishedTag.id)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = deletedTag.id)

            insertTag(accountId, deletedTag.copy(isDeleted = true))

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(finishedTag.id)
        }

        test("TC-TAG-DETAIL-DOMAIN-009 연결과 해제는 태그의 내용과 상태를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag()
            insertTag(accountId, fromTag, toTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)
            unlink(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            database.accountTagDao().find(accountId = accountId, tagId = fromTag.id).first() shouldBe fromTag
            database.accountTagDao().find(accountId = accountId, tagId = toTag.id).first() shouldBe toTag
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-001 연결할 수 있는 태그는 계정과 연결된 미완료·미삭제 태그다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val selectableTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            val deletedTag = tag().copy(isDeleted = true)
            val otherAccountTag = tag()
            insertTag(accountId, fromTag, selectableTag, finishedTag, deletedTag)
            insertTag(otherAccountId, otherAccountTag)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(selectableTag.id)
        }

        test("TC-TAG-DETAIL-DOMAIN-007 출발 태그 자신은 연결할 수 있는 태그에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val otherTag = tag()
            insertTag(accountId, fromTag, otherTag)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(otherTag.id)
        }

        test("TC-TAG-DETAIL-DOMAIN-008 연결된 완료된 태그는 연결할 수 있는 태그와 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val linkedFinishedTag = tag().copy(isFinished = true)
            val unlinkedFinishedTag = tag().copy(isFinished = true)
            insertTag(accountId, fromTag, linkedFinishedTag, unlinkedFinishedTag)

            link(accountId = accountId, fromTagId = fromTag.id, toTagId = linkedFinishedTag.id)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(linkedFinishedTag.id)
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-005 연결할 수 있는 태그에 없는 연결된 태그도 연결할 수 있는 태그와 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "delta")
            val selectableTag = tag(title = "alpha")
            val linkedFinishedTag = tag(title = "bravo").copy(isFinished = true)
            insertTag(accountId, fromTag, selectableTag, linkedFinishedTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = linkedFinishedTag.id)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(selectableTag.id, linkedFinishedTag.id)
        }

        test("연결을 해제한 완료된 태그는 다시 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            insertTag(accountId, fromTag, finishedTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = finishedTag.id)

            unlink(accountId = accountId, fromTagId = fromTag.id, toTagId = finishedTag.id)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-002 연결할 수 있는 태그는 완료 여부와 관계없이 제목 오름차순으로 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "delta")
            val alphaTag = tag(title = "alpha")
            val bravoTag = tag(title = "bravo").copy(isFinished = true)
            val charlieTag = tag(title = "charlie")
            insertTag(accountId, fromTag, charlieTag, alphaTag, bravoTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = bravoTag.id)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe
                listOf(alphaTag.id, bravoTag.id, charlieTag.id)
        }

        test("TC-TAG-LINK-INPUT-DATA-003 저장된 태그가 바뀌면 연결된 태그와 연결할 수 있는 태그에 함께 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val toTag = tag(title = "alpha")
            insertTag(accountId, fromTag, toTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = toTag.id)

            val renamedTag = toTag.copy(detail = toTag.detail.copy(title = "bravo"))
            insertTag(accountId, renamedTag)

            linkedTagList(accountId = accountId, fromTagId = fromTag.id).map { tag -> tag.detail.title } shouldBe listOf("bravo")
            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(renamedTag.id)
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-006 연결할 수 없는 태그의 연결을 해제하면 연결과 목록에서 함께 사라진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag()
            val finishedTag = tag().copy(isFinished = true)
            insertTag(accountId, fromTag, finishedTag)
            link(accountId = accountId, fromTagId = fromTag.id, toTagId = finishedTag.id)

            unlink(accountId = accountId, fromTagId = fromTag.id, toTagId = finishedTag.id)

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()
            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id).shouldBeEmpty()
        }

        test("TC-TAG-LINK-INPUT-DOMAIN-007 검색어는 이모지·제목·설명 중 하나 이상을 포함하는 태그만 남긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "FromTag")
            val titleTag = tag(title = "Travel").withDetail(emoji = "", description = "")
            val emojiTag = tag(title = "AlphaTag").withDetail(emoji = "✈️", description = "")
            val descriptionTag = tag(title = "BetaTag").withDetail(emoji = "", description = "여행 기록")
            val otherTag = tag(title = "GammaTag").withDetail(emoji = "", description = "")
            insertTag(accountId, fromTag, titleTag, emojiTag, descriptionTag, otherTag)

            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id, query = "trav") shouldBe listOf(titleTag.id)
            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id, query = "✈️") shouldBe listOf(emojiTag.id)
            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id, query = "여행") shouldBe listOf(descriptionTag.id)
            selectableTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe
                listOf(emojiTag.id, descriptionTag.id, otherTag.id, titleTag.id)
        }

        test("TC-TAG-LINK-INPUT-DATA-004 검색어를 만족하는 태그가 정렬 뒤쪽에 있어도 첫 페이지에 담긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTag = tag(title = "AAAFromTag")
            val tagList =
                List(SELECTABLE_TAG_COUNT) { index ->
                    tag(title = "Tag-${index.toString().padStart(length = 3, padChar = '0')}").withDetail(emoji = "", description = "")
                }
            val target = tag(title = "ZebraTravel").withDetail(emoji = "", description = "")
            insertTag(accountId, fromTag, *tagList.toTypedArray(), target)

            dataSource
                .pageSelectableTag(accountId = accountId, fromTagId = fromTag.id, query = "travel")
                .pagedTagIdList(loadSize = SELECTABLE_TAG_PAGE_SIZE) shouldBe listOf(target.id)
        }

        test("TC-TAG-ADD-DATA-004 태그와 그 태그가 출발하는 연결을 함께 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstToTag = tag()
            val secondToTag = tag()
            val unlinkedTag = tag()
            insertTag(accountId, firstToTag, secondToTag, unlinkedTag)
            val fromTag = tag()

            tagTransaction.upsert(
                accountId = accountId,
                tagList = listOf(fromTag),
                tagLinkList =
                    listOf(firstToTag.id, secondToTag.id).map { toTagId ->
                        TagLinkLocalEntity(
                            fromTagId = fromTag.id,
                            toTagId = toTagId,
                            isDeleted = false,
                            updatedAt = fromTag.updatedAt,
                            createdAt = fromTag.createdAt,
                        )
                    },
            )

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldContainExactlyInAnyOrder
                listOf(firstToTag.id, secondToTag.id)
        }

        test("TC-TAG-ADD-DATA-005 완료된 태그를 향하는 연결도 태그와 함께 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedTag = tag().copy(isFinished = true)
            insertTag(accountId, finishedTag)
            val fromTag = tag()

            tagTransaction.upsert(
                accountId = accountId,
                tagList = listOf(fromTag),
                tagLinkList =
                    listOf(
                        TagLinkLocalEntity(
                            fromTagId = fromTag.id,
                            toTagId = finishedTag.id,
                            isDeleted = false,
                            updatedAt = fromTag.updatedAt,
                            createdAt = fromTag.createdAt,
                        ),
                    ),
            )

            linkedTagIdList(accountId = accountId, fromTagId = fromTag.id) shouldBe listOf(finishedTag.id)
        }
    }) {
    public companion object {
        private const val SELECTABLE_TAG_COUNT: Int = 25
        private const val SELECTABLE_TAG_PAGE_SIZE: Int = 10

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private suspend fun PagingSource<Int, MemoLocalEntity>.pagedIdList(): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 100,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, MemoLocalEntity>>().data.map { memo -> memo.id }
        }

        private suspend fun PagingSource<Int, TagLocalEntity>.pagedTagIdList(loadSize: Int = 100): List<Uuid> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = loadSize,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>().data.map { tag -> tag.id }
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun tag(title: String = "title-${fixtureMonkey.giveMeOne<String>()}"): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()
                .let { tag ->
                    tag.copy(
                        detail = tag.detail.copy(title = title),
                        isFinished = false,
                        isDeleted = false,
                    )
                }

        private fun TagLocalEntity.withDetail(
            emoji: String,
            description: String,
        ): TagLocalEntity = copy(detail = detail.copy(emoji = emoji, description = description))

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()
                .copy(
                    detail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
                    primaryTagId = null,
                    isFinished = false,
                    isDeleted = false,
                )
    }
}
