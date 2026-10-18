package io.github.taetae98coding.diary.feature.contact.ui.home.filter

import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class ContactFilterExistenceListTest :
    FunSpec({
        test("유무 필터의 세그먼트는 있음, 없음, 전체 순서다") {
            contactFilterExistenceList shouldContainExactly
                listOf(
                    ContactFilterExistence.EXIST,
                    ContactFilterExistence.NOT_EXIST,
                    ContactFilterExistence.ALL,
                )
        }

        test("유무 필터의 세그먼트는 모든 상태를 한 번씩만 담는다") {
            contactFilterExistenceList shouldContainExactlyInAnyOrder ContactFilterExistence.entries
        }
    })
