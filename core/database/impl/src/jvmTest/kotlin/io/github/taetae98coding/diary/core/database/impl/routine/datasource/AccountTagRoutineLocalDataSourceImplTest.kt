package io.github.taetae98coding.diary.core.database.impl.routine.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.datasource.AccountEntityTagLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routine.findRoutineRecordList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineRecord
import io.github.taetae98coding.diary.core.database.impl.routine.loadAll
import io.github.taetae98coding.diary.core.database.impl.routine.routineCurrentCycleOf
import io.github.taetae98coding.diary.core.database.impl.routine.todayRoutineTitleList
import io.github.taetae98coding.diary.core.database.impl.routine.transaction.AccountRoutineTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.routinerecord.transaction.AccountRoutineRecordTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabCase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabItem
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.TagTabTc
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.loadTagTabPage
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.tagEntityTabLocalDataSourceTests
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val TODAY: LocalDate = LocalDate(2026, 3, 20)

// 공통 시나리오는 제목 순서만 확인하므로, 순서가 진행 상태에 따라 갈리지 않도록 모든 루틴을 오늘 2026-03-20에 진행 중으로 둔다.
private object RoutineTagTabCase : TagTabCase {
    override val kind = EntityTagKindLocalEntity.ROUTINE
    override val label = "루틴"
    override val tc =
        TagTabTc(
            exposure = "TC-f14336ab-7d70-45ab-8f72-734101899c96",
            order = "TC-d6db5574-94bc-44f0-8a9c-f6de4cfd94f1",
            paging = "TC-e75ab87f-0214-4f33-bca7-28a5a57471f0",
            change = "TC-ddbeec1c-afd0-43d6-81e4-5940ae265442",
            tagState = "TC-acb8c463-e767-4e39-84e8-8e363dcbb119",
            saveSelection = "TC-3d139967-7507-4ec1-8a56-cbee4e55df7a",
            otherTag = "TC-d854877e-29e2-49dd-8930-df977c2eea03",
        )

    override suspend fun upsert(
        database: DiaryDatabase,
        accountId: Uuid,
        item: TagTabItem,
        entityTagList: List<EntityTagLocalEntity>,
    ) {
        AccountRoutineTransactionImpl(database = database).upsert(
            accountId = accountId,
            routine =
                Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = item.title, isDeleted = item.isDeleted).next().copy(
                    id = item.id,
                    updatedAt = item.updatedAt,
                    createdAt = Arb.secondPrecisionInstant().next(),
                ),
            routineTagList = entityTagList,
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
        AccountTagRoutineLocalDataSourceImpl(database = database)
            .page(accountId = accountId, tagId = tagId, scope = scope, currentCycle = routineCurrentCycleOf(today = TODAY))
            .loadTagTabPage(key = key, loadSize = loadSize) { summary -> summary.routine.id }
}

internal class AccountTagRoutineLocalDataSourceImplTest :
    FunSpec({
        include(
            tagEntityTabLocalDataSourceTests(case = RoutineTagTabCase) { context ->
                suspend fun insertLinkedRoutine(
                    accountId: Uuid,
                    tagId: Uuid,
                    routine: RoutineLocalEntity,
                ) {
                    AccountRoutineTransactionImpl(database = context.database).upsert(accountId = accountId, routine = routine, routineTagList = emptyList())
                    context.link(accountId = accountId, entityId = routine.id, tagId = tagId)
                }

                suspend fun titleAmountList(
                    accountId: Uuid,
                    tagId: Uuid,
                    today: LocalDate = TODAY,
                ): List<Pair<String, Long>> =
                    AccountTagRoutineLocalDataSourceImpl(database = context.database)
                        .page(accountId = accountId, tagId = tagId, scope = TagScopeLocalEntity.SELF, currentCycle = routineCurrentCycleOf(today = today))
                        .loadAll()
                        .map { summary -> summary.routine.detail.title to summary.cycleAmount }

                suspend fun updateDetail(
                    accountId: Uuid,
                    routine: RoutineLocalEntity,
                ) {
                    AccountRoutineTransactionImpl(database = context.database).updateDetail(
                        accountId = accountId,
                        routineId = routine.id,
                        detail = routine.detail,
                        updatedAt = Arb.secondPrecisionInstant().next(),
                    ) shouldBe 1
                }

                suspend fun updateFinished(
                    accountId: Uuid,
                    routine: RoutineLocalEntity,
                    isFinished: Boolean,
                ) {
                    AccountRoutineTransactionImpl(database = context.database).updateFinished(
                        accountId = accountId,
                        routineId = routine.id,
                        isFinished = isFinished,
                        updatedAt = Arb.secondPrecisionInstant().next(),
                    ) shouldBe 1
                }

                test("TC-a1f41d54-c0d8-4878-b057-14a4166011f7 시작 전인 루틴도 기록과 목표 달성 여부와 관계없이 노출한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val notStarted = Arb.localRoutine(startDate = LocalDate(2026, 3, 21), title = "나").next()
                    val noRecord = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "가", goalAmount = 3).next()
                    val achieved = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "라", goalAmount = 3).next()
                    listOf(notStarted, noRecord, achieved).forEach { routine -> insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = routine) }
                    context.database.insertRoutineRecord(accountId = accountId, routineId = achieved.id, date = TODAY, amount = 5)

                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldContainExactlyInAnyOrder listOf("나" to 0L, "가" to 0L, "라" to 5L)
                }

                test("TC-86e7accc-cfd1-4b65-b43d-b944fa12ac0b 보기 범위를 적용하지 않아 하는 요일이 아닌 루틴과 시작 전인 루틴도 노출한다") {
                    val accountId = Arb.kotlinUuid().next()
                    val saturday = LocalDate(2026, 3, 21)
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    listOf(
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycleLocalEntity.WEEKDAYS, title = "가").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycleLocalEntity.WEEKENDS, title = "나").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycleLocalEntity.DAILY, title = "다").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 22), cycle = RoutineCycleLocalEntity.DAILY, title = "라").next(),
                    ).shuffled().forEach { routine -> insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = routine) }
                    AccountRoutineFilterLocalDataSourceImpl(database = context.database).upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.TODAY)

                    context.database.todayRoutineTitleList(accountId = accountId, today = saturday) shouldBe listOf("나", "다")
                    titleAmountList(accountId = accountId, tagId = targetTag.id, today = saturday).map { (title, _) -> title } shouldBe listOf("가", "나", "다", "라")
                }

                test("TC-d6db5574-94bc-44f0-8a9c-f6de4cfd94f1 TC-91435776-71d2-476b-b211-619f1bf8faf0 [루틴] 목록은 진행 중, 시작 전 순이고 오늘이 바뀌면 바뀐 상태의 자리로 옮겨진다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    listOf(
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 25), title = "가").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 21), title = "나").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 20), title = "다").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "라").next(),
                    ).forEach { routine -> insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = routine) }

                    titleAmountList(accountId = accountId, tagId = targetTag.id).map { (title, _) -> title } shouldBe listOf("다", "라", "가", "나")
                    titleAmountList(accountId = accountId, tagId = targetTag.id, today = LocalDate(2026, 3, 21)).map { (title, _) -> title } shouldBe listOf("나", "다", "라", "가")
                }

                test("TC-ddbeec1c-afd0-43d6-81e4-5940ae265442 [루틴] 기록, 주기, 시작 날짜와 완료 여부가 바뀌면 바뀐 합계와 노출, 순서로 다시 조회된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val water =
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 1), title = "물 마시기", unit = "ml", goalAmount = 2000).next()
                    val plant = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), title = "물 주기").next()
                    insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = water)
                    insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = plant)
                    context.database.insertRoutineRecord(accountId = accountId, routineId = water.id, date = TODAY, amount = 1000)
                    val half = context.database.insertRoutineRecord(accountId = accountId, routineId = water.id, date = TODAY, amount = 500)
                    context.database.insertRoutineRecord(accountId = accountId, routineId = water.id, date = LocalDate(2026, 3, 5), amount = 300)
                    val recordTransaction = AccountRoutineRecordTransactionImpl(database = context.database)
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 1500L, "물 주기" to 0L)

                    val added = Arb.localRoutineRecord(routineId = water.id, date = TODAY, amount = 250).next()
                    recordTransaction.upsert(accountId = accountId, record = added) shouldBe 1
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 1750L, "물 주기" to 0L)

                    recordTransaction.updateDeleted(accountId = accountId, recordId = added.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                    recordTransaction.updateDeleted(accountId = accountId, recordId = half.id, isDeleted = true, updatedAt = Arb.secondPrecisionInstant().next()) shouldBe 1
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 1000L, "물 주기" to 0L)

                    updateDetail(accountId, water.copy(detail = water.detail.copy(cycle = RoutineCycleLocalEntity.MONTHLY, goalAmount = 3000, unit = "count")))
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 1300L, "물 주기" to 0L)

                    updateDetail(accountId, water.copy(detail = water.detail.copy(startDate = LocalDate(2026, 3, 21))))
                    titleAmountList(accountId = accountId, tagId = targetTag.id).map { (title, _) -> title } shouldBe listOf("물 주기", "물 마시기")

                    updateDetail(accountId, water)
                    updateFinished(accountId, water, isFinished = true)
                    titleAmountList(accountId = accountId, tagId = targetTag.id).map { (title, _) -> title } shouldBe listOf("물 주기")

                    updateFinished(accountId, water, isFinished = false)
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 1000L, "물 주기" to 0L)

                    updateDetail(accountId, water.copy(detail = water.detail.copy(title = "물 한 잔")))
                    titleAmountList(accountId = accountId, tagId = targetTag.id).map { (title, _) -> title } shouldBe listOf("물 주기", "물 한 잔")
                }

                test("TC-f14336ab-7d70-45ab-8f72-734101899c96 TC-debd0ab6-6c88-45c9-b5e1-173144ddfc81 [루틴] 대상 태그와 연결되어도 완료된 루틴은 조회하지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    listOf(
                        Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "가").next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "나", isFinished = true).next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 3, 21), title = "다", isFinished = true).next(),
                        Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = "라", isFinished = true, isDeleted = true).next(),
                    ).forEach { routine -> insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = routine) }

                    titleAmountList(accountId = accountId, tagId = targetTag.id).map { (title, _) -> title } shouldBe listOf("가")
                }

                test("TC-29c21fac-0f7a-416d-8142-8e76cd310628 TC-69384adb-fbb6-457f-9257-ade7bdd6893d 완료와 삭제, 실행 취소는 루틴과 태그 연결을 남기고 상태와 수정 시각만 바꾼다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
                    insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = routine)
                    val transaction = AccountRoutineTransactionImpl(database = context.database)
                    val changeList: List<Pair<suspend (Instant) -> Int, (RoutineLocalEntity) -> RoutineLocalEntity>> =
                        listOf(
                            Pair({ updatedAt -> transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = true, updatedAt = updatedAt) }, { it.copy(isFinished = true) }),
                            Pair({ updatedAt -> transaction.updateFinished(accountId = accountId, routineId = routine.id, isFinished = false, updatedAt = updatedAt) }, { it }),
                            Pair({ updatedAt -> transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = updatedAt) }, { it.copy(isDeleted = true) }),
                            Pair({ updatedAt -> transaction.updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = false, updatedAt = updatedAt) }, { it }),
                        )

                    changeList.forEachIndexed { index, (change, expected) ->
                        val updatedAt = routine.updatedAt + (index + 1).seconds

                        change(updatedAt) shouldBe 1

                        AccountRoutineLocalDataSourceImpl(database = context.database).find(accountId = accountId, routineId = routine.id).first() shouldBe
                            expected(routine).copy(updatedAt = updatedAt)
                        AccountEntityTagLocalDataSourceImpl(database = context.database)
                            .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routine.id)
                            .first()
                            .map { tag -> tag.id } shouldBe listOf(targetTag.id)
                    }
                }

                test("TC-3391f2ea-8961-4fce-9dac-5ea83fde3d92 다른 기기에서 넣거나 뺀 루틴 기록을 내려받으면 합계에 반영된다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val water = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), title = "물 마시기", unit = "ml").next()
                    insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = water)
                    val stored = context.database.insertRoutineRecord(accountId = accountId, routineId = water.id, date = TODAY, amount = 1000)
                    val syncTransaction = AccountRoutineRecordSyncTransactionImpl(database = context.database)

                    syncTransaction.upsert(accountId = accountId, recordList = listOf(Arb.localRoutineRecord(routineId = water.id, date = TODAY, amount = 500).next()), cursor = 1L)
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 1500L)

                    syncTransaction.upsert(accountId = accountId, recordList = listOf(stored.copy(isDeleted = true, updatedAt = stored.updatedAt + 1.seconds)), cursor = 2L)
                    titleAmountList(accountId = accountId, tagId = targetTag.id) shouldBe listOf("물 마시기" to 500L)
                }

                test("TC-62595f56-152e-4f0a-bd16-5cbf6dcf8fe1 루틴을 삭제해도 그 루틴의 기록과 태그 연결은 지워지지 않는다") {
                    val accountId = Arb.kotlinUuid().next()
                    val targetTag = context.tag()
                    context.insertTag(accountId, targetTag)
                    val routine = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next()
                    insertLinkedRoutine(accountId = accountId, tagId = targetTag.id, routine = routine)
                    val recordList = List(2) { index -> context.database.insertRoutineRecord(accountId = accountId, routineId = routine.id, date = TODAY, amount = index + 1) }
                    val updatedAt = Arb.secondPrecisionInstant().next()

                    AccountRoutineTransactionImpl(database = context.database).updateDeleted(accountId = accountId, routineId = routine.id, isDeleted = true, updatedAt = updatedAt) shouldBe 1

                    AccountRoutineLocalDataSourceImpl(database = context.database).find(accountId = accountId, routineId = routine.id).first() shouldBe
                        routine.copy(isDeleted = true, updatedAt = updatedAt)
                    context.database.findRoutineRecordList() shouldContainExactlyInAnyOrder recordList
                    AccountEntityTagLocalDataSourceImpl(database = context.database)
                        .getTagList(accountId = accountId, kind = EntityTagKindLocalEntity.ROUTINE, entityId = routine.id)
                        .first()
                        .map { tag -> tag.id } shouldBe listOf(targetTag.id)
                }
            },
        )
    })
