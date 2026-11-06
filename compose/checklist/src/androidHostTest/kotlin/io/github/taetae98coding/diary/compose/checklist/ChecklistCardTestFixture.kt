package io.github.taetae98coding.diary.compose.checklist

import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun testChecklistSummary(
    checkedCount: Int = 1,
    totalCount: Int = 3,
): ChecklistSummary =
    ChecklistSummary(
        checklist = Arb.checklist(isDeleted = false, detail = Arb.checklistDetail().next()).next(),
        checkedCount = checkedCount,
        totalCount = totalCount,
    )
