@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class ContactEntryTest :
    FunSpec({
        test("TC-CONTACT-LIST-DETAIL-DOMAIN-002 연락처 목록에서 이어 진입한 연락처 추가는 목록·상세 배치의 상세 pane이다") {
            val backStack = listOf(OtherNavKey, ContactHomeNavKey, ContactAddNavKey)

            metadataOf(backStack = backStack, key = ContactAddNavKey).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-CONTACT-LIST-DETAIL-DOMAIN-001 연락처 목록에서 진입하지 않은 연락처 추가는 목록·상세 배치에 참여하지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherNavKey, ContactAddNavKey),
                    listOf(ContactHomeNavKey, OtherNavKey, ContactAddNavKey),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = ContactAddNavKey).shouldBeEmpty()
            }
        }

        test("TC-CONTACT-LIST-DETAIL-DOMAIN-003 연락처 목록에서 이어 진입한 연락처 상세는 목록·상세 배치의 상세 pane이다") {
            val detailKey = ContactDetailNavKey(id = Uuid.random())
            val backStack = listOf(OtherNavKey, ContactHomeNavKey, detailKey)

            metadataOf(backStack = backStack, key = detailKey).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-CONTACT-LIST-DETAIL-DOMAIN-004 연락처 목록에서 진입하지 않은 연락처 상세는 목록·상세 배치에 참여하지 않는다") {
            val detailKey = ContactDetailNavKey(id = Uuid.random())
            val backStackCases =
                listOf(
                    listOf(OtherNavKey, detailKey),
                    listOf(ContactHomeNavKey, OtherNavKey, detailKey),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = detailKey).shouldBeEmpty()
            }
        }

        test("연락처 추가 위에 놓인 연락처 상세도 목록·상세 배치의 상세 pane이다") {
            val detailKey = ContactDetailNavKey(id = Uuid.random())
            val backStack = listOf(ContactHomeNavKey, ContactAddNavKey, detailKey)

            metadataOf(backStack = backStack, key = detailKey).keys shouldBe detailPaneMetadataKeys
        }

        test("ContactHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherNavKey, ContactHomeNavKey)

            metadataOf(backStack = backStack, key = ContactHomeNavKey).keys shouldBe listPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<NavKey>,
            key: NavKey,
        ): Map<String, Any> {
            val provider = entryProvider<NavKey> { contactEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 연락처 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : NavKey
