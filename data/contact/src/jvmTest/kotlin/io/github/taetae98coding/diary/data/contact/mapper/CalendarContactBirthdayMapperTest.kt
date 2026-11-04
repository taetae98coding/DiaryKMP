package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.LunarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday
import io.github.taetae98coding.diary.core.testing.contact.calendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.lunarContactBirthdayLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class CalendarContactBirthdayMapperTest :
    FunSpec({
        test("local to domain") {
            val local = Arb.calendarContactBirthdayLocalEntity().next()

            local.toDomain() shouldBe
                CalendarContactBirthday(
                    contactId = local.contactId,
                    name = local.name,
                    nameQualifier = local.nameQualifier.trim(),
                    date = local.birthdayDate,
                )
        }

        test("lunar local to domain") {
            val local = Arb.lunarContactBirthdayLocalEntity().next()

            local.toDomain() shouldBe
                LunarContactBirthday(
                    contactId = local.contactId,
                    name = local.name,
                    nameQualifier = local.nameQualifier.trim(),
                    birthday = local.birthday,
                )
        }

        test("구분 경로는 앞뒤 공백을 빼고 공백뿐이면 비운다") {
            listOf("" to "", "  " to "", "  회사  " to "회사").forEach { (stored, expected) ->
                Arb
                    .calendarContactBirthdayLocalEntity()
                    .next()
                    .copy(nameQualifier = stored)
                    .toDomain()
                    .nameQualifier shouldBe expected
                Arb
                    .lunarContactBirthdayLocalEntity()
                    .next()
                    .copy(nameQualifier = stored)
                    .toDomain()
                    .nameQualifier shouldBe expected
            }
        }
    })
