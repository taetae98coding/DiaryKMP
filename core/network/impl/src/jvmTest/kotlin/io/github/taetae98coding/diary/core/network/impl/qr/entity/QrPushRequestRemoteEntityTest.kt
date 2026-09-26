package io.github.taetae98coding.diary.core.network.impl.qr.entity

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.testing.qr.remoteQr
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class QrPushRequestRemoteEntityTest :
    FunSpec({
        // Edge Function은 스키마에 선언하지 않은 키를 버린 뒤 나머지만 저장하므로, 여기서 보내는 키가 늘거나 이름이 바뀌면
        // 함수 스키마도 함께 바꿔야 한다. 어긋나면 그 값만 조용히 사라진다.
        test("QR 업로드 요청은 QR이 가지는 값을 빠짐없이 담는다") {
            val request = QrPushRequestRemoteEntity(qrList = listOf(fixtureMonkey.remoteQr(isDeleted = false)))

            val qr =
                Json
                    .encodeToJsonElement(request)
                    .jsonObject
                    .getValue("qrList")
                    .jsonArray
                    .single()
                    .jsonObject

            qr.keys shouldBe setOf("id", "detail", "isDeleted", "updatedAt", "createdAt")
            qr.getValue("detail").jsonObject.keys shouldBe setOf("title", "description", "value")
        }

        test("QR 값의 줄바꿈과 앞뒤 공백을 바꾸지 않고 담는다") {
            val value = "  ${fixtureMonkey.giveMeOne<String>()}\n${fixtureMonkey.giveMeOne<String>()}  "
            val qr = fixtureMonkey.remoteQr(isDeleted = false).let { qr -> qr.copy(detail = qr.detail.copy(value = value)) }

            Json
                .encodeToJsonElement(QrPushRequestRemoteEntity(qrList = listOf(qr)))
                .jsonObject
                .getValue("qrList")
                .jsonArray
                .single()
                .jsonObject
                .getValue("detail")
                .jsonObject
                .getValue("value")
                .jsonPrimitive
                .content shouldBe value
        }
    })
