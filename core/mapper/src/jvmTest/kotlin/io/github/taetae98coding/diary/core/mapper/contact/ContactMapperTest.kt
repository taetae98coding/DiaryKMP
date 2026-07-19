package io.github.taetae98coding.diary.core.mapper.contact

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ContactMapperTest :
    FunSpec({
        test("domain to local") {
            contactCaseList.forEach { domain ->
                domain.toLocal() shouldBe
                    ContactLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isDeleted = domain.isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            contactCaseList.forEach { value ->
                val local = value.toLocal()

                local.toDomain() shouldBe
                    Contact(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isDeleted = local.isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("local to remote") {
            contactCaseList.forEach { value ->
                val local = value.toLocal()

                local.toRemote() shouldBe
                    ContactRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isDeleted = local.isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            contactCaseList.forEach { value ->
                val remote = value.toLocal().toRemote()

                remote.toLocal() shouldBe
                    ContactLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isDeleted = remote.isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            contactCaseList.forEach { domain ->
                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to remote to local") {
            contactCaseList.forEach { value ->
                val local = value.toLocal()

                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // 전화번호 목록은 비어 있을 수 있고 같은 번호도 합치지 않으므로 순서와 중복을 함께 확인한다.
        private val phoneNumberCaseList: List<List<ContactPhoneNumber>> =
            listOf(
                emptyList(),
                listOf(phoneNumber()),
                phoneNumber().let { duplicated -> listOf(duplicated, phoneNumber(), duplicated) },
            )

        // 키와 신발 사이즈와 생일은 없을 수 있고 없는 값을 0으로 대신하지 않으므로 있는 조건과 없는 조건을 함께 확인한다.
        private val measureCaseList: List<Boolean> = listOf(true, false)

        // isDeleted는 값이 둘뿐이라 다른 Boolean과 뒤바뀌어 매핑돼도 우연히 통과할 수 있으므로 두 값을 모두 확인한다.
        private val isDeletedCaseList: List<Boolean> = listOf(true, false)

        private val contactCaseList: List<Contact> =
            phoneNumberCaseList.flatMap { phoneNumberList ->
                measureCaseList.flatMap { hasMeasure ->
                    isDeletedCaseList.map { isDeleted ->
                        contact(phoneNumberList = phoneNumberList, hasMeasure = hasMeasure, isDeleted = isDeleted)
                    }
                }
            }

        private fun contact(
            phoneNumberList: List<ContactPhoneNumber>,
            hasMeasure: Boolean,
            isDeleted: Boolean,
        ): Contact =
            Contact(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail =
                    ContactDetail(
                        name = "name-${fixtureMonkey.giveMeOne<String>()}",
                        description = "description-${fixtureMonkey.giveMeOne<String>()}",
                        // 키는 센티미터와 밀리미터를 오가며 저장하므로, 왕복에서 부동소수점 오차가 끼지 않도록 소수점 아래 한 자리 값으로 만든다.
                        height = if (hasMeasure) (randomInt(bound = 2000) / 10.0).centimeter else null,
                        footSize = if (hasMeasure) randomInt(bound = 500).millimeter else null,
                        birthday =
                            if (hasMeasure) {
                                ContactBirthday(date = date(), calendar = fixtureMonkey.giveMeOne<ContactBirthdayCalendar>())
                            } else {
                                null
                            },
                        phoneNumberList = phoneNumberList,
                    ),
                isDeleted = isDeleted,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )

        private fun date(): LocalDate =
            LocalDate(
                year = 1900 + randomInt(bound = 200),
                month = 1 + randomInt(bound = 12),
                day = 1 + randomInt(bound = 28),
            )

        private fun randomInt(bound: Int): Int = (fixtureMonkey.giveMeOne<Int>().toUInt() % bound.toUInt()).toInt()

        private fun phoneNumber(): ContactPhoneNumber = ContactPhoneNumber(number = "number-${fixtureMonkey.giveMeOne<String>()}")
    }
}
