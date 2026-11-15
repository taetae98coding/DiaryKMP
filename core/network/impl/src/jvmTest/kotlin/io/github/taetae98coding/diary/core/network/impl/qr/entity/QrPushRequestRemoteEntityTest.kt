package io.github.taetae98coding.diary.core.network.impl.qr.entity

import io.github.taetae98coding.diary.core.testing.qr.remoteQr
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class QrPushRequestRemoteEntityTest :
    FunSpec({
        test("QR 업로드 요청은 QR이 가지는 값을 빠짐없이 담는다") {
            val request = QrPushRequestRemoteEntity(qrList = listOf(Arb.remoteQr(isDeleted = false).next()))

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
            val value = "  ${Arb.string().next()}\n${Arb.string().next()}  "
            val qr = Arb.remoteQr(isDeleted = false).next().let { qr -> qr.copy(detail = qr.detail.copy(value = value)) }

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
