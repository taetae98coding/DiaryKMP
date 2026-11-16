package io.github.taetae98coding.diary.core.testing.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import kotlin.uuid.Uuid

// 제목은 비어 있으면 저장하지 않으므로 앞에 고정 문자열을 붙인다.
public fun Arb.Companion.checklistDetail(
    title: String? = null,
    description: String? = null,
): Arb<ChecklistDetail> =
    arbitrary {
        ChecklistDetail(
            title = title ?: "title-${Arb.string().bind()}",
            description = description ?: Arb.string().bind(),
        )
    }

public fun Arb.Companion.checklist(
    isDeleted: Boolean,
    detail: ChecklistDetail? = null,
): Arb<Checklist> =
    arbitrary {
        Arb.checklist().bind().copy(
            detail = detail ?: Arb.checklistDetail().bind(),
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.checklistItem(
    isChecked: Boolean,
    isDeleted: Boolean,
    checklistId: Uuid? = null,
    content: String? = null,
): Arb<ChecklistItem> =
    arbitrary {
        Arb.checklistItem().bind().copy(
            checklistId = checklistId ?: Arb.kotlinUuid().bind(),
            content = content ?: "content-${Arb.string().bind()}",
            isChecked = isChecked,
            isDeleted = isDeleted,
        )
    }
