package io.github.taetae98coding.diary.core.network.impl.checklist.entity

import io.github.taetae98coding.diary.core.testing.checklist.remoteChecklist
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ChecklistPushRequestRemoteEntityTest :
    FunSpec({
        test("체크리스트 업로드 요청은 체크리스트가 가지는 값을 빠짐없이 담는다") {
            val request = ChecklistPushRequestRemoteEntity(checklistList = listOf(Arb.remoteChecklist(isDeleted = false).next()))

            val checklist =
                Json
                    .encodeToJsonElement(request)
                    .jsonObject
                    .getValue("checklistList")
                    .jsonArray
                    .single()
                    .jsonObject

            checklist.keys shouldBe setOf("id", "detail", "isDeleted", "updatedAt", "createdAt")
            checklist.getValue("detail").jsonObject.keys shouldBe setOf("title", "description")
        }
    })
