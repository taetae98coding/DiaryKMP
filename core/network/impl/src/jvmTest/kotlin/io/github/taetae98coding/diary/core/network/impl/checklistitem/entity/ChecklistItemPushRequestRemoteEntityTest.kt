package io.github.taetae98coding.diary.core.network.impl.checklistitem.entity

import io.github.taetae98coding.diary.core.testing.checklist.remoteChecklistItem
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ChecklistItemPushRequestRemoteEntityTest :
    FunSpec({
        test("TC-860568ea-3f66-4d2d-833d-4eeeed777358 체크 항목 업로드 요청은 속한 체크리스트, 내용, 체크 여부, 삭제 여부와 시각을 빠짐없이 담는다") {
            val request =
                ChecklistItemPushRequestRemoteEntity(
                    checklistItemList = listOf(Arb.remoteChecklistItem(isChecked = true, isDeleted = false).next()),
                )

            Json
                .encodeToJsonElement(request)
                .jsonObject
                .getValue("checklistItemList")
                .jsonArray
                .single()
                .jsonObject
                .keys shouldBe setOf("id", "checklistId", "content", "isChecked", "isDeleted", "updatedAt", "createdAt")
        }
    })
