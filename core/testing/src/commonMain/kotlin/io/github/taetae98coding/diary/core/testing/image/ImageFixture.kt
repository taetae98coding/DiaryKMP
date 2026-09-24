package io.github.taetae98coding.diary.core.testing.image

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary

private val RATIO: Arbitrary<Float> = Arbitraries.floats().between(0F, 1F)

// 네 비율을 따로 만들면 left < right, top < bottom 제약을 거의 만족하지 못해 생성이 실패하므로,
// 두 값씩 만들어 작은 쪽을 시작으로 두고 같으면 다시 만든다.
public fun FixtureMonkey.imageCropRegion(): ImageCropRegion {
    val (left, right) = distinctRatioPair()
    val (top, bottom) = distinctRatioPair()

    return ImageCropRegion(left = left, top = top, right = right, bottom = bottom)
}

private fun FixtureMonkey.distinctRatioPair(): Pair<Float, Float> =
    generateSequence { ratio() to ratio() }
        .first { (first, second) -> first != second }
        .let { (first, second) -> minOf(first, second) to maxOf(first, second) }

private fun FixtureMonkey.ratio(): Float = giveMeBuilder<Float>().set("$", RATIO).sample()
