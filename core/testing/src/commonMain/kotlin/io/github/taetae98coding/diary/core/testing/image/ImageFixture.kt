package io.github.taetae98coding.diary.core.testing.image

import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.numericFloat
import io.kotest.property.arbitrary.pair

// 네 비율을 따로 만들면 left < right, top < bottom 제약을 거의 만족하지 못하므로,
// 두 값씩 만들어 작은 쪽을 시작으로 두고 같으면 다시 만든다.
public fun Arb.Companion.imageCropRegion(): Arb<ImageCropRegion> =
    arbitrary {
        val (left, right) = distinctRatioPair().bind()
        val (top, bottom) = distinctRatioPair().bind()

        ImageCropRegion(left = left, top = top, right = right, bottom = bottom)
    }

private fun distinctRatioPair(): Arb<Pair<Float, Float>> =
    Arb
        .pair(Arb.numericFloat(min = 0F, max = 1F), Arb.numericFloat(min = 0F, max = 1F))
        .filter { (first, second) -> first != second }
        .map { (first, second) -> minOf(first, second) to maxOf(first, second) }
