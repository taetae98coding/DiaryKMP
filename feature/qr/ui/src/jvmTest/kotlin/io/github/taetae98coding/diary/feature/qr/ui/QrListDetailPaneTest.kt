package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.isQrAddOnDetailPane
import io.github.taetae98coding.diary.feature.qr.api.isQrListDetailPane
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class QrListDetailPaneTest :
    FunSpec({
        test("TC-9605be3c-0841-42d9-abbd-9f694c10d02a 선택한 QR이 없거나 QR 추가로 이동했으면 상세 영역에 QR 추가가 놓인다") {
            val backStackCases =
                listOf(
                    listOf(QrHomeNavKey),
                    listOf(OtherScreenNavKey, QrHomeNavKey),
                    listOf(OtherScreenNavKey, QrHomeNavKey, QrAddNavKey()),
                )

            backStackCases.forEach { backStack ->
                backStack.isQrAddOnDetailPane() shouldBe true
            }
        }

        test("TC-7af608de-8aa2-4b0e-815c-d734a5d9ff3c QR 상세로 이동했으면 상세 영역에 QR 추가가 놓이지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherScreenNavKey, QrHomeNavKey, QrDetailNavKey(id = Arb.kotlinUuid().next())),
                    listOf(OtherScreenNavKey, QrHomeNavKey, QrAddNavKey(), QrDetailNavKey(id = Arb.kotlinUuid().next())),
                )

            backStackCases.forEach { backStack ->
                backStack.isQrAddOnDetailPane() shouldBe false
            }
        }

        test("QR 목록에서 진입하지 않은 QR 추가는 상세 영역에 놓인 것으로 보지 않는다") {
            val backStackCases =
                listOf(
                    emptyList(),
                    listOf(OtherScreenNavKey),
                    listOf(OtherScreenNavKey, QrAddNavKey()),
                    listOf(QrHomeNavKey, OtherScreenNavKey, QrAddNavKey()),
                )

            backStackCases.forEach { backStack ->
                backStack.isQrAddOnDetailPane() shouldBe false
            }
        }

        test("TagDetail QR 탭에서 진입한 QR 추가는 QR 목록 위에 있어도 상세 영역에 놓이지 않는다") {
            val backStack = listOf(OtherScreenNavKey, QrHomeNavKey, QrAddNavKey(initialTagId = Uuid.random()))

            backStack.isQrAddOnDetailPane() shouldBe false
            backStack.isQrListDetailPane(backStack.last()) shouldBe false
        }
    })

// QR 목록이 아닌 진입 화면을 대신한다. `더보기` 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherScreenNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
