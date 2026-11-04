package io.github.taetae98coding.diary.core.network.impl.contact.entity

import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.core.testing.contact.contactRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ContactPushRequestRemoteEntityTest :
    FunSpec({
        // Edge Function은 스키마에 선언하지 않은 키를 버린 뒤 나머지만 저장하므로, 여기서 보내는 키가 늘거나 이름이 바뀌면
        // 함수 스키마도 함께 바꿔야 한다. 어긋나면 그 값만 조용히 사라진다.
        test("연락처 업로드 요청은 연락처가 가지는 값을 빠짐없이 담는다") {
            val contact = Arb.contactRemoteEntity().next()

            val encoded = encodeContact(contact = contact)

            encoded.keys shouldBe CONTACT_KEY_SET
            encoded.getValue("detail").jsonObject.keys shouldBe DETAIL_KEY_SET
        }

        // 서버는 키가 없으면 이전 버전 앱의 요청으로 보고 저장된 값을 유지한다.
        // 비거나 없는 값도 키째 보내야 사용자가 지운 값이 서버에 반영된다.
        test("비어 있거나 없는 값도 키를 빼지 않고 담는다") {
            val contact =
                Arb.contactRemoteEntity().next().let { value ->
                    value.copy(
                        detail =
                            value.detail.copy(
                                description = "",
                                heightCentimeter = null,
                                footSizeMillimeter = null,
                                birthday = null,
                                birthdayCalendar = null,
                                birthYear = null,
                                hometown = "",
                                howWeMet = "",
                                phoneNumberList = emptyList(),
                            ),
                    )
                }

            val encoded = encodeContact(contact = contact)

            encoded.keys shouldBe CONTACT_KEY_SET
            encoded.getValue("detail").jsonObject.keys shouldBe DETAIL_KEY_SET
        }
    })

// supabase-kt가 요청 본문을 직렬화하는 설정과 같다. 기본값과 같은 프로퍼티는 키를 빼므로, 원격 엔티티에 기본값을 두면 이 테스트가 잡는다.
private val supabaseRequestJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

private val CONTACT_KEY_SET: Set<String> = setOf("id", "detail", "isFavorite", "isDeleted", "updatedAt", "createdAt")

private val DETAIL_KEY_SET: Set<String> =
    setOf(
        "name",
        "description",
        "heightCentimeter",
        "footSizeMillimeter",
        "birthday",
        "birthdayCalendar",
        "birthYear",
        "hometown",
        "howWeMet",
        "phoneNumberList",
    )

private fun encodeContact(contact: ContactRemoteEntity): JsonObject =
    supabaseRequestJson
        .encodeToJsonElement(ContactPushRequestRemoteEntity(contactList = listOf(contact)))
        .jsonObject
        .getValue("contactList")
        .jsonArray
        .single()
        .jsonObject
