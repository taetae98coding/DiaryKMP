package io.github.taetae98coding.diary.data.checklist.mapper

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.core.testing.checklist.checklistSummary
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next

class ChecklistMapperTest :
    FunSpec({
        test("TC-6d542fda-df4b-434e-ad28-34ec2cb908db 체크리스트의 제목과 설명은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val checklist = Arb.checklist(isDeleted = Arb.boolean().next()).next()

            checklist.toLocal().toDomain() shouldBe checklist
        }

        test("TC-6d542fda-df4b-434e-ad28-34ec2cb908db 체크 항목의 내용과 체크 여부는 로컬 모델을 거쳐도 바뀌지 않는다") {
            val item = Arb.checklistItem(isChecked = Arb.boolean().next(), isDeleted = Arb.boolean().next()).next()

            item.toLocal().toDomain() shouldBe item
        }

        test("진행 정도는 체크한 수와 전체 수를 그대로 옮긴다") {
            val summary = Arb.checklistSummary().next()

            ChecklistSummaryLocalEntity(checklist = summary.checklist.toLocal(), checkedCount = summary.checkedCount, totalCount = summary.totalCount).toDomain() shouldBe summary
        }

        test("항목 정렬은 같은 이름의 로컬 정렬로 바뀐다") {
            ChecklistItemSort.entries.associateWith { sort -> sort.toLocal() } shouldBe
                mapOf(
                    ChecklistItemSort.ADDED to ChecklistItemSortLocalEntity.ADDED,
                    ChecklistItemSort.UNCHECKED_FIRST to ChecklistItemSortLocalEntity.UNCHECKED_FIRST,
                )
        }
    })
