package io.github.taetae98coding.diary.feature.account.ui.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ProfileImageViewerZoomStateTest :
    FunSpec({
        test("TC-78ce52be-ad6e-4fa6-bb36-908fd2b5934b 배율은 처음 배율과 그 5배 사이로 제한된다") {
            listOf(
                0.5F to 1F,
                1F to 1F,
                3F to 3F,
                5F to 5F,
                8F to 5F,
            ).forEach { (requested, applied) ->
                val state = ProfileImageViewerZoomState()
                state.updateSize(containerSize = Size(100F, 100F), imageSize = Size(100F, 100F))

                state.transform(centroid = Offset.Zero, pan = Offset.Zero, zoom = requested)

                state.scale shouldBe applied
            }
        }

        test("TC-fe533a3c-3347-477d-8c06-2f167c4887ac 확대한 이미지는 가장자리가 화면 안쪽으로 들어오지 않는 범위에서만 움직인다") {
            listOf(
                25F to 25F,
                100F to 50F,
                -100F to -50F,
            ).forEach { (moved, placed) ->
                val state = ProfileImageViewerZoomState()
                state.updateSize(containerSize = Size(100F, 100F), imageSize = Size(400F, 400F))
                state.transform(centroid = Offset.Zero, pan = Offset.Zero, zoom = 2F)

                state.transform(centroid = Offset.Zero, pan = Offset(x = moved, y = 0F), zoom = 1F)

                state.offset shouldBe Offset(x = placed, y = 0F)
            }
        }

        test("TC-2eca51bf-4b7e-46bc-83a5-82737e668b11 이미지가 화면보다 작은 방향으로는 움직이지 않는다") {
            val state = ProfileImageViewerZoomState()
            state.updateSize(containerSize = Size(100F, 100F), imageSize = Size(200F, 100F))

            state.transform(centroid = Offset.Zero, pan = Offset(x = 0F, y = 30F), zoom = 1F)

            state.offset shouldBe Offset.Zero
        }

        test("확대한 자리는 확대 뒤에도 같은 곳에 남는다") {
            val state = ProfileImageViewerZoomState()
            state.updateSize(containerSize = Size(100F, 100F), imageSize = Size(100F, 100F))

            state.transform(centroid = Offset(x = 20F, y = 0F), pan = Offset.Zero, zoom = 2F)

            state.offset shouldBe Offset(x = -20F, y = 0F)
        }

        test("되돌릴 때 정한 배율과 위치도 범위 안으로 맞춘다") {
            val state = ProfileImageViewerZoomState()
            state.updateSize(containerSize = Size(100F, 100F), imageSize = Size(100F, 100F))

            state.snapTo(scale = 10F, offset = Offset(x = 1000F, y = -1000F))

            state.scale shouldBe 5F
            state.offset shouldBe Offset(x = 200F, y = -200F)
        }
    })
