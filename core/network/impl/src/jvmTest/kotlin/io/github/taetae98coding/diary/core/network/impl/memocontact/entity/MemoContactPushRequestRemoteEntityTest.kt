package io.github.taetae98coding.diary.core.network.impl.memocontact.entity

import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity
import io.github.taetae98coding.diary.core.testing.memocontact.memoContactRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class MemoContactPushRequestRemoteEntityTest :
    FunSpec({
        test("TC-04b80b1a-7bba-496f-836c-41b1d6ee52dc 메모와 연락처의 연결 업로드 요청은 연결이 가지는 값을 빠짐없이 담는다") {
            val request = MemoContactPushRequestRemoteEntity(memoContactList = listOf(Arb.memoContactRemoteEntity().next()))

            val memoContact =
                Json
                    .encodeToJsonElement(request)
                    .jsonObject
                    .getValue("memoContactList")
                    .jsonArray
                    .single()
                    .jsonObject

            memoContact.keys shouldBe setOf("memoId", "contactId", "isDeleted", "updatedAt", "createdAt")
        }
    })
