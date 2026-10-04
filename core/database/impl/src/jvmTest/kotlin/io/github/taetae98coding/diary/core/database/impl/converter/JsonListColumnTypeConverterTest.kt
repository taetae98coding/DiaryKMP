package io.github.taetae98coding.diary.core.database.impl.converter

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class JsonListColumnTypeConverterTest :
    FunSpec({
        test("웹 헤더 목록은 저장 문자열로 바꿨다가 되돌리면 같은 목록이 된다") {
            val converter = WebHeaderListColumnTypeConverter()
            val headerList = fixtureMonkey.giveMe<WebHeaderLocalEntity>(3)

            converter.textToHeaderList(converter.headerListToText(headerList)) shouldBe headerList
        }

        test("웹 헤더 목록은 이미 저장된 JSON 형식과 같은 문자열로 저장된다") {
            val converter = WebHeaderListColumnTypeConverter()
            val headerList = listOf(WebHeaderLocalEntity(name = "Cookie", value = "a=b"))
            val storedText = """[{"name":"Cookie","value":"a=b"}]"""

            converter.headerListToText(headerList) shouldBe storedText
            converter.textToHeaderList(storedText) shouldBe headerList
        }

        test("전화번호 목록은 저장 문자열로 바꿨다가 되돌리면 같은 목록이 된다") {
            val converter = ContactPhoneNumberListColumnTypeConverter()
            val phoneNumberList = fixtureMonkey.giveMe<ContactPhoneNumberLocalEntity>(3)

            converter.textToPhoneNumberList(converter.phoneNumberListToText(phoneNumberList)) shouldBe phoneNumberList
        }

        test("전화번호 목록은 이미 저장된 JSON 형식과 같은 문자열로 저장된다") {
            val converter = ContactPhoneNumberListColumnTypeConverter()
            val phoneNumberList = listOf(ContactPhoneNumberLocalEntity(number = "010-0000-0000"))
            val storedText = """[{"number":"010-0000-0000"}]"""

            converter.phoneNumberListToText(phoneNumberList) shouldBe storedText
            converter.textToPhoneNumberList(storedText) shouldBe phoneNumberList
        }

        test("빈 목록은 빈 JSON 배열로 저장되고 그대로 되돌아온다") {
            val converter = WebHeaderListColumnTypeConverter()

            converter.headerListToText(emptyList()) shouldBe "[]"
            converter.textToHeaderList("[]") shouldBe emptyList()
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
