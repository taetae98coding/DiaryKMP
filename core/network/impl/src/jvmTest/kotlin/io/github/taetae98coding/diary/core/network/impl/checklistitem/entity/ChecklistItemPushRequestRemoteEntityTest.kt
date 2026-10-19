package io.github.taetae98coding.diary.core.network.impl.checklistitem.entity

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.testing.checklist.remoteChecklistItem
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ChecklistItemPushRequestRemoteEntityTest :
    FunSpec({
        // Edge Function은 스키마에 선언하지 않은 키를 버린 뒤 나머지만 저장하므로, 여기서 보내는 키가 늘거나 이름이 바뀌면
        // 함수 스키마도 함께 바꿔야 한다. 어긋나면 그 값만 조용히 사라진다.
        test("TC-DATA-SYNC-DOMAIN-096 체크 항목 업로드 요청은 속한 체크리스트, 내용, 체크 여부, 삭제 여부와 시각을 빠짐없이 담는다") {
            val request =
                ChecklistItemPushRequestRemoteEntity(
                    checklistItemList = listOf(fixtureMonkey.remoteChecklistItem(isChecked = true, isDeleted = false)),
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
