package io.github.taetae98coding.diary.data.contact.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CalendarContactBirthdayMapperTest :
    FunSpec({
        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<CalendarContactBirthdayLocalEntity>()

            local.toDomain() shouldBe
                CalendarContactBirthday(
                    contactId = local.contactId,
                    name = local.name,
                    date = local.birthdayDate,
                )
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
