package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class ContactBirthdayCalendarRemoteMapperTest :
    FunSpec({
        test("local to remote to local") {
            ContactBirthdayCalendarLocalEntity.entries.forEach { calendar ->
                calendar.toRemote().toLocal() shouldBe calendar
            }
        }

        // 서버와 주고받는 이름도 상수 이름 변경으로 함께 바뀌지 않으므로 직렬화 결과를 고정한다.
        test("원격 구분의 직렬화 이름은 고정되어 있다") {
            Json.encodeToString(ContactBirthdayCalendarRemoteEntity.SOLAR) shouldBe "\"solar\""
            Json.encodeToString(ContactBirthdayCalendarRemoteEntity.LUNAR) shouldBe "\"lunar\""
        }
    })
