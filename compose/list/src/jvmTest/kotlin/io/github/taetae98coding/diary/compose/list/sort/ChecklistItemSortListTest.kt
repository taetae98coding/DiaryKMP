package io.github.taetae98coding.diary.compose.list.sort

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class ChecklistItemSortListTest :
    FunSpec({
        test("체크 항목 정렬은 추가순, 체크 안 된 항목 우선 순서다") {
            checklistItemSortList shouldContainExactly
                listOf(
                    ChecklistItemSort.ADDED,
                    ChecklistItemSort.UNCHECKED_FIRST,
                )
        }

        test("체크 항목 정렬 목록은 모든 정렬을 한 번씩 담는다") {
            checklistItemSortList shouldContainExactlyInAnyOrder ChecklistItemSort.entries
        }
    })
