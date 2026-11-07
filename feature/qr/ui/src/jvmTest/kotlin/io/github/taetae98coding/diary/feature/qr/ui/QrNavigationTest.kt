package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class QrNavigationTest :
    FunSpec({
        test("TC-9912664a-b3ae-4d42-9e7d-61643a05cfde QrHome에서 QR 추가를 선택하면 초기 태그 없는 QR 추가로 이동한다") {
            val backStack = NavBackStack<ScreenNavKey>(QrHomeNavKey)

            backStack.navigateToQrAddFromHome()

            backStack shouldContainExactly listOf(QrHomeNavKey, QrAddNavKey())
            (backStack.last() as QrAddNavKey).initialTagId.shouldBeNull()
        }

        test("TC-75ce6921-30f2-4bf4-8343-b6e86cb0a42a QR 목록의 뒤로가기 동작은 상세 영역의 화면과 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(QrAddNavKey()),
                    listOf(QrDetailNavKey(id = Arb.kotlinUuid().next())),
                    listOf(QrDetailNavKey(id = Arb.kotlinUuid().next()), QrAddNavKey()),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = qrBackStack(detailKeyList = detailKeyList)

                backStack.navigateUpFromQrHome()

                backStack shouldContainExactly listOf(MoreHomeStubNavKey)
            }
        }

        test("TC-f9e861c8-4958-4974-8fe7-3e57a52a5998 QR 상세가 놓여 있으면 새 QR의 상세로 교체한다") {
            val first = QrDetailNavKey(id = Arb.kotlinUuid().next())
            val second = QrDetailNavKey(id = Arb.kotlinUuid().next())
            val backStack = qrBackStack(detailKeyList = listOf(first))

            backStack.navigateToQrDetail(id = second.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, QrHomeNavKey, second)
        }

        test("TC-76f07609-186c-4cf3-b52b-7fa4181c4896 목록에서 QR을 선택하면 목록 위에 쌓인 상세와 QR 추가를 모두 걷어내고 그 상세로 바꾼다") {
            val detailCases =
                listOf(
                    listOf(QrAddNavKey()),
                    listOf(QrDetailNavKey(id = Arb.kotlinUuid().next()), QrAddNavKey()),
                )

            detailCases.forEach { detailKeyList ->
                val selected = QrDetailNavKey(id = Arb.kotlinUuid().next())
                val backStack = qrBackStack(detailKeyList = detailKeyList)

                backStack.navigateToQrDetail(id = selected.id)

                backStack shouldContainExactly listOf(MoreHomeStubNavKey, QrHomeNavKey, selected)
            }
        }

        test("상세 영역에 QR 상세가 없으면 목록에서 선택한 QR 상세를 이어서 놓는다") {
            val detail = QrDetailNavKey(id = Arb.kotlinUuid().next())
            val backStack = qrBackStack(detailKeyList = emptyList())

            backStack.navigateToQrDetail(id = detail.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, QrHomeNavKey, detail)
        }

        test("QR 목록이 없는 전환 이력에서는 QR 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeStubNavKey, QrAddNavKey())
            val expected = backStack.toList()

            backStack.navigateUpFromQrHome()

            backStack shouldContainExactly expected
        }
    })

// QR 목록으로 진입하는 `더보기` 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreHomeStubNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStub"
}

private fun qrBackStack(detailKeyList: List<ScreenNavKey>): NavBackStack<ScreenNavKey> =
    NavBackStack(
        MoreHomeStubNavKey,
        QrHomeNavKey,
        *detailKeyList.toTypedArray(),
    )
