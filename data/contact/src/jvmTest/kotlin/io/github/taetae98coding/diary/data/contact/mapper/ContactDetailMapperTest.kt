package io.github.taetae98coding.diary.data.contact.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseList
import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseWithoutBirthdayCalendar
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ContactDetailMapperTest :
    FunSpec({
        test("domain to local") {
            fixtureMonkey.contactDetailCaseList().forEach { case ->
                case.domain.toLocal() shouldBe case.local
            }
        }

        test("local to domain") {
            (fixtureMonkey.contactDetailCaseList() + fixtureMonkey.contactDetailCaseWithoutBirthdayCalendar()).forEach { case ->
                case.local.toDomain() shouldBe case.domain
            }
        }

        test("domain to local to domain") {
            fixtureMonkey.contactDetailCaseList().forEach { case ->
                case.domain.toLocal().toDomain() shouldBe case.domain
            }
        }
    })
