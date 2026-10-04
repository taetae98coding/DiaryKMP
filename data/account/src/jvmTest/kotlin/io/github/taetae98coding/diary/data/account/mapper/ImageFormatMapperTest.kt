package io.github.taetae98coding.diary.data.account.mapper

import io.github.taetae98coding.diary.core.model.image.ImageFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ImageFormatMapperTest :
    FunSpec({
        test("JPEG는 image/jpeg로 바꾼다") {
            ImageFormat.JPEG.toMimeType() shouldBe "image/jpeg"
        }
    })
