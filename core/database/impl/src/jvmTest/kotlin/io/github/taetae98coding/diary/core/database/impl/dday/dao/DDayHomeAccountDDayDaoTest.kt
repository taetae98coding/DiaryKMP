package io.github.taetae98coding.diary.core.database.impl.dday.dao

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDaySyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.entity.DDayFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.ddayTagTable
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagSyncTransactionImpl
import io.github.taetae98coding.diary.core.testing.dday.ddayDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DDayHomeAccountDDayDaoTest :
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
            vararg ddayList: DDayLocalEntity,
            isDirty: Boolean = true,
        ) {
            database.withWriteTransaction {
                database.ddayDao().upsert(ddayList.toList())
                database.accountDDayDao().upsert(
                    ddayList.map { dday ->
                        AccountDDayLocalEntity(
                            accountId = accountId,
                            ddayId = dday.id,
                            isDirty = isDirty,
                        )
                    },
                )
            }
        }

        suspend fun insertTag(
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

        suspend fun linkTag(
            accountId: Uuid,
            ddayId: Uuid,
            tagId: Uuid,
            isDeleted: Boolean = false,
        ) {
            database.withWriteTransaction {
                database.ddayTagTable().upsertPending(
                    accountId = accountId,
                    entityTagList =
                        listOf(
                            EntityTagLocalEntity(
                                entityId = ddayId,
                                tagId = tagId,
                                isDeleted = isDeleted,
                                updatedAt = Arb.secondPrecisionInstant().next(),
                                createdAt = Arb.secondPrecisionInstant().next(),
                            ),
                        ),
                )
            }
        }

        suspend fun unlinkTag(
            ddayId: Uuid,
            tagId: Uuid,
        ) {
            database.ddayTagTable().updateDeleted(
                entityId = ddayId,
                tagId = tagId,
                isDeleted = true,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1
        }

        suspend fun select(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            database.ddayFilterTagDao().upsert(
                entity =
                    DDayFilterTagLocalEntity(
                        accountId = accountId,
                        tagId = tagId,
                    ),
            )
        }

        suspend fun PagingSource<Int, DDayLocalEntity>.pagedDDays(): List<DDayLocalEntity> {
            val result =
                load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 100,
                        placeholdersEnabled = false,
                    ),
                )

            return result.shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>().data
        }

        suspend fun pagedDDays(accountId: Uuid): List<DDayLocalEntity> = database.accountDDayDao().page(accountId = accountId).pagedDDays()

        suspend fun pagedIds(accountId: Uuid): List<Uuid> = pagedDDays(accountId).map { dday -> dday.id }

        suspend fun selectedTagIdList(accountId: Uuid): List<Uuid> =
            database
                .ddayFilterTagDao()
                .getTagList(accountId = accountId)
                .first()
                .map { tag -> tag.id }

        // 목록 화면은 열어 둔 조회가 무효화되면 스스로 다시 조회하므로, 새로 요청하지 않아도 반영된다는 것을 무효화로 확인한다.
        suspend fun assertOpenedPageInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = database.accountDDayDao().page(accountId = accountId)
            pagingSource.pagedDDays()
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
            pagingSource.invalid.shouldBeTrue()
        }

        suspend fun updateDetail(
            accountId: Uuid,
            dday: DDayLocalEntity,
            detail: DDayDetailLocalEntity,
        ) {
            AccountDDayTransactionImpl(database = database).updateDetail(
                accountId = accountId,
                ddayId = dday.id,
                detail = detail,
                updatedAt = Arb.secondPrecisionInstant().next(),
            ) shouldBe 1
        }

        suspend fun updateDeleted(
            accountId: Uuid,
            ddayId: Uuid,
            isDeleted: Boolean,
            updatedAt: Instant = Arb.secondPrecisionInstant().next(),
        ): Int =
            AccountDDayTransactionImpl(database = database).updateDeleted(
                accountId = accountId,
                ddayId = ddayId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )

        test("TC-dcf47cef-95cb-45df-8b8e-9434e971c2e8 현재 계정과 연결되고 삭제되지 않은 디데이를 모두 노출한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val endedDDay = dday(title = "Alpha", endDate = LocalDate(2000, 1, 2), repeats = emptySet())
            val noRepeatDDay = dday(title = "Bravo", repeats = emptySet())
            val endedRepeatDDay =
                dday(
                    title = "Charlie",
                    baseDate = LocalDate(2000, 1, 1),
                    endDate = LocalDate(2000, 1, 5),
                    repeats = setOf(DateTimeUnit.DayBased(days = 100)),
                )
            val deletedDDay = dday(title = "Delta", isDeleted = true)
            val otherAccountDDay = dday(title = "Echo")
            insert(accountId, endedDDay, noRepeatDDay, endedRepeatDDay, deletedDDay)
            insert(otherAccountId, otherAccountDDay)

            pagedIds(accountId) shouldBe listOf(endedDDay.id, noRepeatDDay.id, endedRepeatDDay.id)
        }

        test("TC-61972442-eb27-4f44-abb0-3b410487aab4 목록은 제목 오름차순이고 카드 문구와 종료일, 기념일은 순서에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val da = dday(title = "다", baseDate = LocalDate(2026, 3, 20), endDate = null, repeats = emptySet())
            val ga = dday(title = "가", baseDate = LocalDate(2025, 1, 15), endDate = LocalDate(2025, 2, 1), repeats = setOf(DateTimeUnit.MonthBased(months = 12)))
            val na = dday(title = "나", baseDate = LocalDate(2026, 4, 9), endDate = null, repeats = setOf(DateTimeUnit.DayBased(days = 100)))
            insert(accountId, da, ga, na)

            pagedDDays(accountId).map { dday -> dday.detail.title } shouldBe listOf("가", "나", "다")
        }

        test("TC-ae31a63a-41e7-4846-93b9-5e35c0db5743 현재 계정 기준으로 기기에 저장된 디데이를 나누어 불러온다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val ddayList = (0 until 5).map { index -> dday(title = "Title$index") }
            insert(accountId, *ddayList.reversed().toTypedArray())
            insert(otherAccountId, dday(title = "Title0-other"))
            val pagingSource = database.accountDDayDao().page(accountId = accountId)

            val firstPage =
                pagingSource
                    .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
            val secondPage =
                pagingSource
                    .load(PagingSource.LoadParams.Append(key = firstPage.nextKey.shouldNotBeNull(), loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()
            val thirdPage =
                pagingSource
                    .load(PagingSource.LoadParams.Append(key = secondPage.nextKey.shouldNotBeNull(), loadSize = 2, placeholdersEnabled = false))
                    .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, DDayLocalEntity>>()

            firstPage.data.map { dday -> dday.id } shouldBe ddayList.take(2).map { dday -> dday.id }
            secondPage.data.map { dday -> dday.id } shouldBe ddayList.subList(2, 4).map { dday -> dday.id }
            thirdPage.data.map { dday -> dday.id } shouldBe ddayList.drop(4).map { dday -> dday.id }
            thirdPage.nextKey shouldBe null
        }

        test("TC-082bd1a2-64b2-473e-b6f5-0fa64d81c823 태그를 선택하면 그 태그와 연결된 디데이만 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagB = tag()
            val aDDay = dday(title = "Alpha")
            val bDDay = dday(title = "Bravo")
            insert(accountId, aDDay, bDDay)
            insertTag(accountId, tagA, tagB)
            linkTag(accountId = accountId, ddayId = aDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = bDDay.id, tagId = tagB.id)
            pagedIds(accountId) shouldBe listOf(aDDay.id, bDDay.id)

            assertOpenedPageInvalidated(accountId) { select(accountId = accountId, tagId = tagA.id) }

            pagedIds(accountId) shouldBe listOf(aDDay.id)
            selectedTagIdList(accountId) shouldBe listOf(tagA.id)
        }

        test("TC-9644c4ab-aeb3-48ee-bbfd-8c8af40e4e9d TC-9aad0c4c-9ee3-42cf-8696-e791e8b1de25 여러 태그를 선택하면 하나 이상과 연결된 디데이를 제목 순으로 다시 조회한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagB = tag()
            val bothDDay = dday(title = "Alpha")
            val bDDay = dday(title = "Bravo")
            val aDDay = dday(title = "Charlie")
            val noTagDDay = dday(title = "Delta")
            insert(accountId, bothDDay, bDDay, aDDay, noTagDDay)
            insertTag(accountId, tagA, tagB)
            linkTag(accountId = accountId, ddayId = aDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = bDDay.id, tagId = tagB.id)
            linkTag(accountId = accountId, ddayId = bothDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = bothDDay.id, tagId = tagB.id)
            select(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(bothDDay.id, aDDay.id)

            assertOpenedPageInvalidated(accountId) { select(accountId = accountId, tagId = tagB.id) }

            pagedIds(accountId) shouldBe listOf(bothDDay.id, bDDay.id, aDDay.id)
        }

        test("TC-a29e4ada-fd9d-43f1-b9b4-0f954371f25e 마지막 태그의 선택을 해제하면 모든 디데이가 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = tag()
            val tagDDay = dday(title = "Alpha")
            val noTagDDay = dday(title = "Bravo")
            insert(accountId, tagDDay, noTagDDay)
            insertTag(accountId, tag)
            linkTag(accountId = accountId, ddayId = tagDDay.id, tagId = tag.id)
            select(accountId = accountId, tagId = tag.id)
            pagedIds(accountId) shouldBe listOf(tagDDay.id)

            assertOpenedPageInvalidated(accountId) { database.ddayFilterTagDao().delete(accountId = accountId, tagId = tag.id) }

            pagedIds(accountId) shouldBe listOf(tagDDay.id, noTagDDay.id)
            selectedTagIdList(accountId).shouldBeEmpty()
        }

        test("TC-591381bb-2364-4183-a436-801a0e7b5588 선택 전체 해제를 실행하면 노출 기준을 만족하는 디데이가 모두 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagB = tag()
            val aDDay = dday(title = "Alpha")
            val bDDay = dday(title = "Bravo")
            val noTagDDay = dday(title = "Charlie")
            insert(accountId, aDDay, bDDay, noTagDDay)
            insertTag(accountId, tagA, tagB)
            linkTag(accountId = accountId, ddayId = aDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = bDDay.id, tagId = tagB.id)
            select(accountId = accountId, tagId = tagA.id)
            select(accountId = accountId, tagId = tagB.id)
            pagedIds(accountId) shouldBe listOf(aDDay.id, bDDay.id)

            assertOpenedPageInvalidated(accountId) { database.ddayFilterTagDao().deleteAll(accountId = accountId) }

            pagedIds(accountId) shouldBe listOf(aDDay.id, bDDay.id, noTagDDay.id)
            selectedTagIdList(accountId).shouldBeEmpty()
        }

        test("TC-c8172f51-2b24-4cfe-8e79-b57387a8bbe3 저장한 두 태그 선택은 다시 조회해도 그대로이고 목록도 같은 범위로 좁혀진다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag(title = "Alpha")
            val tagB = tag(title = "Bravo")
            val aDDay = dday(title = "Alpha")
            val bDDay = dday(title = "Bravo")
            val noTagDDay = dday(title = "Charlie")
            insert(accountId, aDDay, bDDay, noTagDDay)
            insertTag(accountId, tagA, tagB)
            linkTag(accountId = accountId, ddayId = aDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = bDDay.id, tagId = tagB.id)
            select(accountId = accountId, tagId = tagA.id)
            select(accountId = accountId, tagId = tagB.id)
            val firstSelection = selectedTagIdList(accountId)
            val firstList = pagedIds(accountId)

            selectedTagIdList(accountId) shouldBe firstSelection
            selectedTagIdList(accountId) shouldBe listOf(tagA.id, tagB.id)
            pagedIds(accountId) shouldBe firstList
            pagedIds(accountId) shouldBe listOf(aDDay.id, bDDay.id)
        }

        test("TC-af1a72e9-f180-4cc3-b3d6-fb4ae9f7a103 태그 필터는 목록 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val ga = dday(title = "가")
            val na = dday(title = "나")
            val da = dday(title = "다")
            val ra = dday(title = "라")
            insert(accountId, ra, da, na, ga)
            insertTag(accountId, tagA)
            listOf(ra, ga, da).forEach { dday -> linkTag(accountId = accountId, ddayId = dday.id, tagId = tagA.id) }

            select(accountId = accountId, tagId = tagA.id)

            pagedDDays(accountId).map { dday -> dday.detail.title } shouldBe listOf("가", "다", "라")
        }

        test("TC-bafc2c46-4d23-4df3-abda-3a661b2e9463 추가한 디데이가 적용한 필터에 맞지 않으면 조회되지 않고 선택을 해제하면 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val aDDay = dday(title = "Alpha")
            insert(accountId, aDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = aDDay.id, tagId = tagA.id)
            select(accountId = accountId, tagId = tagA.id)
            val addedDDay = dday(title = "Bravo")

            AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = addedDDay, ddayTagList = emptyList())

            pagedIds(accountId) shouldBe listOf(aDDay.id)

            database.ddayFilterTagDao().delete(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId) shouldBe listOf(aDDay.id, addedDDay.id)
        }

        test("TC-a46438ae-4ab7-41d7-a49b-37304accb159 태그 필터는 해제되지 않은 연결로만 판정한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val linkedDDay = dday(title = "Alpha")
            val unlinkedDDay = dday(title = "Bravo")
            insert(accountId, linkedDDay, unlinkedDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = linkedDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = unlinkedDDay.id, tagId = tagA.id)
            unlinkTag(ddayId = unlinkedDDay.id, tagId = tagA.id)

            select(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId) shouldBe listOf(linkedDDay.id)
        }

        test("TC-32f177c6-98b6-42f1-840d-82c9760ec692 태그와 태그의 연결을 따라 판정하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagB = tag()
            val bDDay = dday(title = "Alpha")
            val otherDDay = dday(title = "Bravo")
            insert(accountId, bDDay, otherDDay)
            insertTag(accountId, tagA, tagB)
            database.tagLinkDao().upsert(
                TagLinkLocalEntity(
                    fromTagId = tagA.id,
                    toTagId = tagB.id,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ),
            )
            linkTag(accountId = accountId, ddayId = bDDay.id, tagId = tagB.id)

            select(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId).shouldBeEmpty()
        }

        test("TC-a8f1b102-e4a3-40a1-84b1-953dd0c8e261 선택한 태그와 연결되어 있어도 삭제된 디데이는 조회하지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val activeDDay = dday(title = "Alpha")
            val deletedDDay = dday(title = "Bravo", isDeleted = true)
            insert(accountId, activeDDay, deletedDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = activeDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = deletedDDay.id, tagId = tagA.id)

            select(accountId = accountId, tagId = tagA.id)

            pagedIds(accountId) shouldBe listOf(activeDDay.id)
        }

        context("TC-dba071a6-eb8f-4196-979e-76536b2fdc50 선택한 태그를 선택할 수 없게 되면 그 선택을 무시한다") {
            data class Case(
                val name: String,
                val makeUnselectable: suspend (accountId: Uuid, tagId: Uuid) -> Unit,
            )

            listOf(
                Case("완료") { accountId, tagId ->
                    database.accountTagDao().updateFinished(accountId = accountId, tagId = tagId, isFinished = true, updatedAt = Arb.secondPrecisionInstant().next())
                },
                Case("삭제") { accountId, tagId ->
                    database.accountTagDao().updateDeleted(accountId = accountId, tagId = tagId, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next())
                },
                Case("현재 사용자 계정과의 연결 해제") { accountId, _ ->
                    database.accountDataDao().deleteAccountTag(accountId = accountId)
                },
            ).forEach { case ->
                test(case.name) {
                    val accountId = Arb.kotlinUuid().next()
                    val tagA = tag()
                    val tagDDay = dday(title = "Alpha")
                    val noTagDDay = dday(title = "Bravo")
                    insert(accountId, tagDDay, noTagDDay)
                    insertTag(accountId, tagA)
                    linkTag(accountId = accountId, ddayId = tagDDay.id, tagId = tagA.id)
                    select(accountId = accountId, tagId = tagA.id)
                    pagedIds(accountId) shouldBe listOf(tagDDay.id)

                    assertOpenedPageInvalidated(accountId) { case.makeUnselectable(accountId, tagA.id) }

                    pagedIds(accountId) shouldBe listOf(tagDDay.id, noTagDDay.id)
                    selectedTagIdList(accountId).shouldBeEmpty()
                }
            }
        }

        test("TC-911975f0-716c-451b-95bf-aa91e58af35b 태그가 다시 선택할 수 있게 되면 이전 선택을 다시 적용한다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagDDay = dday(title = "Alpha")
            val noTagDDay = dday(title = "Bravo")
            insert(accountId, tagDDay, noTagDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = tagDDay.id, tagId = tagA.id)
            select(accountId = accountId, tagId = tagA.id)
            database.accountTagDao().updateFinished(accountId = accountId, tagId = tagA.id, isFinished = true, updatedAt = Instant.fromEpochMilliseconds(2_000))
            pagedIds(accountId) shouldBe listOf(tagDDay.id, noTagDDay.id)

            assertOpenedPageInvalidated(accountId) {
                database.accountTagDao().updateFinished(accountId = accountId, tagId = tagA.id, isFinished = false, updatedAt = Instant.fromEpochMilliseconds(3_000))
            }

            pagedIds(accountId) shouldBe listOf(tagDDay.id)
            selectedTagIdList(accountId) shouldBe listOf(tagA.id)
        }

        test("TC-d9607898-3007-4ce8-845e-84c573b6df53 무시되고 있는 선택만 남으면 보이는 선택이 없고 삭제가 풀리면 그 선택이 다시 적용된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag(isDeleted = true)
            val tagDDay = dday(title = "Alpha")
            val noTagDDay = dday(title = "Bravo")
            insert(accountId, tagDDay, noTagDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = tagDDay.id, tagId = tagA.id)
            select(accountId = accountId, tagId = tagA.id)

            selectedTagIdList(accountId).shouldBeEmpty()
            pagedIds(accountId) shouldBe listOf(tagDDay.id, noTagDDay.id)

            AccountTagSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                tagList = listOf(tagA.copy(isDeleted = false, updatedAt = Instant.fromEpochMilliseconds(tagA.updatedAt.toEpochMilliseconds() + 1_000))),
                cursor = Arb.long().next(),
            )

            selectedTagIdList(accountId) shouldBe listOf(tagA.id)
            pagedIds(accountId) shouldBe listOf(tagDDay.id)
        }

        test("TC-c456fd21-3927-43b9-ac55-90b5b25e5890 필터를 적용한 동안 태그 연결이 바뀌면 다시 고르지 않아도 목록에 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val xDDay = dday(title = "Bravo")
            val yDDay = dday(title = "Alpha")
            val otherDDay = dday(title = "Charlie")
            insert(accountId, xDDay, yDDay, otherDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = xDDay.id, tagId = tagA.id)
            linkTag(accountId = accountId, ddayId = otherDDay.id, tagId = tagA.id)
            select(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(xDDay.id, otherDDay.id)

            assertOpenedPageInvalidated(accountId) { unlinkTag(ddayId = xDDay.id, tagId = tagA.id) }
            assertOpenedPageInvalidated(accountId) { linkTag(accountId = accountId, ddayId = yDDay.id, tagId = tagA.id) }

            pagedIds(accountId) shouldBe listOf(yDDay.id, otherDDay.id)
        }

        context("TC-74601fdf-3695-4b1b-b8e5-cf7c44531ce5 저장 상태가 바뀌면 열어 둔 목록이 노출, 필터와 정렬 기준으로 갱신된다") {
            test("노출 기준을 만족하는 디데이를 추가하면 제목 순서에 맞는 자리에 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val first = dday(title = "Alpha")
                val last = dday(title = "Charlie")
                insert(accountId, first, last)
                val added = dday(title = "Bravo")

                assertOpenedPageInvalidated(accountId) {
                    AccountDDayTransactionImpl(database = database).upsert(accountId = accountId, dday = added, ddayTagList = emptyList())
                }

                pagedIds(accountId) shouldBe listOf(first.id, added.id, last.id)
            }

            test("삭제 상태로 바뀌면 사라지고 미삭제로 되돌리면 제목 순서에 맞는 자리에 다시 나타난다") {
                val accountId = Arb.kotlinUuid().next()
                val first = dday(title = "Alpha")
                val middle = dday(title = "Bravo")
                val last = dday(title = "Charlie")
                insert(accountId, first, middle, last)

                assertOpenedPageInvalidated(accountId) { updateDeleted(accountId, middle.id, isDeleted = true) shouldBe 1 }
                pagedIds(accountId) shouldBe listOf(first.id, last.id)

                assertOpenedPageInvalidated(accountId) { updateDeleted(accountId, middle.id, isDeleted = false) shouldBe 1 }
                pagedIds(accountId) shouldBe listOf(first.id, middle.id, last.id)
            }

            test("제목이 수정되면 바뀐 제목으로 제목 순서에 맞는 자리로 옮겨진다") {
                val accountId = Arb.kotlinUuid().next()
                val first = dday(title = "Alpha")
                val middle = dday(title = "Bravo")
                val last = dday(title = "Charlie")
                insert(accountId, first, middle, last)

                assertOpenedPageInvalidated(accountId) { updateDetail(accountId, first, first.detail.copy(title = "Delta")) }

                pagedDDays(accountId).map { dday -> dday.detail.title } shouldBe listOf("Bravo", "Charlie", "Delta")
            }

            test("컬러, 기준일, 기준일을 1일로 세기, 반복 간격, 종료일, 달력에 없는 날짜 처리가 수정되면 바뀐 값으로 조회되고 자리는 그대로다") {
                val accountId = Arb.kotlinUuid().next()
                val first = dday(title = "Alpha")
                val middle = dday(title = "Bravo")
                val last = dday(title = "Charlie")
                insert(accountId, first, middle, last)
                val changedDetailList =
                    listOf(
                        middle.detail.copy(color = middle.detail.color + 1),
                        middle.detail.copy(baseDate = LocalDate(2026, 3, 21)),
                        middle.detail.copy(countsBaseDay = !middle.detail.countsBaseDay),
                        middle.detail.copy(
                            repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                            endDate = LocalDate(2099, 12, 31),
                            overflowPolicy =
                                if (middle.detail.overflowPolicy == DDayOverflowPolicyLocalEntity.entries.first()) {
                                    DDayOverflowPolicyLocalEntity.entries.last()
                                } else {
                                    DDayOverflowPolicyLocalEntity.entries.first()
                                },
                        ),
                    )

                changedDetailList.forEach { detail ->
                    assertOpenedPageInvalidated(accountId) { updateDetail(accountId, middle, detail) }

                    val ddayList = pagedDDays(accountId)
                    ddayList.map { dday -> dday.id } shouldBe listOf(first.id, middle.id, last.id)
                    ddayList[1].detail shouldBe detail
                }
            }

            test("서버에서 내려받은 디데이가 반영되면 노출 기준과 정렬 기준에 따라 목록에 반영된다") {
                val accountId = Arb.kotlinUuid().next()
                val first = dday(title = "Alpha")
                val last = dday(title = "Charlie")
                insert(accountId, first, last, isDirty = false)
                val pulled = dday(title = "Bravo")
                val pulledDeleted = dday(title = "Bravo2", isDeleted = true)
                val renamedLast = last.copy(detail = last.detail.copy(title = "0-Charlie"), updatedAt = Instant.fromEpochMilliseconds(last.updatedAt.toEpochMilliseconds() + 1_000))

                assertOpenedPageInvalidated(accountId) {
                    AccountDDaySyncTransactionImpl(database = database).upsert(
                        accountId = accountId,
                        ddayList = listOf(pulled, pulledDeleted, renamedLast),
                        cursor = Arb.long().next(),
                    )
                }

                pagedIds(accountId) shouldBe listOf(last.id, first.id, pulled.id)
            }
        }

        test("TC-2a38eccb-d6fd-4c10-b3ba-ef1fb0e3e885 TC-ae40f0f7-9272-43b6-9f35-a2098f5e6abb 삭제와 실행 취소는 현재 계정 디데이의 삭제 여부와 수정 시각만 바꾼다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val target = dday(title = "Alpha")
            val otherAccountDDay = dday(title = "Bravo")
            insert(accountId, target)
            insert(otherAccountId, otherAccountDDay)
            val deletedAt = Arb.secondPrecisionInstant().next()
            val restoredAt = Arb.secondPrecisionInstant().next()

            updateDeleted(accountId, target.id, isDeleted = true, updatedAt = deletedAt) shouldBe 1
            database.accountDDayDao().find(accountId = accountId, ddayId = target.id).first() shouldBe target.copy(isDeleted = true, updatedAt = deletedAt)

            updateDeleted(accountId, target.id, isDeleted = false, updatedAt = restoredAt) shouldBe 1
            database.accountDDayDao().find(accountId = accountId, ddayId = target.id).first() shouldBe target.copy(isDeleted = false, updatedAt = restoredAt)

            updateDeleted(accountId, otherAccountDDay.id, isDeleted = true) shouldBe 0
            database.accountDDayDao().find(accountId = otherAccountId, ddayId = otherAccountDDay.id).first() shouldBe otherAccountDDay
        }

        context("TC-ec3e145c-10e3-4f3f-aa03-52f484fd1b0d 삭제와 실행 취소를 기기에 저장하면 업로드 대기로 기록되고 디데이와 계정 연결은 남는다") {
            listOf(true to "삭제", false to "실행 취소").forEach { (isDeleted, name) ->
                test(name) {
                    val accountId = Arb.kotlinUuid().next()
                    val target = dday(title = "Alpha", isDeleted = !isDeleted, repeats = setOf(DateTimeUnit.DayBased(days = 100)))
                    insert(accountId, target, isDirty = false)
                    database.accountDDaySyncDao().findPending(accountId = accountId).shouldBeEmpty()
                    val updatedAt = Arb.secondPrecisionInstant().next()

                    updateDeleted(accountId, target.id, isDeleted = isDeleted, updatedAt = updatedAt) shouldBe 1

                    val expected = target.copy(isDeleted = isDeleted, updatedAt = updatedAt)
                    database.accountDDaySyncDao().findPending(accountId = accountId) shouldBe listOf(expected)
                    database.accountDDayDao().find(accountId = accountId, ddayId = target.id).first() shouldBe expected
                }
            }
        }

        test("TC-d06f6522-e878-4a77-8a6d-dfa5e210aaa0 삭제하고 실행 취소해도 디데이의 태그 연결은 그대로 남는다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = tag()
            val tagDDay = dday(title = "Alpha")
            val noTagDDay = dday(title = "Bravo")
            insert(accountId, tagDDay, noTagDDay)
            insertTag(accountId, tagA)
            linkTag(accountId = accountId, ddayId = tagDDay.id, tagId = tagA.id)

            updateDeleted(accountId, tagDDay.id, isDeleted = true) shouldBe 1
            database.ddayTagTable().findByEntityIdList(listOf(tagDDay.id)).map { link -> link.tagId to link.isDeleted } shouldBe listOf(tagA.id to false)

            updateDeleted(accountId, tagDDay.id, isDeleted = false) shouldBe 1
            database.ddayTagTable().findByEntityIdList(listOf(tagDDay.id)).map { link -> link.tagId to link.isDeleted } shouldBe listOf(tagA.id to false)

            select(accountId = accountId, tagId = tagA.id)
            pagedIds(accountId) shouldBe listOf(tagDDay.id)
        }
    }) {
    public companion object {
        private const val INVALIDATION_TIMEOUT_MILLIS = 30_000L

        private fun dday(
            title: String,
            isDeleted: Boolean = false,
            baseDate: LocalDate? = null,
            endDate: LocalDate? = null,
            repeats: Set<DateTimeUnit.DateBased>? = null,
        ): DDayLocalEntity {
            val detail = Arb.ddayDetailLocalEntity().next()

            return Arb
                .ddayLocalEntity()
                .next()
                .copy(
                    id = Arb.kotlinUuid().next(),
                    detail =
                        detail.copy(
                            title = title,
                            baseDate = baseDate ?: detail.baseDate,
                            endDate = endDate ?: detail.endDate,
                            repeats = repeats ?: detail.repeats,
                        ),
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
        }

        private fun tag(
            title: String? = null,
            isDeleted: Boolean = false,
        ): TagLocalEntity {
            val detail = Arb.tagDetailLocalEntity().next()

            return Arb
                .tagLocalEntity()
                .next()
                .copy(
                    id = Arb.kotlinUuid().next(),
                    detail = detail.copy(title = title ?: detail.title),
                    isFinished = false,
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
        }
    }
}
