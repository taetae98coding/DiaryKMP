package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.checklistitem.checklistItemLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ChecklistRemoteMapperTest :
    FunSpec({
        test("로컬 체크리스트는 원격 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.checklistLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }

        test("로컬 체크 항목을 원격 모델로 바꿀 때 속한 체크리스트, 내용, 체크 여부와 삭제 여부를 그대로 옮긴다") {
            val local = Arb.checklistItemLocalEntity().next()

            local.toRemote() shouldBe
                ChecklistItemRemoteEntity(
                    id = local.id,
                    checklistId = local.checklistId,
                    content = local.content,
                    isChecked = local.isChecked,
                    isDeleted = local.isDeleted,
                    updatedAt = local.updatedAt,
                    createdAt = local.createdAt,
                )
            local.toRemote().toLocal() shouldBe local
        }
    })
