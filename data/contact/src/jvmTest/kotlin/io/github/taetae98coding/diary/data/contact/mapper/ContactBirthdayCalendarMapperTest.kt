package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ContactBirthdayCalendarMapperTest :
    FunSpec({
        test("domain to local to domain") {
            ContactBirthdayCalendar.entries.forEach { calendar ->
                calendar.toLocal().toDomain() shouldBe calendar
            }
        }

        test("모든 도메인 구분이 서로 다른 로컬 구분으로 옮겨진다") {
            ContactBirthdayCalendar.entries
                .map { calendar -> calendar.toLocal() }
                .shouldContainExactly(ContactBirthdayCalendarLocalEntity.entries)
        }

        // 저장된 값은 상수 이름 변경으로 함께 바뀌지 않으므로 저장 문자열을 고정한다.
        test("로컬 구분의 저장 문자열은 고정되어 있다") {
            ContactBirthdayCalendarLocalEntity.SOLAR.persistentValue shouldBe "solar"
            ContactBirthdayCalendarLocalEntity.LUNAR.persistentValue shouldBe "lunar"
        }

        test("저장 문자열로 로컬 구분을 되찾는다") {
            ContactBirthdayCalendarLocalEntity.entries.forEach { calendar ->
                ContactBirthdayCalendarLocalEntity.fromPersistentValue(calendar.persistentValue) shouldBe calendar
            }
        }

        test("모르는 저장 문자열은 어떤 로컬 구분도 아니다") {
            ContactBirthdayCalendarLocalEntity.fromPersistentValue("SOLAR") shouldBe null
        }
    })
