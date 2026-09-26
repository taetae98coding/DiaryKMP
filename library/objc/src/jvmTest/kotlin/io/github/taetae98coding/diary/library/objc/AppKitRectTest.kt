package io.github.taetae98coding.diary.library.objc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppKitRectTest : FunSpec() {
    init {
        test("컨텐츠 전체를 덮는 프레임은 원점이 좌하단으로 옮겨져도 0이다") {
            appKitFrame(x = 0, y = 0, width = 800, height = 600, contentHeight = 600) shouldBe
                AppKitRect(x = 0.0, y = 0.0, width = 800.0, height = 600.0)
        }

        test("AWT 상단에 붙은 프레임은 AppKit에서 컨텐츠 높이만큼 아래에서 올라간 위치가 된다") {
            appKitFrame(x = 10, y = 0, width = 100, height = 50, contentHeight = 600) shouldBe
                AppKitRect(x = 10.0, y = 550.0, width = 100.0, height = 50.0)
        }

        test("AWT 하단에 붙은 프레임은 AppKit 원점에 놓인다") {
            appKitFrame(x = 10, y = 550, width = 100, height = 50, contentHeight = 600) shouldBe
                AppKitRect(x = 10.0, y = 0.0, width = 100.0, height = 50.0)
        }

        test("가로 좌표와 크기는 변환 없이 유지된다") {
            val frame = appKitFrame(x = 37, y = 120, width = 240, height = 180, contentHeight = 900)

            frame.x shouldBe 37.0
            frame.width shouldBe 240.0
            frame.height shouldBe 180.0
        }
    }
}
