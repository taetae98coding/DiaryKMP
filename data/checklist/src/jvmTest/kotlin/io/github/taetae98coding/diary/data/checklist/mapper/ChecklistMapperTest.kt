package io.github.taetae98coding.diary.data.checklist.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ChecklistMapperTest :
    FunSpec({
        test("TC-CHECKLIST-ADD-DATA-003 체크리스트의 제목과 설명은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val checklist = fixtureMonkey.checklist(isDeleted = fixtureMonkey.giveMeOne<Boolean>())

            checklist.toLocal().toDomain() shouldBe checklist
        }

        test("TC-CHECKLIST-ADD-DATA-003 체크 항목의 내용과 체크 여부는 로컬 모델을 거쳐도 바뀌지 않는다") {
            val item = fixtureMonkey.checklistItem(isChecked = fixtureMonkey.giveMeOne<Boolean>(), isDeleted = fixtureMonkey.giveMeOne<Boolean>())

            item.toLocal().toDomain() shouldBe item
        }

        test("진행 정도는 체크한 수와 전체 수를 그대로 옮긴다") {
            val checklist = fixtureMonkey.checklist(isDeleted = false)
            val checkedCount = fixtureMonkey.giveMeOne<Int>()
            val totalCount = fixtureMonkey.giveMeOne<Int>()

            ChecklistSummaryLocalEntity(checklist = checklist.toLocal(), checkedCount = checkedCount, totalCount = totalCount).toDomain() shouldBe
                ChecklistSummary(checklist = checklist, checkedCount = checkedCount, totalCount = totalCount)
        }

        test("항목 정렬은 같은 이름의 로컬 정렬로 바뀐다") {
            ChecklistItemSort.entries.associateWith { sort -> sort.toLocal() } shouldBe
                mapOf(
                    ChecklistItemSort.ADDED to ChecklistItemSortLocalEntity.ADDED,
                    ChecklistItemSort.UNCHECKED_FIRST to ChecklistItemSortLocalEntity.UNCHECKED_FIRST,
                )
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
