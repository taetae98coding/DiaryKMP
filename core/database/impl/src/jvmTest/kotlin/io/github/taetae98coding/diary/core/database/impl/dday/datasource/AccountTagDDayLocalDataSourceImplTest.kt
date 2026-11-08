package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.dday.transaction.AccountDDayTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabCase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabItem
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabTc
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.loadTagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.tagEntityTabLocalDataSourceTests
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.localDDay
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

private object DDayTagTabCase : TagTabCase {
    override val kind = EntityTagKindLocalEntity.DDAY
    override val label = "디데이"
    override val tc =
        TagTabTc(
            exposure = "TC-4a98b5fd-dbd6-46b7-982d-ca6403ee0535",
            order = "TC-ad79fabc-7400-4ea8-9a8e-3bf040031533",
            paging = "TC-2900e45d-ec26-465f-a051-bcb9814d3231",
            change = "TC-1f64fda5-cb8b-4c2a-b5c2-6df4e1939631",
            tagState = "TC-8defdf1d-fe00-45e2-b855-9fb07eb3b252",
            saveSelection = "TC-f855026e-45a7-43d4-9b64-1dc027d63e8f",
            otherTag = "TC-7ecf16c5-8c79-4fbd-818c-bae1fe9ebbbf",
        )

    override suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        val dday = Arb.ddayLocalEntity().next()
        AccountDDayTransactionImpl(database = database).upsert(
            accountId = accountId,
            dday =
                dday.copy(
                    id = item.id,
                    detail = dday.detail.copy(title = item.title),
                    isDeleted = item.isDeleted,
                    updatedAt = item.updatedAt,
                    createdAt = Arb.secondPrecisionInstant().next(),
                ),
            ddayTagList = entityTagList,
        )
    }

    override suspend fun page(
        database: DiaryDatabase,
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
        key: Int?,
        loadSize: Int,
    ): TagTabPage =
        AccountTagDDayLocalDataSourceImpl(database = database)
            .page(accountId = accountId, tagId = tagId, scope = scope)
            .loadTagTabPage(key = key, loadSize = loadSize) { dday -> dday.id }
}

internal class AccountTagDDayLocalDataSourceImplTest :
    FunSpec({
        include(
            tagEntityTabLocalDataSourceTests(case = DDayTagTabCase) { context ->
                suspend fun insertLinkedDDay(
                    accountId: Uuid,
                    tagId: Uuid,
                    dday: DDayLocalEntity,
                ) {
                    AccountDDayTransactionImpl(database = context.database).upsert(accountId = accountId, dday = dday, ddayTagList = emptyList())
                    context.link(accountId = accountId, entityId = dday.id, tagId = tagId)
                }

                suspend fun ddayList(
                    accountId: Uuid,
                    tagId: Uuid,
                ): List<DDayLocalEntity> =
                    AccountTagDDayLocalDataSourceImpl(database = context.database)
                        .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF)
                        .load(
                            androidx.paging.PagingSource.LoadParams
                                .Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
                        ).let { result -> (result as androidx.paging.PagingSource.LoadResult.Page).data }

                test("TC-725c3823-17b5-42f0-ac86-bd47416db6d4 종료일이 지났거나 다가오는 날짜가 없는 디데이도 노출한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val endedDDay =
                        Arb
                            .localDDay(
                                baseDate = LocalDate(2026, 1, 1),
                                repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                                endDate = LocalDate(2026, 3, 31),
                            ).next()
                    val passedDDay = Arb.localDDay(baseDate = LocalDate(2026, 1, 1)).next()
                    insertLinkedDDay(accountId = accountId, tagId = targetTag.id, dday = endedDDay)
                    insertLinkedDDay(accountId = accountId, tagId = targetTag.id, dday = passedDDay)

                    ddayList(accountId = accountId, tagId = targetTag.id).map { dday -> dday.id } shouldContainExactlyInAnyOrder
                        listOf(endedDDay.id, passedDDay.id)
                }

                test("TC-1f64fda5-cb8b-4c2a-b5c2-6df4e1939631 [디데이] 기준일을 바꾸면 바뀐 기준일로 다시 조회된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val exam = Arb.localDDay(baseDate = LocalDate(2026, 3, 20), title = "수능").next()
                    insertLinkedDDay(accountId = accountId, tagId = targetTag.id, dday = exam)
                    ddayList(accountId = accountId, tagId = targetTag.id).map { dday -> dday.detail.baseDate } shouldBe listOf(LocalDate(2026, 3, 20))

                    AccountDDayTransactionImpl(database = context.database).updateDetail(
                        accountId = accountId,
                        ddayId = exam.id,
                        detail = exam.detail.copy(baseDate = LocalDate(2026, 3, 21)),
                        updatedAt = Arb.secondPrecisionInstant().next(),
                    )

                    ddayList(accountId = accountId, tagId = targetTag.id).map { dday -> dday.id to dday.detail.baseDate } shouldBe
                        listOf(exam.id to LocalDate(2026, 3, 21))
                }
            },
        )
    })
