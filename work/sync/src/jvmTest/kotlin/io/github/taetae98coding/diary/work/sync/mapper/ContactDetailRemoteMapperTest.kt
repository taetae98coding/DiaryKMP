package io.github.taetae98coding.diary.work.sync.mapper

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

        test("생일과 다른 태어난 연도가 저장된 로컬 연락처는 생일의 연도로 올린다") {
            fixtureMonkey.contactDetailCaseList().filter { case -> case.local.birthday != null }.forEach { case ->
                val birthYear = checkNotNull(case.local.birthday).year

                case.local
                    .copy(birthYear = birthYear + 1)
                    .toRemote()
                    .birthYear shouldBe birthYear
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
