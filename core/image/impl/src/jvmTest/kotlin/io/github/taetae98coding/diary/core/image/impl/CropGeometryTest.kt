package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CropGeometryTest :
    FunSpec({
        test("비율 영역을 사진 크기의 화소 사각형으로 옮긴다") {
            val region = ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F)

            region.toPixelRect(imageWidth = 400, imageHeight = 200) shouldBe PixelRect(left = 100, top = 0, width = 200, height = 200)
        }

        test("전체 영역은 사진 전체다") {
            ImageCropRegion.FULL.toPixelRect(imageWidth = 300, imageHeight = 120) shouldBe PixelRect(left = 0, top = 0, width = 300, height = 120)
        }

        test("반올림으로 폭이 없어지는 영역도 최소 한 화소를 남긴다") {
            val region = ImageCropRegion(left = 0.4F, top = 0.4F, right = 0.6F, bottom = 0.6F)

            val rect = region.toPixelRect(imageWidth = 1, imageHeight = 1)

            rect shouldBe PixelRect(left = 0, top = 0, width = 1, height = 1)
        }

        test("영역이 사진 끝에 닿아도 사진 밖으로 나가지 않는다") {
            val region = ImageCropRegion(left = 0.999F, top = 0.999F, right = 1F, bottom = 1F)

            val rect = region.toPixelRect(imageWidth = 10, imageHeight = 10)

            rect.right shouldBe 10
            rect.bottom shouldBe 10
            rect.width shouldBe 1
            rect.height shouldBe 1
        }

        test("긴 변이 최대 변 길이를 넘으면 그 길이로 줄이는 배율을 준다") {
            PixelRect(left = 0, top = 0, width = 2000, height = 1000).scaleToFit(maxSideLength = 1000) shouldBe 0.5
        }

        test("긴 변이 최대 변 길이를 넘지 않으면 늘리지 않는다") {
            PixelRect(left = 0, top = 0, width = 300, height = 300).scaleToFit(maxSideLength = 1024) shouldBe 1.0
        }

        test("배율을 적용한 크기는 최소 한 화소다") {
            PixelRect(left = 0, top = 0, width = 3, height = 1).scaled(scale = 0.1) shouldBe PixelSize(width = 1, height = 1)
            PixelSize(width = 4000, height = 3000).scaled(scale = 0.25) shouldBe PixelSize(width = 1000, height = 750)
        }
    })
