package io.github.taetae98coding.diary.core.database.impl.tag.dao

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
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagFinishedDaoTest :
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

        suspend fun finishedTagList(accountId: Uuid): List<TagLocalEntity> =
            database
                .accountTagDao()
                .pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadPage()
                .data

        suspend fun finishedTagIdList(accountId: Uuid): List<Uuid> = finishedTagList(accountId = accountId).map { tag -> tag.id }

        suspend fun activeTagIdList(accountId: Uuid): List<Uuid> =
            database
                .accountTagDao()
                .page(accountId = accountId, query = "", sort = ListSortLocalEntity.DEFAULT.queryValue)
                .loadPage()
                .data
                .map { tag -> tag.id }

        suspend fun assertFinishedPageInvalidated(
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
            finishedTagIdList(accountId) shouldBe expectedIdList
        }

        test("TC-TAG-FINISHED-LIST-FEATURE-001 TC-TAG-FINISHED-LIST-DATA-001 현재 계정의 완료·미삭제 태그만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedTag = tag(isFinished = true, isDeleted = false)
            val activeTag = tag(isFinished = false, isDeleted = false)
            val finishedDeletedTag = tag(isFinished = true, isDeleted = true)
            val activeDeletedTag = tag(isFinished = false, isDeleted = true)
            val otherAccountFinishedTag = tag(isFinished = true, isDeleted = false)
            insert(accountId, finishedTag, activeTag, finishedDeletedTag, activeDeletedTag)
            insert(otherAccountId, otherAccountFinishedTag)

            finishedTagIdList(accountId) shouldBe listOf(finishedTag.id)
        }

        test("TC-TAG-FINISHED-LIST-DATA-002 TC-TAG-FINISHED-LIST-DOMAIN-008 수정 시각과 생성 시각에 관계없이 제목 오름차순으로 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val bravoTag =
                tag(
                    title = "Bravo",
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(3_000),
                    createdAt = Instant.fromEpochMilliseconds(3_000),
                )
            val alphaTag =
                tag(
                    title = "Alpha",
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                    createdAt = Instant.fromEpochMilliseconds(1_000),
                )
            val charlieTag =
                tag(
                    title = "Charlie",
                    isFinished = true,
                    updatedAt = Instant.fromEpochMilliseconds(2_000),
                    createdAt = Instant.fromEpochMilliseconds(2_000),
                )
            insert(accountId, bravoTag, alphaTag, charlieTag)

            finishedTagIdList(accountId) shouldBe
                listOf(
                    alphaTag.id,
                    bravoTag.id,
                    charlieTag.id,
                )
        }

        test("TC-TAG-FINISHED-LIST-DATA-006 완료 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val expectedTagList =
                List(TAG_COUNT) { index ->
                    tag(
                        title = "Tag-${index.toString().padStart(length = 3, padChar = '0')}",
                        isFinished = true,
                    )
                }

            insert(accountId, *expectedTagList.reversed().toTypedArray())

            val firstPage = database.accountTagDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue).loadPage(loadSize = PAGE_SIZE)

            firstPage.data shouldBe expectedTagList.take(PAGE_SIZE)
            firstPage.nextKey shouldBe PAGE_SIZE

            val secondPage =
                database
                    .accountTagDao()
                    .pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
                    .loadPage(key = firstPage.nextKey, loadSize = PAGE_SIZE)

            secondPage.data shouldBe expectedTagList.drop(PAGE_SIZE).take(PAGE_SIZE)
        }

        test("TC-TAG-FINISHED-LIST-DATA-004 완료는 페이지를 무효화하고 태그를 완료 목록의 정렬 위치에 넣는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedTag = tag(title = "Bravo", isFinished = true)
            val activeTag = tag(title = "Alpha", isFinished = false)
            insert(accountId, finishedTag, activeTag)
            val pagingSource = database.accountTagDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.loadPage().data shouldBe listOf(finishedTag)

            assertFinishedPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                expectedIdList = listOf(activeTag.id, finishedTag.id),
            ) {
                insert(accountId, activeTag.copy(isFinished = true))
            }
        }

        test("TC-TAG-FINISHED-LIST-DATA-004 제목 변경은 페이지를 무효화하고 태그를 변경된 정렬 위치로 옮긴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val anchorTag = tag(title = "Bravo", isFinished = true)
            val movingTag = tag(title = "Alpha", isFinished = true)
            insert(accountId, anchorTag, movingTag)
            val pagingSource = database.accountTagDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
            pagingSource.loadPage().data shouldBe listOf(movingTag, anchorTag)

            assertFinishedPageInvalidated(
                pagingSource = pagingSource,
                accountId = accountId,
                expectedIdList = listOf(anchorTag.id, movingTag.id),
            ) {
                insert(accountId, movingTag.copy(detail = movingTag.detail.copy(title = "Charlie")))
            }
        }

        listOf(
            "다시 시작" to true,
            "삭제" to false,
        ).forEach { (label, isRestart) ->
            test("TC-TAG-FINISHED-LIST-DATA-004 $label 은 페이지를 무효화하고 태그를 완료 목록에서 제외한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val remainingTag = tag(title = "Alpha", isFinished = true)
                val changingTag = tag(title = "Bravo", isFinished = true)
                insert(accountId, remainingTag, changingTag)
                val pagingSource = database.accountTagDao().pageFinished(accountId = accountId, sort = ListSortLocalEntity.DEFAULT.queryValue)
                pagingSource.loadPage().data shouldBe listOf(remainingTag, changingTag)

                assertFinishedPageInvalidated(
                    pagingSource = pagingSource,
                    accountId = accountId,
                    expectedIdList = listOf(remainingTag.id),
                ) {
                    if (isRestart) {
                        database.accountTagDao().updateFinished(
                            accountId = accountId,
                            tagId = changingTag.id,
                            isFinished = false,
                            updatedAt = Instant.fromEpochMilliseconds(2_000),
                        )
                    } else {
                        database.accountTagDao().updateDeleted(
                            accountId = accountId,
                            tagId = changingTag.id,
                            isDeleted = true,
                            updatedAt = Instant.fromEpochMilliseconds(2_000),
                        )
                    }
                }
            }
        }

        test("TC-TAG-DETAIL-DATA-003 상태 변경은 현재 계정과 연결된 태그에만 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(isFinished = true)
            insert(accountId, tag)

            database.accountTagDao().updateFinished(
                accountId = otherAccountId,
                tagId = tag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 0

            finishedTagIdList(accountId) shouldBe listOf(tag.id)
        }

        test("TC-TAG-DETAIL-DATA-002 상태 변경은 태그의 이모지, 제목, 설명, 컬러를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(isFinished = true)
            insert(accountId, tag)

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1
            database.accountTagDao().updateDeleted(
                accountId = accountId,
                tagId = tag.id,
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(3_000),
            ) shouldBe 1

            val updated =
                database
                    .accountTagDao()
                    .find(accountId = accountId, tagId = tag.id)
                    .first()
                    .shouldNotBeNull()
            updated.detail shouldBe tag.detail
            updated.createdAt shouldBe tag.createdAt
        }

        test("TC-TAG-DETAIL-DOMAIN-005 다시 시작한 태그는 완료 목록에서 사라지고 태그 필터 선택 대상으로 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(isFinished = true)
            insert(accountId, tag)
            activeTagIdList(accountId).shouldBeEmpty()

            database.accountTagDao().updateFinished(
                accountId = accountId,
                tagId = tag.id,
                isFinished = false,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            finishedTagIdList(accountId).shouldBeEmpty()
            activeTagIdList(accountId) shouldBe listOf(tag.id)
        }

        test("삭제를 되돌린 완료 태그는 제목에 해당하는 자리에서 다시 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstTag = tag(title = "Bravo", isFinished = true)
            val secondTag = tag(title = "Alpha", isFinished = true, isDeleted = true)
            insert(accountId, firstTag, secondTag)

            database.accountTagDao().updateDeleted(
                accountId = accountId,
                tagId = secondTag.id,
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            ) shouldBe 1

            finishedTagIdList(accountId) shouldBe listOf(secondTag.id, firstTag.id)
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
            isFinished: Boolean = false,
            isDeleted: Boolean = false,
            updatedAt: Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            createdAt: Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::id, id)
                .setExp(TagLocalEntity::detail, fixtureMonkey.giveMeOne<TagDetailLocalEntity>().copy(title = title))
                .setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, updatedAt)
                .setExp(TagLocalEntity::createdAt, createdAt)
                .sample()
    }
}
