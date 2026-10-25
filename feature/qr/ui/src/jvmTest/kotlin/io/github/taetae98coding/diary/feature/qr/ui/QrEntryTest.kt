@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.qr.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class QrEntryTest :
    FunSpec({
        test("TC-716e1825-c501-4fb2-80e9-4da12240bdbc QR 목록에서 이어 진입한 QR 추가는 목록·상세 배치의 상세 pane이다") {
            val backStack = listOf(OtherNavKey, QrHomeNavKey, QrAddNavKey)

            metadataOf(backStack = backStack, key = QrAddNavKey).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-3a2f6ba4-c418-49a0-b6eb-7905a5fca6c2 QR 목록에서 진입하지 않은 QR 추가는 목록·상세 배치에 참여하지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherNavKey, QrAddNavKey),
                    listOf(QrHomeNavKey, OtherNavKey, QrAddNavKey),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = QrAddNavKey).shouldBeEmpty()
            }
        }

        test("TC-0a34437d-40fb-40de-8b7a-53f3dc5293ae QR 목록에서 이어 진입한 QR 상세는 목록·상세 배치의 상세 pane이다") {
            val key = QrDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = listOf(OtherNavKey, QrHomeNavKey, QrAddNavKey, key)

            metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
        }

        test("QR 목록에서 진입하지 않은 QR 상세는 목록·상세 배치에 참여하지 않는다") {
            val key = QrDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = listOf(QrHomeNavKey, OtherNavKey, key)

            metadataOf(backStack = backStack, key = key).shouldBeEmpty()
        }

        test("QrHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherNavKey, QrHomeNavKey)

            metadataOf(backStack = backStack, key = QrHomeNavKey).keys shouldBe listPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { qrEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// QR 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
