package io.github.taetae98coding.diary.core.mapper.contact

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPhoneNumberRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class ContactDetailMapperTest :
    FunSpec({
        test("domain to local") {
            domainOriginCaseList.forEach { case ->
                case.domain.toLocal() shouldBe case.local
            }
        }

        test("local to domain") {
            localOriginCaseList.forEach { case ->
                case.local.toDomain() shouldBe case.domain
            }
        }

        test("local to remote") {
            localOriginCaseList.forEach { case ->
                case.local.toRemote() shouldBe case.remote
            }
        }

        test("remote to local") {
            domainOriginCaseList.forEach { case ->
                case.remote.toLocal() shouldBe case.local
            }
        }

        test("domain to local to domain") {
            domainOriginCaseList.forEach { case ->
                case.domain.toLocal().toDomain() shouldBe case.domain
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // 전화번호 목록은 비어 있을 수 있고 같은 번호도 합치지 않으므로 순서와 중복을 함께 확인한다.
        private val phoneNumberCaseList: List<List<String>> =
            listOf(
                emptyList(),
                listOf(number()),
                number().let { duplicated -> listOf(duplicated, number(), duplicated) },
            )

        // 달력 구분은 계층마다 별도 타입이라 매퍼가 아니라 이 짝이 대응을 소유한다. null은 생일 자체가 없는 조건이다.
        private val calendarCaseList: List<Triple<ContactBirthdayCalendar, ContactBirthdayCalendarLocalEntity, ContactBirthdayCalendarRemoteEntity>?> =
            listOf(
                Triple(ContactBirthdayCalendar.SOLAR, ContactBirthdayCalendarLocalEntity.SOLAR, ContactBirthdayCalendarRemoteEntity.SOLAR),
                Triple(ContactBirthdayCalendar.LUNAR, ContactBirthdayCalendarLocalEntity.LUNAR, ContactBirthdayCalendarRemoteEntity.LUNAR),
                null,
            )

        // 키와 신발 사이즈는 없을 수 있고 없는 값을 0으로 대신하지 않으므로 있는 조건과 없는 조건을 함께 확인한다.
        private val measureCaseList: List<Boolean> = listOf(true, false)

        private val domainOriginCaseList: List<DetailCase> =
            calendarCaseList.flatMap { calendarCase ->
                phoneNumberCaseList.flatMap { numberList ->
                    measureCaseList.map { hasMeasure ->
                        detailCase(numberList = numberList, calendarCase = calendarCase, hasMeasure = hasMeasure)
                    }
                }
            }

        // 날짜와 달력 구분이 서로 다른 컬럼이라 날짜만 있고 구분이 비어 있는 로컬 값이 남아 있을 수 있다.
        // domain에서는 만들어지지 않는 조건이므로 local이 출발점인 방향에만 더한다. 기준을 양력 짝으로 잡아 기대값이 스펙의 기본값과 같다.
        private val solarCase: DetailCase =
            detailCase(numberList = phoneNumberCaseList.first(), calendarCase = calendarCaseList.first(), hasMeasure = true)

        private val localOriginCaseList: List<DetailCase> =
            domainOriginCaseList + solarCase.copy(local = solarCase.local.copy(birthdayCalendar = null))

        private fun detailCase(
            numberList: List<String>,
            calendarCase: Triple<ContactBirthdayCalendar, ContactBirthdayCalendarLocalEntity, ContactBirthdayCalendarRemoteEntity>?,
            hasMeasure: Boolean,
        ): DetailCase {
            val name = "name-${fixtureMonkey.giveMeOne<String>()}"
            val description = "description-${fixtureMonkey.giveMeOne<String>()}"
            // 키는 센티미터와 밀리미터를 오가며 저장하므로, 왕복에서 부동소수점 오차가 끼지 않도록 소수점 아래 한 자리 값으로 만든다.
            val heightCentimeter = if (hasMeasure) randomInt(bound = 2000) / 10.0 else null
            val footSizeMillimeter = if (hasMeasure) randomInt(bound = 500) else null
            val birthday = calendarCase?.let { (calendar, _, _) -> ContactBirthday(date = date(), calendar = calendar) }

            return DetailCase(
                domain =
                    ContactDetail(
                        name = name,
                        description = description,
                        height = heightCentimeter?.centimeter,
                        footSize = footSizeMillimeter?.millimeter,
                        birthday = birthday,
                        phoneNumberList = numberList.map { number -> ContactPhoneNumber(number = number) },
                    ),
                local =
                    ContactDetailLocalEntity(
                        name = name,
                        description = description,
                        heightCentimeter = heightCentimeter,
                        footSizeMillimeter = footSizeMillimeter,
                        birthday = birthday?.date,
                        birthdayCalendar = calendarCase?.second,
                        phoneNumberList = numberList.map { number -> ContactPhoneNumberLocalEntity(number = number) },
                    ),
                remote =
                    ContactDetailRemoteEntity(
                        name = name,
                        description = description,
                        heightCentimeter = heightCentimeter,
                        footSizeMillimeter = footSizeMillimeter,
                        birthday = birthday?.date,
                        birthdayCalendar = calendarCase?.third,
                        phoneNumberList = numberList.map { number -> ContactPhoneNumberRemoteEntity(number = number) },
                    ),
            )
        }

        private fun date(): LocalDate =
            LocalDate(
                year = 1900 + randomInt(bound = 200),
                month = 1 + randomInt(bound = 12),
                day = 1 + randomInt(bound = 28),
            )

        private fun randomInt(bound: Int): Int = (fixtureMonkey.giveMeOne<Int>().toUInt() % bound.toUInt()).toInt()

        private fun number(): String = "number-${fixtureMonkey.giveMeOne<String>()}"

        private data class DetailCase(
            val domain: ContactDetail,
            val local: ContactDetailLocalEntity,
            val remote: ContactDetailRemoteEntity,
        )
    }
}
