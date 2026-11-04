package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseList
import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseWithoutBirthdayCalendar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb

class ContactDetailMapperTest :
    FunSpec({
        test("domain to local") {
            contactDetailCaseList().forEach { case ->
                case.domain.toLocal() shouldBe case.local
            }
        }

        test("local to domain") {
            (contactDetailCaseList() + contactDetailCaseWithoutBirthdayCalendar()).forEach { case ->
                case.local.toDomain() shouldBe case.domain
            }
        }

        test("생일과 다른 태어난 연도를 가진 모델은 생일의 연도로 저장한다") {
            contactDetailCaseList().filter { case -> case.domain.birthday != null }.forEach { case ->
                val birthYear = checkNotNull(case.domain.birthday).date.year

                case.domain
                    .copy(birthYear = birthYear + 1)
                    .toLocal()
                    .birthYear shouldBe birthYear
            }
        }

        test("domain to local to domain") {
            contactDetailCaseList().forEach { case ->
                case.domain.toLocal().toDomain() shouldBe case.domain
            }
        }
    })
