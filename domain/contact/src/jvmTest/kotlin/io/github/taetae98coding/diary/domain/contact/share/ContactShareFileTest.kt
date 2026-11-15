package io.github.taetae98coding.diary.domain.contact.share

import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.testing.contact.contactDetail
import io.github.taetae98coding.diary.domain.share.ShareContent
import io.github.taetae98coding.diary.domain.share.ShareTarget
import io.github.taetae98coding.diary.domain.share.toShareTarget
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal class ContactShareFileTest :
    FunSpec({
        test("TC-037501f1-5c15-46ba-9f3b-efb77db9aedf 정해진 줄과 CR LF") {
            val detail =
                ContactDetail.EMPTY.copy(
                    name = "김철수",
                    phoneNumberList = listOf(ContactPhoneNumber(number = "010-1234-5678"), ContactPhoneNumber(number = "02-123-4567")),
                    birthday = ContactBirthday(date = LocalDate(year = 1990, month = 3, day = 5), calendar = ContactBirthdayCalendar.SOLAR),
                    birthYear = 1990,
                )

            detail.toContactShareFile(storedName = Arb.nonBlankString().next()).vCard shouldBe
                listOf("BEGIN:VCARD", "VERSION:3.0", "N:김철수", "FN:김철수", "TEL:010-1234-5678", "TEL:02-123-4567", "BDAY:1990-03-05", "END:VCARD")
                    .joinToString(separator = CRLF, postfix = CRLF)
        }

        test("TC-7c6f3ca9-de4c-46ad-9382-651084125130 전화번호 정리") {
            listOf(
                listOf(" 010-1234-5678 ", "02-123-4567") to listOf("010-1234-5678", "02-123-4567"),
                listOf("010-1234-5678", "", "   ", "02-123-4567") to listOf("010-1234-5678", "02-123-4567"),
                emptyList<String>() to emptyList(),
                listOf("") to emptyList(),
            ).forEach { (input, expected) ->
                val detail = ContactDetail.EMPTY.copy(name = "김철수", phoneNumberList = input.map(::ContactPhoneNumber))

                detail
                    .toContactShareFile(storedName = "")
                    .vCard
                    .lineList()
                    .filter { line -> line.startsWith("TEL") } shouldBe expected.map { number -> "TEL:$number" }
            }
        }

        test("TC-25761ed5-602b-4a59-a324-cc12ee599c63 양력 생일만") {
            val date = LocalDate(year = 1990, month = 3, day = 5)

            listOf(
                Triple(ContactBirthday(date = date, calendar = ContactBirthdayCalendar.SOLAR), 1990, listOf("BDAY:1990-03-05")),
                Triple(ContactBirthday(date = date, calendar = ContactBirthdayCalendar.LUNAR), 1990, emptyList()),
                Triple(null, 1990, emptyList()),
                Triple(null, null, emptyList()),
            ).forEach { (birthday, birthYear, expected) ->
                val detail = ContactDetail.EMPTY.copy(name = "김철수", birthday = birthday, birthYear = birthYear)

                detail
                    .toContactShareFile(storedName = "")
                    .vCard
                    .lineList()
                    .filter { line -> line.startsWith("BDAY") } shouldBe expected
            }
        }

        test("TC-72fc0313-5426-4750-8b3f-5fdbcf7496bf 생일 자리 수") {
            listOf(
                LocalDate(year = 1990, month = 3, day = 5) to "1990-03-05",
                LocalDate(year = 990, month = 12, day = 25) to "0990-12-25",
                LocalDate(year = 2004, month = 11, day = 14) to "2004-11-14",
            ).forEach { (date, expected) ->
                val detail = ContactDetail.EMPTY.copy(name = "김철수", birthday = ContactBirthday(date = date, calendar = ContactBirthdayCalendar.SOLAR), birthYear = date.year)

                detail
                    .toContactShareFile(storedName = "")
                    .vCard
                    .lineList()
                    .single { line -> line.startsWith("BDAY") } shouldBe "BDAY:$expected"
            }
        }

        test("TC-64f628b9-3473-4d0d-a749-de99ed3da654 담지 않는 값") {
            val description = Arb.alphanumericString(prefix = "Description").next()
            val hometown = Arb.alphanumericString(prefix = "Hometown").next()
            val howWeMet = Arb.alphanumericString(prefix = "HowWeMet").next()
            val detail =
                ContactDetail.EMPTY.copy(
                    name = "김철수",
                    description = description,
                    height = 175.0.centimeter,
                    footSize = 270.millimeter,
                    birthYear = 1990,
                    hometown = hometown,
                    howWeMet = howWeMet,
                )

            val vCard = detail.toContactShareFile(storedName = "").vCard

            vCard.lineList() shouldBe listOf("BEGIN:VCARD", "VERSION:3.0", "N:김철수", "FN:김철수", "END:VCARD")
            listOf(description, hometown, howWeMet, "175", "270", "1990").forEach { value -> vCard shouldNotContain value }
        }

        test("TC-a609bad2-93fe-44c3-8f44-7ca3da45d27f 특수 글자 바꾸기") {
            listOf(
                """김\철수""" to """김\\철수""",
                "김,철수" to """김\,철수""",
                "김;철수" to """김\;철수""",
                "김${'\n'}철수" to """김\n철수""",
            ).forEach { (name, expected) ->
                val lineList =
                    ContactDetail.EMPTY
                        .copy(name = name)
                        .toContactShareFile(storedName = "")
                        .vCard
                        .lineList()

                lineList.filter { line -> line.startsWith("N:") || line.startsWith("FN:") } shouldBe listOf("N:$expected", "FN:$expected")
            }

            val phoneLineList =
                ContactDetail.EMPTY
                    .copy(name = "김철수", phoneNumberList = listOf(ContactPhoneNumber(number = "010;1234,5678")))
                    .toContactShareFile(storedName = "")
                    .vCard
                    .lineList()

            phoneLineList.filter { line -> line.startsWith("TEL") } shouldBe listOf("""TEL:010\;1234\,5678""")
        }

        test("TC-c642252a-c4eb-47b2-b8e4-0c02d7914a5f 이름 정하기") {
            listOf(
                "김철수" to "김철수",
                " 김철수 " to "김철수",
                "" to "저장된이름",
                "   " to "저장된이름",
            ).forEach { (input, expected) ->
                val file = ContactDetail.EMPTY.copy(name = input).toContactShareFile(storedName = "저장된이름")

                file.name shouldBe expected
                file.vCard.lineList().filter { line -> line.startsWith("N:") || line.startsWith("FN:") } shouldBe listOf("N:$expected", "FN:$expected")
            }
        }

        test("TC-d08892da-0130-41bd-a11b-bd42e703ad0f 공유로 받으면 그대로 채운다") {
            val name = Arb.alphanumericString(prefix = "이름\\,;").next()
            val firstNumber = Arb.alphanumericString(prefix = "010;").next()
            val secondNumber = Arb.alphanumericString(prefix = "02,").next()
            val birthday = Arb.kotlinxLocalDate().next()
            val detail =
                Arb.contactDetail().next().copy(
                    name = " $name ",
                    phoneNumberList = listOf(ContactPhoneNumber(number = " $firstNumber "), ContactPhoneNumber(number = " "), ContactPhoneNumber(number = "$secondNumber\\")),
                    birthday = ContactBirthday(date = birthday, calendar = ContactBirthdayCalendar.SOLAR),
                    birthYear = birthday.year,
                )

            val target = ShareContent.Contact(vCard = detail.toContactShareFile(storedName = "").vCard).toShareTarget(timeZone = TimeZone.UTC)

            target.shouldBeInstanceOf<ShareTarget.Contact>() shouldBe
                ShareTarget.Contact(
                    name = name,
                    description = "",
                    phoneNumberList = listOf(firstNumber, "$secondNumber\\"),
                    birthday = birthday,
                )
        }
    })

private const val CRLF = "\r\n"

private fun String.lineList(): List<String> = removeSuffix(CRLF).split(CRLF)
