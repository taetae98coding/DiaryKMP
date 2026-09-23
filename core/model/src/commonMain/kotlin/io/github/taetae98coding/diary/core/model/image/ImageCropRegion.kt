package io.github.taetae98coding.diary.core.model.image

/**
 * 사진 전체를 기준으로 한 비율(0..1)로 남길 영역을 가리킨다.
 * 화면에 표시한 사진과 반영에 쓰는 사진의 화소 크기가 달라도 같은 부분을 가리키게 하기 위해서다.
 */
public data class ImageCropRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(left in 0F..1F && top in 0F..1F && right in 0F..1F && bottom in 0F..1F) {
            "Image crop region must be within 0..1. left=$left, top=$top, right=$right, bottom=$bottom"
        }
        require(left < right && top < bottom) {
            "Image crop region must not be empty. left=$left, top=$top, right=$right, bottom=$bottom"
        }
    }

    val width: Float get() = right - left

    val height: Float get() = bottom - top

    public companion object {
        public val FULL: ImageCropRegion = ImageCropRegion(left = 0F, top = 0F, right = 1F, bottom = 1F)
    }
}
