package io.github.taetae98coding.diary.core.testing.checklist

import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int

private const val MAX_ITEM_COUNT = 50

/**
 * 목록에 나타나는(삭제되지 않은) 체크리스트의 요약을 만든다. 체크한 수는 전체 수를 넘지 않는다.
 */
public fun Arb.Companion.checklistSummary(
    titlePrefix: String = "",
    title: String? = null,
    checkedCount: Int? = null,
    totalCount: Int? = null,
): Arb<ChecklistSummary> =
    arbitrary {
        val detail = Arb.checklistDetail().bind()
        val total = totalCount ?: Arb.int(0..MAX_ITEM_COUNT).bind()

        ChecklistSummary(
            checklist = Arb.checklist(isDeleted = false, detail = detail.copy(title = titlePrefix + (title ?: detail.title))).bind(),
            checkedCount = checkedCount ?: Arb.int(0..total).bind(),
            totalCount = total,
        )
    }
