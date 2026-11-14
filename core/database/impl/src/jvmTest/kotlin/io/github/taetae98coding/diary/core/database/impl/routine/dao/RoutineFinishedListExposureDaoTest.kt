package io.github.taetae98coding.diary.core.database.impl.routine.dao

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.finishedRoutineSummaryList
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutine
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.linkRoutineTag
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.selectRoutineFilterTag
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class RoutineFinishedListExposureDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase

        beforeTest {
            database = routineTestDatabase()
        }

        afterTest {
            database.close()
        }

        suspend fun finishedIds(accountId: Uuid): List<Uuid> = database.finishedRoutineSummaryList(accountId = accountId, today = TODAY).map { summary -> summary.routine.id }

        test("TC-0aa087e9-d89b-45d2-9389-415f9cf0f59e 완료되고 삭제되지 않은 루틴만 목록에 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val finished = routine(title = "Alpha", isFinished = true, isDeleted = false)
            database.insertRoutine(
                accountId,
                finished,
                routine(title = "Bravo", isFinished = false, isDeleted = false),
                routine(title = "Charlie", isFinished = true, isDeleted = true),
                routine(title = "Delta", isFinished = false, isDeleted = true),
            )

            finishedIds(accountId) shouldBe listOf(finished.id)
        }

        test("TC-dc755a33-47d4-4d1e-8686-7658fddaf132 RoutineHome의 태그 필터에서 태그를 골라 두어도 연결된 루틴과 연결되지 않은 루틴이 모두 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val tagA = Arb.localTag(isFinished = false, isDeleted = false).next()
            val linked = routine(title = "Alpha", isFinished = true, isDeleted = false)
            val unlinked = routine(title = "Bravo", isFinished = true, isDeleted = false)
            database.insertRoutineTestTag(accountId, tagA)
            database.insertRoutine(accountId, linked, unlinked)
            database.linkRoutineTag(accountId = accountId, routineId = linked.id, tagId = tagA.id)
            database.selectRoutineFilterTag(accountId = accountId, tagId = tagA.id)

            finishedIds(accountId) shouldBe listOf(linked.id, unlinked.id)
        }
    }) {
    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)

        fun routine(
            title: String,
            isFinished: Boolean,
            isDeleted: Boolean,
        ): RoutineLocalEntity = Arb.localRoutine(startDate = LocalDate(2026, 1, 1), title = title, isFinished = isFinished, isDeleted = isDeleted).next()
    }
}
