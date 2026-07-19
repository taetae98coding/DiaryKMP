package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagDaoTest :
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

        suspend fun insert(
            accountId: Uuid,
            vararg tagList: TagLocalEntity,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(tagList.toList())
                database.accountTagDao().upsert(
                    tagList.map { tag ->
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = true,
                        )
                    },
                )
            }
        }

        suspend fun tagList(accountId: Uuid): List<TagLocalEntity> =
            database
                .accountTagDao()
                .page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadPage()
                .data

        suspend fun tagIdList(accountId: Uuid): List<Uuid> = tagList(accountId = accountId).map { tag -> tag.id }

        suspend fun assertPageInvalidated(
            pagingSource: PagingSource<Int, TagLocalEntity>,
            accountId: Uuid,
            expectedIdList: List<Uuid>,
            change: suspend () -> Unit,
        ) {
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
            tagIdList(accountId) shouldBe expectedIdList
        }

        test("TC-TAG-HOME-DATA-001 TC-MEMO-HOME-DOMAIN-007 현재 계정과 연결된 미완료·미삭제 태그만 목록에서 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val activeTag = tag(isFinished = false, isDeleted = false)
            val finishedTag = tag(isFinished = true, isDeleted = false)
            val deletedTag = tag(isFinished = false, isDeleted = true)
            val otherAccountTag = tag(isFinished = false, isDeleted = false)
            insert(accountId, activeTag, finishedTag, deletedTag)
            insert(otherAccountId, otherAccountTag)

            tagIdList(accountId) shouldBe listOf(activeTag.id)
        }

        test("TC-TAG-HOME-DATA-002 TC-MEMO-HOME-DOMAIN-007 수정 시각과 생성 시각에 관계없이 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val bravoTag =
                tag(
                    title = "Bravo",
                    updatedAt = Instant.fromEpochMilliseconds(3_000),
                    createdAt = Instant.fromEpochMilliseconds(3_000),
                )
            val alphaTag =
                tag(
                    title = "Alpha",
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                    createdAt = Instant.fromEpochMilliseconds(1_000),
                )
            val charlieTag =
                tag(
                    title = "Charlie",
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                    createdAt = Instant.fromEpochMilliseconds(2_000),
                )
            insert(accountId, bravoTag, alphaTag, charlieTag)

            tagIdList(accountId) shouldBe
                listOf(
                    alphaTag.id,
                    bravoTag.id,
                    charlieTag.id,
                )
        }

        test("TC-TAG-HOME-DOMAIN-006 이모지는 목록 정렬에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val alphaTag = tag(title = "Alpha", emoji = "\uD83E\uDD8A")
            val bravoTag = tag(title = "Bravo", emoji = "\uD83C\uDF4E")
            val charlieTag = tag(title = "Charlie", emoji = "")
            insert(accountId, charlieTag, bravoTag, alphaTag)

            tagIdList(accountId) shouldBe
                listOf(
                    alphaTag.id,
                    bravoTag.id,
                    charlieTag.id,
                )
        }

        test("TC-TAG-HOME-DATA-003 태그 추가는 페이지를 무효화하고 목록에 새 정렬로 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val initialTag = tag(title = "Bravo")
            val addedTag = tag(title = "Alpha")
            insert(accountId, initialTag)
            val pagingSource = database.accountTagDao().page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.loadPage().data shouldBe listOf(initialTag)

            assertPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                expectedIdList = listOf(addedTag.id, initialTag.id),
            ) {
                insert(accountId, addedTag)
            }
        }

        test("TC-TAG-HOME-DATA-003 제목 변경은 페이지를 무효화하고 태그를 변경된 정렬 위치로 옮긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val anchorTag = tag(title = "Bravo")
            val movingTag = tag(title = "Alpha")
            insert(accountId, anchorTag, movingTag)
            val pagingSource = database.accountTagDao().page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.loadPage().data shouldBe listOf(movingTag, anchorTag)

            assertPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                expectedIdList = listOf(anchorTag.id, movingTag.id),
            ) {
                insert(accountId, movingTag.copy(detail = movingTag.detail.copy(title = "Charlie")))
            }
        }

        test("TC-TAG-HOME-DATA-003 완료는 페이지를 무효화하고 태그를 목록에서 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remainingTag = tag(title = "Alpha")
            val finishingTag = tag(title = "Bravo")
            insert(accountId, remainingTag, finishingTag)
            val pagingSource = database.accountTagDao().page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.loadPage().data shouldBe listOf(remainingTag, finishingTag)

            assertPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                expectedIdList = listOf(remainingTag.id),
            ) {
                database.accountTagDao().updateFinished(
                    accountId = accountId,
                    tagId = finishingTag.id,
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                )
            }
        }

        test("TC-TAG-HOME-DATA-005 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val expectedTagList =
                List(TAG_COUNT) { index ->
                    tag(title = "Tag-${index.toString().padStart(length = 3, padChar = '0')}")
                }

            insert(accountId, *expectedTagList.reversed().toTypedArray())

            val firstPage = database.accountTagDao().page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue).loadPage(loadSize = PAGE_SIZE)

            firstPage.data shouldBe expectedTagList.take(PAGE_SIZE)
            firstPage.nextKey shouldBe PAGE_SIZE

            val secondPage =
                database
                    .accountTagDao()
                    .page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
                    .loadPage(key = firstPage.nextKey, loadSize = PAGE_SIZE)

            secondPage.data shouldBe expectedTagList.drop(PAGE_SIZE).take(PAGE_SIZE)
        }

        test("단일 태그 조회는 해당 계정과 연결된 태그만 반환한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            insert(accountId, tag)

            database
                .accountTagDao()
                .find(accountId = accountId, tagId = tag.id)
                .first() shouldBe tag
            database
                .accountTagDao()
                .find(accountId = otherAccountId, tagId = tag.id)
                .first()
                .shouldBeNull()
            database
                .accountTagDao()
                .find(accountId = accountId, tagId = fixtureMonkey.giveMeOne<Uuid>())
                .first()
                .shouldBeNull()
        }

        test("완료 갱신은 해당 계정과 연결된 태그에만 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(isFinished = false)
            insert(accountId, tag)

            database.accountTagDao().updateFinished(
                accountId = otherAccountId,
                tagId = tag.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            tagIdList(accountId) shouldBe listOf(tag.id)

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            tagIdList(accountId).shouldBeEmpty()
        }

        test("삭제 갱신은 해당 계정과 연결된 태그에만 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(isDeleted = false)
            insert(accountId, tag)

            database.accountTagDao().updateDeleted(
                accountId = otherAccountId,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            tagIdList(accountId) shouldBe listOf(tag.id)

            database.accountTagDao().updateDeleted(
                accountId = accountId,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            tagIdList(accountId).shouldBeEmpty()
        }

        test("완료를 되돌리면 태그가 제목에 해당하는 자리에서 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag(title = "Alpha", updatedAt = Instant.fromEpochMilliseconds(1_000))
            val secondTag = tag(title = "Bravo", isFinished = true, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insert(accountId, firstTag, secondTag)

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = secondTag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(500),
            )

            tagIdList(accountId) shouldBe listOf(firstTag.id, secondTag.id)
        }

        test("삭제를 되돌리면 태그가 제목에 해당하는 자리에서 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag(title = "Bravo", updatedAt = Instant.fromEpochMilliseconds(1_000))
            val secondTag = tag(title = "Alpha", isDeleted = true, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insert(accountId, firstTag, secondTag)

            database.accountTagDao().updateDeleted(
                accountId = accountId,
                tagId = secondTag.id,
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(500),
            )

            tagIdList(accountId) shouldBe listOf(secondTag.id, firstTag.id)
        }

        test("상세 갱신은 해당 계정의 태그 제목, 설명, 컬러, 수정 시각만 바꾸고 나머지 속성은 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insert(accountId, tag)
            val newDetail = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()

            database.accountTagDao().updateDetail(
                accountId = otherAccountId,
                tagId = tag.id,
                emoji = newDetail.emoji,
                title = newDetail.title,
                description = newDetail.description,
                color = newDetail.color,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            database.accountTagDao().find(accountId = accountId, tagId = tag.id).first() shouldBe tag

            database.accountTagDao().updateDetail(
                accountId = accountId,
                tagId = tag.id,
                emoji = newDetail.emoji,
                title = newDetail.title,
                description = newDetail.description,
                color = newDetail.color,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            val updated =
                database
                    .accountTagDao()
                    .find(accountId = accountId, tagId = tag.id)
                    .first()
                    .shouldNotBeNull()
            updated.detail shouldBe newDetail
            updated.updatedAt shouldBe Instant.fromEpochMilliseconds(2_000)
            updated.id shouldBe tag.id
            updated.isFinished shouldBe tag.isFinished
            updated.isDeleted shouldBe tag.isDeleted
            updated.createdAt shouldBe tag.createdAt
        }

        test("여러 계정과 연결된 태그는 같은 내용을 공유한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insert(accountId, tag)
            insert(otherAccountId, tag)
            val newDetail = fixtureMonkey.giveMeOne<TagDetailLocalEntity>()
            val updatedAt = Instant.fromEpochMilliseconds(2_000)

            database.accountTagDao().updateDetail(
                accountId = accountId,
                tagId = tag.id,
                emoji = newDetail.emoji,
                title = newDetail.title,
                description = newDetail.description,
                color = newDetail.color,
                updatedAt = updatedAt,
            ) shouldBe 1

            val expected = tag.copy(detail = newDetail, updatedAt = updatedAt)
            database.accountTagDao().find(accountId = accountId, tagId = tag.id).first() shouldBe expected
            database.accountTagDao().find(accountId = otherAccountId, tagId = tag.id).first() shouldBe expected
        }
    }) {
    public companion object {
        private const val TAG_COUNT: Int = 25
        private const val PAGE_SIZE: Int = 10
        private const val INVALIDATION_TIMEOUT_MILLIS: Long = 5_000

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private suspend fun PagingSource<Int, TagLocalEntity>.loadPage(
            key: Int? = null,
            loadSize: Int = 100,
        ): PagingSource.LoadResult.Page<Int, TagLocalEntity> {
            val params: PagingSource.LoadParams<Int> =
                if (key == null) {
                    PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
                } else {
                    PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
                }

            return load(params).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, TagLocalEntity>>()
        }

        private fun tag(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            title: String = fixtureMonkey.giveMeOne<String>(),
            emoji: String = "",
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            updatedAt: Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            createdAt: Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, id)
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>().copy(emoji = emoji, title = title))
                .setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, updatedAt)
                .setExp(TagLocalEntity::createdAt, createdAt)
                .sample()
    }
}
