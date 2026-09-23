package io.github.taetae98coding.diary.core.network.impl.contact.entity

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ContactPushRequestRemoteEntityTest :
    FunSpec({
        // Edge Function은 스키마에 선언하지 않은 키를 버린 뒤 나머지만 저장하므로, 여기서 보내는 키가 늘거나 이름이 바뀌면
        // 함수 스키마도 함께 바꿔야 한다. 어긋나면 그 값만 조용히 사라진다.
        test("연락처 업로드 요청은 연락처가 가지는 값을 빠짐없이 담는다") {
            val request = ContactPushRequestRemoteEntity(contactList = listOf(fixtureMonkey.giveMeOne<ContactRemoteEntity>()))

            val contact =
                Json
                    .encodeToJsonElement(request)
                    .jsonObject
                    .getValue("contactList")
                    .jsonArray
                    .single()
                    .jsonObject

            contact.keys shouldBe setOf("id", "detail", "isFavorite", "isDeleted", "updatedAt", "createdAt")
            contact.getValue("detail").jsonObject.keys shouldBe
                setOf(
                    "name",
                    "description",
                    "heightCentimeter",
                    "footSizeMillimeter",
                    "birthday",
                    "birthdayCalendar",
                    "hometown",
                    "phoneNumberList",
                )
        }
    })
