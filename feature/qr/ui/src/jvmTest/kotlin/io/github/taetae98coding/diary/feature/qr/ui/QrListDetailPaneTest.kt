package io.github.taetae98coding.diary.feature.qr.ui

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.isQrAddOnDetailPane
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class QrListDetailPaneTest :
    FunSpec({
        test("TC-9605be3c-0841-42d9-abbd-9f694c10d02a 선택한 QR이 없거나 QR 추가로 이동했으면 상세 영역에 QR 추가가 놓인다") {
            val backStackCases =
                listOf(
                    listOf(QrHomeNavKey),
                    listOf(OtherScreenNavKey, QrHomeNavKey),
                    listOf(OtherScreenNavKey, QrHomeNavKey, QrAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isQrAddOnDetailPane() shouldBe true
            }
        }

        test("TC-7af608de-8aa2-4b0e-815c-d734a5d9ff3c QR 상세로 이동했으면 상세 영역에 QR 추가가 놓이지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherScreenNavKey, QrHomeNavKey, QrDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(OtherScreenNavKey, QrHomeNavKey, QrAddNavKey, QrDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
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
                    listOf(OtherScreenNavKey, QrAddNavKey),
                    listOf(QrHomeNavKey, OtherScreenNavKey, QrAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isQrAddOnDetailPane() shouldBe false
            }
        }
    })

// QR 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherScreenNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
