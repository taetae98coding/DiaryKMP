package io.github.taetae98coding.diary.feature.contact.ui.detail.tab

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ContactDetailTabListTest :
    FunSpec({
        test("TC-CONTACT-DETAIL-FEATURE-033 탭은 연락처 디테일, 메모 순서다") {
            contactDetailTabList shouldContainExactly listOf(ContactDetailTab.DETAIL, ContactDetailTab.MEMO)
        }

        test("탭 목록은 모든 탭을 한 번씩만 담는다") {
            contactDetailTabList.toSet() shouldBe ContactDetailTab.entries.toSet()
            contactDetailTabList.size shouldBe ContactDetailTab.entries.size
        }
    })
