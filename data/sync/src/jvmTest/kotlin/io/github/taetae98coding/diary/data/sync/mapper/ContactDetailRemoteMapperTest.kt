package io.github.taetae98coding.diary.data.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseList
import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseWithoutBirthdayCalendar
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ContactDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            (fixtureMonkey.contactDetailCaseList() + fixtureMonkey.contactDetailCaseWithoutBirthdayCalendar()).forEach { case ->
                case.local.toRemote() shouldBe case.remote
            }
        }

        test("remote to local") {
            fixtureMonkey.contactDetailCaseList().forEach { case ->
                case.remote.toLocal() shouldBe case.local
            }
        }

        test("local to remote to local") {
            fixtureMonkey.contactDetailCaseList().forEach { case ->
                case.local.toRemote().toLocal() shouldBe case.local
            }
        }
    })
