package io.github.taetae98coding.diary.feature.contact.ui

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.isContactAddOnDetailPane
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class ContactListDetailPaneTest :
    FunSpec({
        test("TC-CONTACT-LIST-DETAIL-FEATURE-003 선택한 연락처가 없거나 연락처 추가로 이동했으면 상세 영역에 연락처 추가가 놓인다") {
            val backStackCases =
                listOf(
                    listOf(ContactHomeNavKey),
                    listOf(OtherScreenNavKey, ContactHomeNavKey),
                    listOf(OtherScreenNavKey, ContactHomeNavKey, ContactAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isContactAddOnDetailPane() shouldBe true
            }
        }

        test("TC-CONTACT-LIST-DETAIL-FEATURE-004 연락처 상세로 이동했으면 상세 영역에 연락처 추가가 놓이지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherScreenNavKey, ContactHomeNavKey, ContactDetailNavKey(id = Uuid.random())),
                    listOf(OtherScreenNavKey, ContactHomeNavKey, ContactAddNavKey, ContactDetailNavKey(id = Uuid.random())),
                )

            backStackCases.forEach { backStack ->
                backStack.isContactAddOnDetailPane() shouldBe false
            }
        }

        test("연락처 목록에서 진입하지 않은 연락처 추가는 상세 영역에 놓인 것으로 보지 않는다") {
            val backStackCases =
                listOf(
                    emptyList(),
                    listOf(OtherScreenNavKey),
                    listOf(OtherScreenNavKey, ContactAddNavKey),
                    listOf(ContactHomeNavKey, OtherScreenNavKey, ContactAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isContactAddOnDetailPane() shouldBe false
            }
        }
    })

// 연락처 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherScreenNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
